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

import org.openremote.controller.protocol.enocean.profile.EepOutOfRangeException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a linear scale which is used to convert raw EnOcean equipment profile (EEP)
 * data field values to engineering units. <p>
 *
 * EnOcean equipment profiles (EEP) are used to structure the payload field of an EnOcean
 * radio telegram. Each EnOcean equipment profile is formally specified by a table with rows
 * for each profile data field. The table contains the columns 'Valid Range' and 'Scale'
 * used to specify the conversion from raw values to engineering units.
 *
 * This class is used in combination with a {@link Range} data type. <p>
 *
 * @see org.openremote.controller.protocol.enocean.profile.EepDataField
 * @see Range
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class LinearScale
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Begin of raw value range.
   */
  BigDecimal minRawValue;

  /**
   * End of raw value range.
   */
  BigDecimal maxRawValue;

  /**
   * Begin of units value range.
   */
  BigDecimal minUnitsValue;

  /**
   * End of units value range.
   */
  BigDecimal maxUnitsValue;

  /**
   * Number of fractional digits.
   */
  private int fractionalDigits;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a linear scale instance with given input/output data ranges and number of
   * fractional digits.
   *
   * @param rawDataRange     raw data value range (see 'Valid Range' column of EnOcean
   *                         equipment profile (EEP) specification table).
   *
   * @param unitsDataRange   engineering units value range (see 'Scale' column of EnOcean
   *                         equipment profile (EEP) specification table).

   * @param fractionalDigits number of fractional digits
   */
  public LinearScale(DataRange rawDataRange, DataRange unitsDataRange, int fractionalDigits)
  {
    if(rawDataRange == null)
    {
      throw new IllegalArgumentException("null raw data range");
    }

    if(unitsDataRange == null)
    {
      throw new IllegalArgumentException("null units data range");
    }

    this.fractionalDigits = fractionalDigits;

    if(rawDataRange.getStart().compareTo(rawDataRange.getEnd()) < 0)
    {
      maxUnitsValue = unitsDataRange.getEnd();
      minUnitsValue = unitsDataRange.getStart();
      maxRawValue = rawDataRange.getEnd();
      minRawValue = rawDataRange.getStart();
    }

    else
    {
      maxUnitsValue = unitsDataRange.getStart();
      minUnitsValue = unitsDataRange.getEnd();
      maxRawValue = rawDataRange.getStart();
      minRawValue = rawDataRange.getEnd();
    }
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Scales a raw value to engineering units.
   *
   * @param  rawValue  raw value from EnOcean equipment profile (EEP) data field.
   *
   * @return scaled value (engineering unit)
   *
   * @throws EepOutOfRangeException
   *           if the raw value parameter is not within the raw value range
   */

  public BigDecimal scaleRawValue(int rawValue) throws EepOutOfRangeException
  {
    if(rawValue < minRawValue.intValue() || rawValue > maxRawValue.intValue())
    {
      throw new EepOutOfRangeException(
        "Raw value ''{0}'' is out of valid range [{1}..{2}].",
        rawValue, minRawValue.intValue(), maxRawValue.intValue()
      );
    }

    BigDecimal decRawValue = BigDecimal.valueOf(rawValue);
    BigDecimal scaledValue;

    int divideScale = Math.max(minUnitsValue.scale(), this.fractionalDigits);

    scaledValue = maxUnitsValue.subtract(minUnitsValue)
                  .multiply(
                      decRawValue.subtract(minRawValue))
                  .divide(
                      maxRawValue.subtract(minRawValue), divideScale, RoundingMode.HALF_UP)
                  .add(
                      minUnitsValue)
                  .setScale(this.fractionalDigits, RoundingMode.HALF_UP);

    return scaledValue;
  }
}
