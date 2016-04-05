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
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.profile.EepDataField;
import org.openremote.controller.utils.Logger;

import static org.openremote.controller.protocol.enocean.profile.EepConstants.*;

/**
 * Represents a boolean EnOcean equipment profile (EEP) data field type.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Bool extends Ordinal
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);

  /**
   * Factory method for creating a boolean data type based on raw value ranges
   * representing the true/false states.
   *
   * @param name       EnOcean equipment profile (EEP) data field name
   *
   * @param offset     start bit of EnOcean equipment profile (EEP) data field
   *
   * @param size       bit size of EnOcean equipment profile (EEP) data field
   *
   * @param trueName   name of true state
   *
   * @param trueMin    begin of raw value range representing the true state
   *
   * @param trueMax    end of raw value range representing the true state
   *
   * @param falseName  name of false state
   *
   * @param falseMin   begin of raw value range representing the false state
   *
   * @param falseMax   end of raw value range representing the false state
   *
   * @return  new bool data type instance
   */
  public static Bool createBoolWithRange(String name, int offset, int size,
                                         String trueName, int trueMin, int trueMax,
                                         String falseName, int falseMin, int falseMax)
  {
    EepDataField dataField = new EepDataField(name, offset, size);

    // TODO : on/off constants
    ScaleCategory trueCategory = new ScaleCategory(trueName, trueMin, trueMax, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory(falseName, falseMin, falseMax, "off", 0);
    BoolScale scale = new BoolScale(trueCategory, falseCategory);

    Bool bool = new Bool(dataField, scale);

    return bool;
  }

  /**
   * Factory method for creating a boolean data type based on single values
   * representing the true/false states.
   *
   * @param name       EnOcean equipment profile (EEP) data field name
   *
   * @param offset     start bit of EnOcean equipment profile (EEP) data field
   *
   * @param size       bit size of EnOcean equipment profile (EEP) data field
   *
   * @param trueName   name of true state
   *
   * @param trueValue  raw data value representing the true state
   *
   * @param falseName  name of false state
   *
   * @param falseValue raw data value representing the false state
   *
   * @return  new bool data type instance
   */
  public static Bool createBool(String name, int offset, int size,
                                String trueName, int trueValue,
                                String falseName, int falseValue)
  {
    return createBoolWithRange(
        name, offset, size,
        trueName, trueValue, trueValue,
        falseName, falseValue, falseValue
    );
  }

  /**
   * Factory method for creating a bool data type representing the teach in flag
   * for a {@link org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram.RORG#BS4 4BS}
   * radio telegram.
   *
   * @return new bool data type instance
   */
  public static Bool createTeachInFlag4BS()
  {
    return Bool.createBool(
        EEP_LEARN_BIT_DATA_FIELD_NAME, EEP_LEARN_BIT_DATA_FIELD_OFFSET_4BS, EEP_LEARN_BIT_DATA_FIELD_SIZE,
        EEP_LEARN_BIT_DATA_FIELD_TEACH_IN_TELEGRAM_DESC, EEP_LEARN_BIT_DATA_FIELD_TEACH_IN_TELEGRAM_VALUE,
        EEP_LEARN_BIT_DATA_FIELD_DATA_TELEGRAM_DESC, EEP_LEARN_BIT_DATA_FIELD_DATA_TELEGRAM_VALUE
    );
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) data field.
   */
  private EepDataField dataField;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a bool instance with given data field and scale.
   *
   * @param dataField  EnOcean equipment profile (EEP) data field
   *
   * @param scale      scale for converting raw EnOcean equipment profile (EEP) data field
   *                   values to a boolean value
   */
  public Bool(EepDataField dataField, BoolScale scale)
  {
    super(dataField, scale);
  }

  /**
   * Constructs a bool instance with given data field and raw value.
   *
   * @param dataField  EnOcean equipment profile (EEP) data field.
   *
   * @param rawValue   raw value (<tt>true</tt> --> 1, <tt>false</tt> --> 0)
   */
  public Bool(EepDataField dataField, boolean rawValue)
  {
    super(dataField, rawValue ? 1 : 0);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the value of this <tt>Bool</tt> object as a boolean
   * primitive.
   *
   * @return <tt>true</tt> if the object represents the boolean primitive value <tt>true</tt>,
   *         otherwise <tt>false</tt>
   */
  public Boolean boolValue()
  {
    if(category != null)
    {
      return ((BoolScale)scale).isTrue(category);
    }

    else if(rawValue != null)
    {
      return (rawValue == 0 ? false : true);
    }

    else
    {
      return null;
    }
  }


  // Implements DataType --------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void updateSensor(Sensor sensor)
  {
    if(category == null)
    {
      return;
    }

    if(sensor instanceof StateSensor)
    {
      sensor.update(category.getSensorStateValue());
    }

    else if(sensor instanceof RangeSensor)
    {
      sensor.update("" + category.getSensorValue());
    }

    else
    {
      throw new RuntimeException("Unrecognized sensor type: " + sensor.toString());
    }
  }
}
