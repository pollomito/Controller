/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.model.event;

import org.junit.Test;
import org.junit.Assert;

/**
 * TODO : See ORCJAVA-81 -- add unit tests for Switch events
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */

/**
 * Basic tests for {@link org.openremote.controller.model.event.Switch} class.  <p>
 *
 * (ORCJAVA-81 -- http://jira.openremote.org/browse/ORCJAVA-81)
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class SwitchTest
{

  /**
   * Basic constructor and validation.
   */
  @Test public void basicConstruction()
  {
    Switch s1 = new Switch(0, "Source-1", Switch.State.ON);

    Assert.assertTrue(s1.getState().equals(Switch.State.ON));
    Assert.assertTrue(s1.getValue().equals(Switch.State.ON.serialize()));
    Assert.assertTrue(s1.getSource().equals("Source-1"));
    Assert.assertTrue(s1.getSourceID() == 0);

    Assert.assertTrue(s1.serialize().equals(Switch.State.ON.serialize()));

    Assert.assertTrue(s1.toString() != null);
    Assert.assertFalse(s1.toString().equals(""));


    Switch s2 = new Switch(1, "Source-2", "foo", Switch.State.ON);

    Assert.assertTrue(s2.getState().equals(Switch.State.ON));
    Assert.assertTrue(s2.getValue().equals("foo"));
    Assert.assertTrue(s2.getSource().equals("Source-2"));
    Assert.assertTrue(s2.getSourceID() == 1);

    Assert.assertTrue(s2.serialize().equals("foo"));

    Assert.assertTrue(s2.toString() != null);
    Assert.assertFalse(s2.toString().equals(""));
  }

  @Test public void testEquals() throws Exception
  {
    Switch s1 = new Switch(1, "Source-1", Switch.State.OFF);
    Switch s2 = new Switch(1, "Source-1", Switch.State.OFF);

    Assert.assertTrue(s1.equals(s1));
    Assert.assertTrue(s1.equals(s2));
    Assert.assertTrue(s2.equals(s1));
    Assert.assertTrue(s1.hashCode() == s2.hashCode());


    Switch s3 = new Switch(2, "Source-1", Switch.State.OFF);

    Assert.assertFalse(s3.equals(s1));


    Switch s4 = new Switch(1, "Source-2", Switch.State.OFF);

    Assert.assertFalse(s4.equals(s1));


    Switch s5 = new Switch(1, "Source-1", Switch.State.ON);

    Assert.assertFalse(s5.equals(s1));


    Switch s6 = new Switch(1, "Source-1", "foo", Switch.State.OFF);
    Switch s7 = new Switch(1, "Source-1", "foo", Switch.State.OFF);

    Assert.assertTrue(s6.equals(s6));
    Assert.assertTrue(s6.equals(s7));
    Assert.assertTrue(s7.equals(s6));
    Assert.assertTrue(s6.hashCode() == s7.hashCode());


    Switch s8 = new Switch(2, "Source-1", "foo", Switch.State.OFF);

    Assert.assertFalse(s8.equals(s6));


    Switch s9 = new Switch(1, "Source-2", "foo", Switch.State.OFF);

    Assert.assertFalse(s9.equals(s6));


    Switch s10 = new Switch(1, "Source-1", "bar", Switch.State.OFF);

    Assert.assertFalse(s10.equals(s6));


    Switch s11 = new Switch(1, "Source-1", "foo", Switch.State.ON);

    Assert.assertTrue(s11.equals(s6));


    Switch s12 = new Switch(444, "Source-1", Switch.State.OFF.serialize(), Switch.State.OFF);
    Switch s13 = new Switch(444, "Source-1", Switch.State.OFF);
    Switch s14 = new Switch(444, "Source-1", "foo", Switch.State.OFF);

    Assert.assertTrue(s12.equals(s13));
    Assert.assertTrue(s12.hashCode() == s13.hashCode());
    Assert.assertFalse(s12.equals(s14));
  }
}

