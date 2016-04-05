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

import org.junit.Test;
import org.junit.Assert;

import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;

/**
 * Unit tests for {@link EepType} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepTypeTest
{

  // Tests ----------------------------------------------------------------------------------------


  @Test public void testLookup() throws Exception
  {
    EepType eep = EepType.lookup("F6-02-01");

    Assert.assertNotNull(eep);
    Assert.assertEquals(EspRadioTelegram.RORG.RPS, eep.getRORG());
    Assert.assertEquals(0x02, eep.getFunc());
    Assert.assertEquals(0x01, eep.getType());
  }


  // Helpers --------------------------------------------------------------------------------------

  private byte getInvalidRORGValue()
  {
    EspRadioTelegram.RORG rorg = null;
    int rorgValue = 0;

    do
    {
      rorgValue++;
      try
      {
        rorg = EspRadioTelegram.RORG.resolve(rorgValue);
      }
      catch (EspRadioTelegram.UnknownRorgException e)
      {

      }

    } while(rorg != null);

    return (byte)rorgValue;
  }
}
