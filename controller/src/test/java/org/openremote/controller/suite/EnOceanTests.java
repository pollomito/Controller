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
package org.openremote.controller.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openremote.controller.protocol.enocean.*;
import org.openremote.controller.protocol.enocean.datatype.*;
import org.openremote.controller.protocol.enocean.packet.*;
import org.openremote.controller.protocol.enocean.packet.command.*;
import org.openremote.controller.protocol.enocean.packet.radio.*;
import org.openremote.controller.protocol.enocean.port.Esp2ComPortAdapterTest;
import org.openremote.controller.protocol.enocean.port.Esp3ComPortAdapterTest;
import org.openremote.controller.protocol.enocean.profile.*;

/**
 * All EnOcean tests aggregated here.
 *
 * @author Rainer Hitz
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        EnOceanCommandBuilderTest.class,
        EnOceanGatewayTest.class,
        EnOceanConnectionManagerTest.class,
        Esp2ConnectionTest.class,
        Esp3ConnectionTest.class,
        DeviceIDTest.class,
        Esp2ComPortAdapterTest.class,
        Esp3ComPortAdapterTest.class,
        BoolScaleTest.class,
        BoolTest.class,
        CategoricalScaleTest.class,
        LinearScaleTest.class,
        OrdinalTest.class,
        RangeTest.class,
        ScaleCategoryTest.class,
        Esp2PacketHeaderTest.class,
        Esp2PacketTest.class,
        Esp3PacketCRC8Test.class,
        Esp3PacketHeaderTest.class,
        Esp3PacketTest.class,
        Esp2ProcessorTest.class,
        Esp3ProcessorTest.class,
        Esp2RPSTelegramTest.class,
        Esp21BSTelegramTest.class,
        Esp24BSTelegramTest.class,
        Esp3RadioTelegramOptDataTest.class,
        Esp3RPSTelegramTest.class,
        Esp31BSTelegramTest.class,
        Esp34BSTelegramTest.class,
        Esp2RdIDBaseCommandTest.class,
        Esp2RdIDBaseResponseTest.class,
        Esp3RdIDBaseCommandTest.class,
        Esp3RdIDBaseResponseTest.class,
        Esp3RdVersionCommandTest.class,
        Esp3RdVersionResponseTest.class,
        EepDataFieldTest.class,
        EepDataTest.class,
        EepTypeTest.class,
        EepD50001Test.class,
        EepA50201Test.class,
        EepA50202Test.class,
        EepA50203Test.class,
        EepA50204Test.class,
        EepA50205Test.class,
        EepA50206Test.class,
        EepA50207Test.class,
        EepA50208Test.class,
        EepA50209Test.class,
        EepA5020ATest.class,
        EepA5020BTest.class,
        EepA50210Test.class,
        EepA50211Test.class,
        EepA50212Test.class,
        EepA50213Test.class,
        EepA50214Test.class,
        EepA50215Test.class,
        EepA50216Test.class,
        EepA50217Test.class,
        EepA50218Test.class,
        EepA50219Test.class,
        EepA5021ATest.class,
        EepA5021BTest.class,
        EepA50220Test.class,
        EepA50230Test.class,
        EepA50401Test.class,
        EepA50601Test.class,
        EepA50602Test.class,
        EepA50701Test.class,
        EepA50801Test.class,
        EepA50802Test.class,
        EepA50803Test.class,
        EepA50904Test.class,
        EepA51001Test.class,
        EepA51002Test.class,
        EepA51003Test.class,
        EepA51004Test.class,
        EepA51005Test.class,
        EepA51006Test.class,
        EepA51007Test.class,
        EepA51008Test.class,
        EepA51009Test.class,
        EepA5100ATest.class,
        EepA5100BTest.class,
        EepA5100CTest.class,
        EepA5100DTest.class,
        EepA51010Test.class,
        EepA51011Test.class,
        EepA51012Test.class,
        EepA51013Test.class,
        EepA51014Test.class,
        EepA51015Test.class,
        EepA51016Test.class,
        EepA51017Test.class,
        EepA51018Test.class,
        EepA51019Test.class,
        EepA5101ATest.class,
        EepA5101BTest.class,
        EepA5101CTest.class,
        EepA5101DTest.class,
        EepA51200Test.class,
        EepA51201Test.class,
        EepA51202Test.class,
        EepA51203Test.class
    }
)
public class EnOceanTests
{

}

