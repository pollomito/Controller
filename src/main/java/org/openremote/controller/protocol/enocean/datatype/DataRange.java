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

import java.math.BigDecimal;

/**
 * EnOcean equipment profile (EEP) data range. <p>
 *
 * EnOcean equipment profiles (EEP) are used to structure the payload field of an EnOcean
 * radio telegram. Each EnOcean equipment profile is formally specified by a table with rows
 * for each profile data field. The table contains the data range columns 'Valid Range' and
 * 'Scale' which are used to specify the conversion from raw values to engineering units. <p>
 *
 * Note that an EnOcean equipment profile (EEP) raw value data range may start with the high
 * value (e.g. [255..0]).
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class DataRange
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Start of the data range.
   */
  BigDecimal start;

  /**
   * End of the data range.
   */
  BigDecimal end;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a data range instance with given data range boundaries.
   *
   * @param start  start of the data range
   * @param end    end of the data range
   */
  public DataRange(double start, double end)
  {
    this.start = BigDecimal.valueOf(start);
    this.end = BigDecimal.valueOf(end);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the start of the data range.
   *
   * @return  start of the data range
   */
  public BigDecimal getStart()
  {
    return start;
  }

  /**
   * Returns the end of the data range.
   *
   * @return  end of the data range
   */
  public BigDecimal getEnd()
  {
    return end;
  }
}
