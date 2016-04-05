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

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a categorical scale which is used to convert raw EnOcean equipment profile (EEP)
 * data field values to a categorical value (in contrast to a continuous value). <p>
 *
 * EnOcean equipment profiles (EEP) are used to structure the payload field of an EnOcean
 * radio telegram. Each EnOcean equipment profile is formally specified by a table with rows
 * for each profile data field. The table contains the columns 'Valid Range' and 'Scale'
 * used to specify the conversion from raw values to a categorical value. <p>
 *
 * This class is used in combination with a {@link Ordinal} and {@link Bool} data type. <p>
 *
 * Note that the EnOcean equipment profile (EEP) specification uses the term 'Enum' for
 * specifying categorical scales.
 *
 * @see org.openremote.controller.protocol.enocean.profile.EepDataField
 * @see Ordinal
 * @see Bool
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class CategoricalScale
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Creates a scale category set with given categories.
   *
   * @param categories  scale category array
   *
   * @return scale category set
   */
  private static Set<ScaleCategory> createSet(ScaleCategory[] categories)
  {
    Set<ScaleCategory> set = new HashSet<ScaleCategory>(categories.length);

    for(ScaleCategory category : categories)
    {
      if(category != null)
      {
        set.add(category);
      }
    }

    if(set.size() == 0)
    {
      throw new IllegalArgumentException("null scale category");
    }

    return set;
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Scale categories for converting raw values to categorical values.
   */
  Set<ScaleCategory> categories;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a categorical scale instance with given scale category set.
   *
   * @param categories  scale category set for converting raw values to categorical values
   */
  public CategoricalScale(Set<ScaleCategory> categories)
  {
    if(categories == null)
    {
      throw new IllegalArgumentException("null scale categories");
    }

    this.categories = categories;
  }

  /**
   * Constructs a categorical scale instance with given scale categories.
   *
   * @param categories scale categories for converting raw values to categorical values
   */
  public CategoricalScale(ScaleCategory...categories)
  {
    this(createSet(categories));
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the scale category which contains the raw value in its raw value range.
   *
   * @param   rawValue  raw EnOcean equipment profile (EEP) data field value
   *
   * @return  the scale category
   *
   * @throws EepOutOfRangeException
   *           if no scale category can be found
   */
  public ScaleCategory scaleRawValue(int rawValue) throws EepOutOfRangeException
  {
    for(ScaleCategory category : categories)
    {
      if(category.fallsIntoCategory(rawValue))
      {
        return category;
      }
    }

    throw new  EepOutOfRangeException(
        "Raw value ''{0}'' is not within the valid scale ranges : {1}", categories
    );
  }
}
