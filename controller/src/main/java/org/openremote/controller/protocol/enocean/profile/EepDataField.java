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
package org.openremote.controller.protocol.enocean.profile;

/**
 * EnOcean equipment profile (EEP) data field. <p>
 *
 * EnOcean equipment profiles are used to structure the payload field of an EnOcean
 * radio telegram. Each EnOcean equipment profile is formally specified by a table with rows
 * for each profile data field. The table contains the columns 'Offset' and 'Size' which are
 * used to specify the data field location within the radio telegram payload field. <p>
 *
 * This class offers methods to read/write raw values from/to an EnOcean equipment profile
 * data field. <p>
 *
 * Note that EnOcean equipment profile (EEP) data field values are stored
 * in big-endian format.
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepDataField
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Data field name.
   */
  private String name;

  /**
   * Start bit of data field.
   */
  private int offset;

  /**
   * Number of data field bits.
   */
  private int size;

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a data field instance with given offset and size.
   *
   * @param name    data field name
   *
   * @param offset  start bit of data field
   *
   * @param size    number of data field bits
   */
  public EepDataField(String name, int offset, int size)
  {
    this.name = name;
    this.offset = offset;
    this.size = size;
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    StringBuilder builder = new StringBuilder();

    String name = "Name = '" + this.name + "', ";
    String offsetString = "Offset = '" + this.offset + "', ";
    String sizeString = "Size = '" + this.size + "'";

    builder
        .append("EEP data field (")
        .append(name)
        .append(offsetString)
        .append(sizeString)
        .append(")");

    return builder.toString();
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Retrieves the data field value from the given EnOcean equipment profile (EEP)
   * data and returns it.
   *
   * @param  data  EnOcean equipment (EEP) profile data
   *
   * @return data field value
   */
  public int read(EepData data)
  {
    checkDataParameter(data);

    int curSize = this.size;
    int curDataByteIndex = getLSBIndex();
    int curDataBitIndex = getLSBStartBitIndex();
    int curDataBitLength;
    int curValueBitIndex = 0;

    int value = 0;

    while(curSize > 0)
    {
      if(curSize >= (Byte.SIZE - curDataBitIndex))
      {
        curDataBitLength = Byte.SIZE - curDataBitIndex;
      }
      else
      {
        curDataBitLength = curSize;
      }

      int subValue = readSubValue(data, curDataByteIndex, curDataBitIndex, curDataBitLength);

      value |= (subValue << curValueBitIndex);

      curDataByteIndex--;
      curDataBitIndex = 0;
      curValueBitIndex += curDataBitLength;
      curSize -= curDataBitLength;
    }

    return value;
  }

  /**
   * Stores the data field value in the given EnOcean equipment profile (EEP) data.
   *
   * @param  value  data field value
   *
   * @param  data   EnOcean equipment profile (EEP) data
   *
   * @throws EepOutOfRangeException
   *           if the value exceeds the valid range of this data field
   */
  public void write(int value, EepData data) throws EepOutOfRangeException
  {
    checkDataParameter(data);
    checkValueParameter(value);

    int curSize = this.size;
    int curDataByteIndex = getLSBIndex();
    int curDataBitIndex = getLSBStartBitIndex();
    int curDataBitLength = 0;
    int curValueBitIndex = 0;

    while(curSize > 0)
    {
      if(curSize >= (Byte.SIZE - curDataBitIndex))
      {
        curDataBitLength = Byte.SIZE - curDataBitIndex;
      }
      else
      {
        curDataBitLength = curSize;
      }

      int mask = 0xFFFFFFFF >>> (Integer.SIZE - curDataBitLength);
      int subValue = (value >> curValueBitIndex) & mask;

      writeSubValue(subValue, data, curDataByteIndex, curDataBitIndex);

      curDataByteIndex--;
      curDataBitIndex = 0;
      curValueBitIndex += curDataBitLength;
      curSize -= curDataBitLength;
    }
  }

  /**
   * Checks if given data field value is within the valid range.
   *
   * @param value  data field value
   *
   * @return  <tt>true</tt> if valid, <tt>false</tt> otherwise
   */
  public boolean isValid(int value)
  {
    int max = maxValue();

    return (value >= 0 && value <= max);
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private int maxValue()
  {
    return (int)Math.pow(2, this.size) - 1;
  }


  private void checkValueParameter(int value) throws EepOutOfRangeException
  {
    if(!isValid(value))
    {
      throw new EepOutOfRangeException(
          "Raw value '" + value + "' is out of valid range [0.." + maxValue() + "]."
      );
    }
  }

  private void checkDataParameter(EepData data)
  {
    if(data == null)
    {
      throw new IllegalArgumentException("null data");
    }

    if(data.length() < (getLSBIndex() + 1))
    {
      throw new IllegalArgumentException(
          "'" + this + "' is not within the boundaries of '" + data + "'"
      );
    }
  }

  /**
   * Writes part of the data field value to the payload data
   *
   * @param subValue       value that is stored
   *
   * @param data           EnOcean equipment profile (EEP) data
   *
   * @param dataByteIndex  index of payload data byte
   *
   * @param bitIndex       index of start bit
   */
  private void writeSubValue(int subValue, EepData data, int dataByteIndex, int bitIndex)
  {
    int oldValue = data.getValue(dataByteIndex);
    int newValue = oldValue | (subValue << bitIndex);

    data.setValue(dataByteIndex, newValue);
  }

  /**
   * Reads part of the data field value from the payload data
   *
   * @param data           EnOcean equipment profile (EEP) data
   *
   * @param dataByteIndex  index of payload data byte
   *
   * @param bitIndex       index of start bit
   *
   * @param length         bit range length
   *
   * @return               data field sub value
   */
  private int readSubValue(EepData data, int dataByteIndex, int bitIndex, int length)
  {
    int mask = 0xFF;

    if(bitIndex > 0)
    {
      mask = (0xFF << bitIndex) & 0xFF;
    }

    if((bitIndex + length) < Byte.SIZE)
    {
      mask = mask & (0xFF >> (Byte.SIZE - (bitIndex + length)));
    }

    return (data.getValue(dataByteIndex) & mask) >> bitIndex;
  }

  /**
   * Returns the index of the first bit within the least significant byte.
   *
   * @return index of first bit within LSB
   */
  private int getLSBStartBitIndex()
  {
    return (Byte.SIZE - ((this.offset + this.size - 1) % Byte.SIZE)) - 1;
  }

  /**
   * Returns index of least significant byte.
   *
   * @return index of least significant byte
   */
  private int getLSBIndex()
  {
    return (this.offset + this.size - 1) / Byte.SIZE;
  }
}
