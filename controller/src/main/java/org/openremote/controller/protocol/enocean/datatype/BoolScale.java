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

import java.util.HashSet;
import java.util.Set;

/**
 * Scale for converting EnOcean equipment profile (EEP) data field values
 * to a boolean representation. <p>
 *
 * This class is used in combination with a {@link Bool} data type. <p>
 *
 * @see org.openremote.controller.protocol.enocean.profile.EepDataField
 * @see Bool
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class BoolScale extends CategoricalScale
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Creates a scale category set with given scale categories and returns it.
   *
   * @param  category1  first scale category
   *
   * @param  category2  second scale category
   *
   * @return scale category set
   */
  private static Set<ScaleCategory> createSet(ScaleCategory category1, ScaleCategory category2)
  {
    if(category1 == null || category2 == null)
    {
      throw new IllegalArgumentException("null scale category");
    }

    Set<ScaleCategory> set = new HashSet<ScaleCategory>(2);

    set.add(category1);
    set.add(category2);

    return set;
  }

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Scale category representing the boolean value <tt>true</tt>.
   */
  private ScaleCategory trueCategory;

  /**
   * Scale category representing the boolean value <tt>false</tt>.
   */
  private ScaleCategory falseCategory;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a boolean scale instance with given scale categories representing the
   * boolean values <tt>true</tt> and <tt>false</tt>.
   *
   * @param trueCategory  scale category representing the boolean value <tt>true</tt>
   *
   * @param falseCategory scale category representing the boolean value <tt>false</tt>
   */
  public BoolScale(ScaleCategory trueCategory, ScaleCategory falseCategory)
  {
    super(createSet(trueCategory, falseCategory));

    this.trueCategory = trueCategory;
    this.falseCategory = falseCategory;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Checks if the given scale category represents the boolean value <tt>true</tt>.
   *
   * @param  category scale category
   *
   * @return <tt>true</tt> if scale category represents the boolean value <tt>true</tt>,
   *         <tt>false</tt> otherwise
   */
  public boolean isTrue(ScaleCategory category)
  {
    return trueCategory.equals(category);
  }

  /**
   * Returns the scale category which represents the boolean value <tt>true</tt>.
   *
   * @return scale category representing the boolean value <tt>true</tt>
   */
  public ScaleCategory getTrueCategory()
  {
    return trueCategory;
  }

  /**
   * Returns the scale category which represents the boolean value <tt>false</tt>.
   *
   * @return scale category representing the boolean value <tt>false</tt>
   */
  public ScaleCategory getFalseCategory()
  {
    return falseCategory;
  }
}
