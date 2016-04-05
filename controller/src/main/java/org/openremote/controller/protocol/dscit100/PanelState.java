/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
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
package org.openremote.controller.protocol.dscit100;

import java.util.HashMap;
import java.util.Map;

import org.openremote.controller.utils.Logger;


/**
 * Class to store the security panel's current state in memory.
 *
 * @author Greg Rapp
 * @author Phil Taylor
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
class PanelState
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * DSC logger. Uses a common category for all DSC related logging.
   */
  private final static Logger log = Logger.getLogger(DSCIT100CommandBuilder.DSC_LOG_CATEGORY);


  // Enums ----------------------------------------------------------------------------------------

  enum StateType
  {
    ZONE, ZONE_ALARM, PARTITION, LABEL, LED
  }

  enum ZoneState implements State
  {
    OPEN, RESTORED, FAULT
  }

  enum AlarmState implements State
  {
    NORMAL, ALARM
  }

  enum PartitionState implements State
  {
    READY, NOTREADY, AWAYARMED, STAYARMED,
    AWAYARMEDND /* ND = NO DELAY */, STAYARMEDND /* ND = NO DELAY */,
    ALARM, DISARMED, EXITDELAY, ENTRYDELAY, FAILTOARM, BUSY
  }

  /**
   * A security system label (i.e. Back Door, Basement Motion Detector)
   */
  enum LedState implements State
  {
    OFF, ON, FLASH
  }


  // Instance Fields ------------------------------------------------------------------------------


  /**
   * Map containing Maps of State objects. Outside key is the state
   * type, inside key is state target (zone number, partition number)
   */
  private Map<StateType, Map<String, State>> internalState;


  // Constructors ---------------------------------------------------------------------------------

  PanelState()
  {
    internalState = new HashMap<StateType, Map<String, State>>();
  }

  // Protected Instance Methods -------------------------------------------------------------------

  /**
   * Called by a connection listener to process incoming data
   * 
   * @param packet
   *          Incoming <code>Packet</code>
   */
  protected synchronized void processPacket(Packet packet)
  {
    if (packet.getCommand().equals("510"))
    {
      // Led Update
      int leds = Integer.parseInt(packet.getData().substring(0, 2),16);
      int mask = 1 << 7;

      for (int c = 1;c <= 8; c++)
      {
        log.debug("LED Status update [LED"+String.valueOf(c)+"="+((leds&mask)==0?"OFF":"ON")+"]");

        updateInternalState(
            StateType.LED, String.valueOf(c), (leds & mask) == 0 ? LedState.OFF : LedState.ON
        );

        leds <<= 1;
      }
    }

    else if (packet.getCommand().equals("511"))
    {
      // Led Update
      int leds = Integer.parseInt(packet.getData().substring(0, 2),16);
      int mask = 1 << 7;

      for (int c = 8;c > 0; c--)
      {
        log.debug("LED Status update [LED"+String.valueOf(c)+"="+((leds&mask)==0?"OFF":"FLASH")+"]");

        updateInternalState(
            StateType.LED, String.valueOf(c), (leds & mask) == 0 ? LedState.OFF : LedState.FLASH
        );

        leds <<= 1;
      }
    }

    else if (packet.getCommand().equals("570"))
    {
      // Broadcast Labels
      String num = packet.getData().substring(0, 3);
      num = trimLeadingZeros(num);

      String label = packet.getData().substring(3).trim();

      log.debug("Broadcast label [number=" + num + ",label=" + label + "]");

      updateInternalState(StateType.LABEL, num, new Label(label));
    }

    else if (packet.getCommand().equals("601"))
    {
      // Zone Alarm
      String partition = packet.getData().substring(0, 1);
      String zone = packet.getData().substring(1, 4);

      zone = trimLeadingZeros(zone);

      log.debug("Zone alarm [partition=" + partition + ",zone=" + zone + "]");

      updateInternalState(StateType.ZONE_ALARM, zone, AlarmState.ALARM);
    }

    else if (packet.getCommand().equals("602"))
    {
      // Zone Alarm Restore
      String partition = packet.getData().substring(0, 1);
      String zone = packet.getData().substring(1, 4);

      zone = trimLeadingZeros(zone);

      log.debug("Zone alarm restore [partition=" + partition + ",zone=" + zone + "]");

      updateInternalState(StateType.ZONE_ALARM, zone, AlarmState.NORMAL);
    }

    else if (packet.getCommand().equals("609"))
    {
      // Zone open
      String zone = packet.getData().substring(0, 3);
      zone = trimLeadingZeros(zone);

      log.debug("Zone open [zone=" + zone + "]");

      updateInternalState(StateType.ZONE, zone, ZoneState.OPEN);
    }

    else if (packet.getCommand().equals("610"))
    {
      // Zone restored
      String zone = packet.getData().substring(0, 3);
      zone = trimLeadingZeros(zone);

      log.debug("Zone restored [zone=" + zone + "]");

      updateInternalState(StateType.ZONE, zone, ZoneState.RESTORED);
    }

    else if (packet.getCommand().equals("650"))
    {
      // Partition ready
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition ready [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.READY);
    }

    else if (packet.getCommand().equals("651"))
    {
      // Partition not ready
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition not ready [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.NOTREADY);
    }

    else if (packet.getCommand().equals("652"))
    {
      // Partition armed
      String partition = packet.getData().substring(0, 1);
      String mode = packet.getData().substring(1, 2);

      log.debug("Partition armed [partition=" + partition + ",mode=" + mode + "]");

      if (mode.equals("0"))
      {
        updateInternalState(StateType.PARTITION, partition, PartitionState.AWAYARMED);
      }

      else if (mode.equals("1"))
      {
        updateInternalState(StateType.PARTITION, partition, PartitionState.STAYARMED);
      }

      else if (mode.equals("2"))
      {
        updateInternalState(StateType.PARTITION, partition, PartitionState.AWAYARMEDND);
      }

      else if (mode.equals("3"))
      {
        updateInternalState(StateType.PARTITION, partition, PartitionState.STAYARMEDND);
      }
    }

    else if (packet.getCommand().equals("654"))
    {
      // Partition alarm
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition in alarm [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.ALARM);
    }

    else if (packet.getCommand().equals("655"))
    {
      // Partition alarm
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition disarmed [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.DISARMED);
    }

    else if (packet.getCommand().equals("656"))
    {
      // Partition exit delay
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition in exit delay [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.EXITDELAY);
    }

    else if (packet.getCommand().equals("657"))
    {
      // Partition entry delay
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition in entry delay [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.ENTRYDELAY);
    }

    else if (packet.getCommand().equals("672"))
    {
      // Partition failed to arm
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition failed to arm [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.FAILTOARM);
    }

    else if (packet.getCommand().equals("673"))
    {
      // Partition busy
      String partition = packet.getData().substring(0, 1);

      log.debug("Partition busy [partition=" + partition + "]");

      updateInternalState(StateType.PARTITION, partition, PartitionState.BUSY);
    }

    else if (packet.getCommand().equals("903"))
    {
      // Annoyingly the IT-100 uses this instead of 510/511 for led status but never mind!

      String led = packet.getData().substring(0,1);
      int led_state=Integer.parseInt(packet.getData().substring(1,2));

      log.debug("LED Status update [LED"+led+"="+String.valueOf(led_state)+"]");

      if (led_state==0)
      {
        updateInternalState(StateType.LED, led, LedState.OFF);
      }

      else
      {
        updateInternalState(
            StateType.LED, led, (led_state) == 1 ? LedState.ON : LedState.FLASH
        );
      }
    }
  }


  protected synchronized State getState(StateDefinition stateDefinition)
  {
    if (!this.internalState.containsKey(stateDefinition.getType()))
    {
      log.warn("Cannot find state type information for " + stateDefinition);
      return null;
    }

    Map<String, State> members = this.internalState.get(stateDefinition.getType());

    if (members == null || (!members.containsKey(stateDefinition.getTarget())))
    {
      log.warn("Cannot find state information for " + stateDefinition);
      return null;
    }

    return members.get(stateDefinition.getTarget());
  }

  protected synchronized ZoneState getZoneState(Integer zone)
  {
    if (!this.internalState.containsKey(StateType.ZONE))
      return null;

    Map<String, State> zones = this.internalState.get(StateType.ZONE);

    if (zones == null || (!zones.containsKey(String.valueOf(zone))))
      return null;

    return (ZoneState) zones.get(String.valueOf(zone));
  }

  protected synchronized AlarmState getZoneAlarmState(Integer zone)
  {
    if (!this.internalState.containsKey(StateType.ZONE_ALARM))
      return null;

    Map<String, State> zones = this.internalState.get(StateType.ZONE_ALARM);

    if (zones == null || (!zones.containsKey(String.valueOf(zone))))
      return null;

    return (AlarmState) zones.get(String.valueOf(zone));
  }

  protected synchronized PartitionState getPartitionState(Integer partition)
  {
    if (!this.internalState.containsKey(StateType.PARTITION))
      return null;

    Map<String, State> partitions = this.internalState.get(StateType.PARTITION);

    if (partitions == null
        || (!partitions.containsKey(String.valueOf(partition))))
      return null;

    return (PartitionState) partitions.get(String.valueOf(partition));
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Update the internal state map
   *
   * @param type
   *          State type
   * @param target
   *          Zone or partition to which this <code>State</code> applies
   * @param state
   *          Target state
   */
  private void updateInternalState(StateType type, String target, State state)
  {
    Map<String, State> tmp;

    if (internalState.containsKey(type) && internalState.get(type) != null)
    {
      tmp = internalState.get(type);
      tmp.put(target, state);
    }

    else
    {
      tmp = new HashMap<String, State>();
      tmp.put(target, state);
    }

    internalState.put(type, tmp);
  }

  /**
   * Remove leading zeros from passed in string
   */
  private String trimLeadingZeros(String str)
  {
    return str.replaceFirst("^0+(?!$)", "");
  }


  // Inner classes --------------------------------------------------------------------------------

  public interface State
  {
  }


  public class Label implements State
  {
    private String label;

    public Label(String label)
    {
      this.label = label;
    }

    public String toString()
    {
      return label;
    }
  }

}
