/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.enocean.datatype;

import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.profile.EepData;
import org.openremote.controller.protocol.enocean.profile.EepDataField;
import org.openremote.controller.protocol.enocean.profile.EepDataListener;
import org.openremote.controller.protocol.enocean.profile.EepOutOfRangeException;
import org.openremote.controller.utils.Logger;

import java.math.BigDecimal;

/**
 * Represents an EnOcean equipment profile (EEP) data field type which
 * contains a continuous value (as opposed to an ordinal value).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Range implements DataType, EepDataListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  /**
   * Factory method for creating new Range instances with given EnOcean equipment profile (EEP)
   * data field and data range parameters.
   *
   * @param  name                EnOcean equipment profile (EEP) data field name
   *
   * @param  offset              start bit of EnOcean equipment profile (EEP) data field
   *
   * @param  size                bit size of EnOcean equipment profile (EEP) data field
   *
   * @param  rawDataRangeMin     begin of raw value data range
   *
   * @param  rawDataRangeMax     end of raw value data range
   *
   * @param  unitsDataRangeMin   begin of scaled data range
   *
   * @param  unitsDataRangeMax   end of scaled data range
   *
   * @param  fractionalDigits    number of fractional digits to be used for scaled values
   *
   * @return new Range instance
   */
  public static Range createRange(String name, int offset, int size,
                                  int rawDataRangeMin, int rawDataRangeMax,
                                  double unitsDataRangeMin, double unitsDataRangeMax,
                                  int fractionalDigits)
  {
    EepDataField dataField = new EepDataField(name, offset, size);

    DataRange rawDataRange = new DataRange(rawDataRangeMin, rawDataRangeMax);
    DataRange unitsDataRange = new DataRange(unitsDataRangeMin, unitsDataRangeMax);
    LinearScale scale = new LinearScale(rawDataRange, unitsDataRange, fractionalDigits);

    Range range = new Range(dataField, scale);

    return range;
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) data field.
   */
  private EepDataField dataField;

  /**
   * Scale for converting raw EnOcean equipment profile (EEP) data field values to
   * floating point values
   */
  private LinearScale scale;

  /**
   * Raw EnOcean equipment profile (EEP) data field value.
   */
  private Integer rawValue;


  /**
   * Scaled {@link #rawValue}.
   */
  private BigDecimal scaledValue;

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a range instance with given data field and scale.
   *
   * @param dataField  EnOcean equipment profile (EEP) data field
   *
   * @param scale      scale for converting raw EnOcean equipment profile (EEP) data field
   *                   values to continuous values
   */
  public Range(EepDataField dataField, LinearScale scale)
  {
    if(dataField == null)
    {
      throw new IllegalArgumentException("null data field");
    }

    if(scale == null)
    {
      throw new IllegalArgumentException("null scale");
    }

    this.dataField = dataField;
    this.scale = scale;
  }

  /**
   * Constructs a range instance with given data field and raw value.
   *
   * @param dataField  EnOcean equipment profile (EEP) data field
   *
   * @param rawValue   raw value
   */
  public Range(EepDataField dataField, int rawValue)
  {
    if(dataField == null)
    {
      throw new IllegalArgumentException("null data field");
    }

    this.dataField = dataField;
    this.rawValue = rawValue;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the value of this <tt>Range</tt> object as a primitive value.
   *
   * @return  range value. Returns <tt>null</tt> if there was no data update since creation of
   *          this <tt>Range</tt> object
   */
  public Double rangeValue()
  {
    if(scaledValue != null)
    {
      return scaledValue.doubleValue();
    }

    else if(rawValue != null)
    {
      return (double)rawValue;
    }

    else
    {
      return null;
    }
  }


  // Implements EepDataListener -------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void didUpdateData(EepData data) throws EepOutOfRangeException
  {
    int newRawValue = dataField.read(data);

    if(scale != null)
    {
      // may throw EepOutOfRangeException
      this.scaledValue = scale.scaleRawValue(newRawValue);
    }

    this.rawValue = newRawValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override public void updateData(EepData data) throws EepOutOfRangeException
  {
    if(rawValue == null)
    {
      return;
    }

    this.dataField.write(rawValue, data);
  }


  // Implements DataType --------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void updateSensor(Sensor sensor)
  {
    if(scaledValue == null)
    {
      return;
    }

    if(sensor instanceof RangeSensor)
    {
      sensor.update("" + scaledValue.intValue());
    }

    else if (sensor instanceof SwitchSensor)
    {
      log.warn(
          "Failed to update sensor ''{0}'' because SWITCH sensor " +
          "cannot be linked with ''{1}''", sensor, dataField
      );
    }

    else if (sensor instanceof StateSensor)
    {
      sensor.update(scaledValue.toString());
    }

    else
    {
      throw new RuntimeException("Unrecognized sensor type: " + sensor.toString());
    }
  }
}
