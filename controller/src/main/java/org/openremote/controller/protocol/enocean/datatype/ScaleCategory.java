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

/**
 * EnOcean equipment profile (EEP) scale category.
 *
 * EnOcean equipment profiles (EEP) are used to structure the payload field of an EnOcean
 * radio telegram. Each EnOcean equipment profile is formally specified by a table with rows
 * for each profile data field. The table contains the column 'Scale' which is used to specify
 * a linear scale or a categorical scale. This class represents a scale category item of a
 * categorical scale.
 *
 * @see CategoricalScale
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class ScaleCategory
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Scale category name mainly used for logging purposes. <p>
   */
  private String name;

  /**
   * Start of raw value data range.
   */
  private int minValue;

  /**
   * End of raw value data range.
   */
  private int maxValue;

  /**
   * Value for updating sensors with a state value (mainly state sensors).
   */
  private String sensorStateValue;

  /**
   * Value for updating sensors with a numeric value.
   */
  private int sensorValue;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a scale category instance.
   *
   * @param name              scale category name mainly used for logging purposes
   *
   * @param minValue          start of raw value data range
   *
   * @param maxValue          end of raw value data range
   *
   * @param sensorStateValue  value for updating sensors with a state value (mainly state sensors)
   *
   * @param sensorValue       value for updating sensors with a numeric value
   */
  public ScaleCategory(String name, int minValue, int maxValue, String sensorStateValue, int sensorValue)
  {
    this.name = name;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.sensorStateValue = sensorStateValue;
    this.sensorValue = sensorValue;
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * Tests category object equality based on raw value range.
   *
   * @param   o   device ID object to compare to
   *
   * @return  true if equals, false otherwise
   */
  @Override public boolean equals(Object o)
  {
    if(o == null)
      return false;

    if(!o.getClass().equals(this.getClass()))
      return false;

    ScaleCategory category = (ScaleCategory)o;

    return this.minValue == category.minValue &&
           this.maxValue == category.maxValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override public int hashCode()
  {
    return minValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    return "EEP Scale Category (Name = '" + name + "', Range = [" + minValue +
           "..." + maxValue  + "], Enum = '" + sensorValue + "')";
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Determines if a raw value falls into this scale category.
   *
   * @param  rawValue  raw value from EnOcean equipment profile (EEP) data field
   *
   * @return true if the raw value falls into this scale category, otherwise false
   */
  public boolean fallsIntoCategory(int rawValue)
  {
    if(rawValue >= minValue && rawValue <= maxValue)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Returns the category name (mainly used for logging purposes).
   *
   * @return the category name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the start of the raw value range.
   *
   * @return start of value range
   */
  public int getMinValue()
  {
    return minValue;
  }

  /**
   * Returns the end of the raw value range.
   *
   * @return end of value range
   */
  public int getMaxValue()
  {
    return maxValue;
  }

  /**
   * Returns the value for updating sensors with a state value.
   *
   * @return the sensor state value
   */
  public String getSensorStateValue()
  {
    return sensorStateValue;
  }

  /**
   * Returns the value for updating sensors with a numeric value.
   *
   * @return the sensor value
   */
  public int getSensorValue()
  {
    return sensorValue;
  }
}
