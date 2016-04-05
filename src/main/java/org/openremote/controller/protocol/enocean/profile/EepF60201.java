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

import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.enocean.*;
import org.openremote.controller.protocol.enocean.datatype.*;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * EnOcean equipment profile (EEP) F6-02-01 implementation.
 *
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp3RPSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepF60201 implements EepTransceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Payload value which indicates that the energy bow of the switch has been pressed.
   */
  private static final int ENERGY_BOW_PRESS = 0x01;

  /**
   * Payload value which indicates that the energy bow of the switch has been released.
   */
  private static final int ENERGY_BOW_RELEASE = 0x00;

  /**
   * Payload value which indicates that the bottom left button has been activated.
   */
  private static final int ROCKER_AI = 0x00;

  /**
   * Payload value which indicates that the upper left button has been activated.
   */
  private static final int ROCKER_AO = 0x01;

  /**
   * Payload value which indicates that the bottom right button has been activated.
   */
  private static final int ROCKER_BI = 0x02;

  /**
   * Payload value which indicates that upper right button has been activated.
   */
  private static final int ROCKER_BO = 0x03;

  /**
   * Status byte value which indicates that the radio telegram is a N-Message. <p>
   *
   * The payload field of a N-Message contains information about which button has
   * been activated.
   */
  private static final int N_MESSAGE = 0x10;

  /**
   * Status byte value which indicates that the radio telegram is a U-Message. <p>
   *
   * The payload field of a U-Message contains the number of simultaneously activated
   * buttons.
   */
  private static final int U_MESSAGE = 0x00;

  /**
   * Status byte value which indicates that the message is related to a switch with
   * 2 rockers (as opposed to a switch with 4 rockers).
   */
  private static final int T21 = 0x20;


  private static final int ENERGY_BOW_DATA_FIELD_OFFSET = 0x03;
  private static final int ENERGY_BOW_DATA_FIELD_SIZE = 0x01;

  private static final int ROCKER_FIRST_ACTION_DATA_FIELD_OFFSET = 0x01;
  private static final int ROCKER_FIRST_ACTION_DATA_FIELD_SIZE = 0x02;

  private static final EepDataField ENERGY_BOW_DATA_FIELD = new EepDataField(
      "EB", ENERGY_BOW_DATA_FIELD_OFFSET, ENERGY_BOW_DATA_FIELD_SIZE);

  private static final EepDataField ROCKER_FIRST_ACTION_DATA_FIELD = new EepDataField(
      "R1", ROCKER_FIRST_ACTION_DATA_FIELD_OFFSET, ROCKER_FIRST_ACTION_DATA_FIELD_SIZE
  );

  private static final BoolScale ENERGY_BOW_SCALE = new BoolScale(
      new ScaleCategory("Energy bow pressed", ENERGY_BOW_PRESS, ENERGY_BOW_PRESS, "on", ENERGY_BOW_PRESS),
      new ScaleCategory("Energy bow released", ENERGY_BOW_RELEASE, ENERGY_BOW_RELEASE, "off", ENERGY_BOW_RELEASE)
  );

  private static final CategoricalScale ROCKER_FIRST_ACTION_SCALE = new CategoricalScale(
      new ScaleCategory("Rocker 1st action: Button AI", ROCKER_AI, ROCKER_AI, "ROCKER_AI", ROCKER_AI),
      new ScaleCategory("Rocker 1st action: Button AO", ROCKER_AO, ROCKER_AO, "ROCKER_AO", ROCKER_AO),
      new ScaleCategory("Rocker 1st action: Button BI", ROCKER_BI, ROCKER_BI, "ROCKER_BI", ROCKER_BI),
      new ScaleCategory("Rocker 1st action: Button BO", ROCKER_BO, ROCKER_BO, "ROCKER_BO", ROCKER_BO)
  );

  private static final Bool ENERGY_BOW_PRESS_VALUE = new Bool(ENERGY_BOW_DATA_FIELD, true);

  private static final Bool ENERGY_BOW_RELEASE_VALUE = new Bool(ENERGY_BOW_DATA_FIELD, false);

  private static final Ordinal ROCKER_AI_VALUE = new Ordinal(ROCKER_FIRST_ACTION_DATA_FIELD, ROCKER_AI);
  private static final Ordinal ROCKER_AO_VALUE = new Ordinal(ROCKER_FIRST_ACTION_DATA_FIELD, ROCKER_AO);
  private static final Ordinal ROCKER_BI_VALUE = new Ordinal(ROCKER_FIRST_ACTION_DATA_FIELD, ROCKER_BI);
  private static final Ordinal ROCKER_BO_VALUE = new Ordinal(ROCKER_FIRST_ACTION_DATA_FIELD, ROCKER_BO);


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Enums ----------------------------------------------------------------------------------------

  /**
   * Represents a command which can be applied to this EEP.
   */
  private enum CommandType
  {

    /**
     * Press bottom left button.
     */
    PRESS_ROCKER_AI("PRESS_ROCKER_AI", T21 | N_MESSAGE, null, ENERGY_BOW_PRESS_VALUE, ROCKER_AI_VALUE),

    /**
     * Release bottom left button.
     */
    RELEASE_ROCKER_AI("PRESS_ROCKER_AI", T21 | U_MESSAGE, null, ENERGY_BOW_RELEASE_VALUE),

    /**
     * Press upper left button.
     */
    PRESS_ROCKER_AO("PRESS_ROCKER_AO", T21 | N_MESSAGE, null, ENERGY_BOW_PRESS_VALUE, ROCKER_AO_VALUE),

    /**
     * Release upper left button.
     */
    RELEASE_ROCKER_AO("PRESS_ROCKER_AO", T21 | U_MESSAGE, null, ENERGY_BOW_RELEASE_VALUE),

    /**
     * Press and release bottom left button.
     */
    ON_ROCKER_A("ON_ROCKER_A", T21 | N_MESSAGE, RELEASE_ROCKER_AI, ENERGY_BOW_PRESS_VALUE, ROCKER_AI_VALUE),

    /**
     * Press and release bottom left button.
     */
    ON("ON", T21 | N_MESSAGE, RELEASE_ROCKER_AI, ENERGY_BOW_PRESS_VALUE, ROCKER_AI_VALUE),

    /**
     * Press and release upper left button.
     */
    OFF_ROCKER_A("OFF_ROCKER_A",  T21 | N_MESSAGE, RELEASE_ROCKER_AO, ENERGY_BOW_PRESS_VALUE, ROCKER_AO_VALUE),

    /**
     * Press and release upper left button.
     */
    OFF("OFF",  T21 | N_MESSAGE, RELEASE_ROCKER_AO, ENERGY_BOW_PRESS_VALUE, ROCKER_AO_VALUE),

    /**
     * Read ON/OFF status of left rocker.
     */
    STATUS_ROCKER_A("STATUS_ROCKER_A", 0x00, null, ENERGY_BOW_PRESS_VALUE),

    /**
     * Press bottom right button.
     */
    PRESS_ROCKER_BI("PRESS_ROCKER_BI", T21 | N_MESSAGE, null, ENERGY_BOW_PRESS_VALUE, ROCKER_BI_VALUE),

    /**
     * Release bottom right button.
     */
    RELEASE_ROCKER_BI("PRESS_ROCKER_BI", T21 | U_MESSAGE, null, ENERGY_BOW_RELEASE_VALUE),

    /**
     * Press upper right button.
     */
    PRESS_ROCKER_BO("PRESS_ROCKER_BO", T21 | N_MESSAGE, null, ENERGY_BOW_PRESS_VALUE, ROCKER_BO_VALUE),

    /**
     * Release upper right button.
     */
    RELEASE_ROCKER_BO("PRESS_ROCKER_BO", T21 | U_MESSAGE, null, ENERGY_BOW_RELEASE_VALUE),

    /**
     * Press and release bottom right button.
     */
    ON_ROCKER_B("ON_ROCKER_B", T21 | N_MESSAGE, RELEASE_ROCKER_BI, ENERGY_BOW_PRESS_VALUE, ROCKER_BI_VALUE),

    /**
     * Press and release upper right button.
     */
    OFF_ROCKER_B("OFF_ROCKER_B", T21 | N_MESSAGE, RELEASE_ROCKER_BO, ENERGY_BOW_PRESS_VALUE, ROCKER_BO_VALUE),

    /**
     * Read ON/OFF status of right rocker.
     */
    STATUS_ROCKER_B("STATUS_ROCKER_B", 0x00, null, ENERGY_BOW_PRESS_VALUE);


    // Members ------------------------------------------------------------------------------------

    public static CommandType resolve(String commandString) throws ConfigurationException
    {
      commandString = commandString.toUpperCase().trim();

      CommandType[] allTypes = CommandType.values();

      for (CommandType commandType : allTypes)
      {
        if (commandType.commandString.equals(commandString))
        {
          return commandType;
        }
      }

      throw new ConfigurationException(
          "Command ''{0}'' is an invalid EEP ''F6-02-01'' command.", commandString
      );
    }

    private String commandString;
    private byte[] payload;
    private byte statusByte;
    private CommandType releaseCommand;

    private CommandType(String commandString, int statusByte, CommandType releaseCommand, EepDataListener...listenerArray)
    {
      this.commandString = commandString;
      this.statusByte = (byte)statusByte;
      this.releaseCommand = releaseCommand;

      Set<EepDataListener> listenerSet = new HashSet<EepDataListener>();
      for(EepDataListener listener : listenerArray)
      {
        listenerSet.add(listener);
      }

      EepData eepData = new EepData(EepType.EEP_TYPE_F60201, 1, listenerSet);

      try
      {
        this.payload = eepData.asByteArray();
      }
      catch (EepOutOfRangeException e)
      {
        throw new RuntimeException("Failed to create command: " + e.getMessage(), e);
      }
    }

    public byte[] getPayload()
    {
      return payload;
    }

    private byte getStatusByte()
    {
      return statusByte;
    }

    private CommandType getReleaseCommand()
    {
      return releaseCommand;
    }
  }


  // Private Instance Fields --------------------------------------------------------------------

  private Ordinal rockerStatus = new Ordinal(ROCKER_FIRST_ACTION_DATA_FIELD, ROCKER_FIRST_ACTION_SCALE);
  private Bool energyBowStatus = new Bool(ENERGY_BOW_DATA_FIELD, ENERGY_BOW_SCALE);


  private boolean statusRockerA = false;
  private boolean statusRockerB = false;

  /**
   * EnOcean equipment profile (EEP) type.
   */
  private EepType eepType;

  /**
   * The command from the command configuration.
   */
  private CommandType command;

  /**
   * The device ID used as a radio telegram sender ID or as a filter for receiving radio
   * telegrams depending on the {@link #command}.
   */
  private DeviceID deviceID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean equipment profile (EEP) F6-02-01 instance with given EEP type,
   * device ID and command.
   *
   * @param  eepType   the EnOcean equipment profile (EEP) type
   *
   * @param  deviceID  EnOcean device ID
   *
   * @param  command   command string
   *
   * @throws ConfigurationException
   *           if the command is unknown
   */
  public EepF60201(EepType eepType, DeviceID deviceID, String command) throws ConfigurationException
  {
    this.eepType = eepType;
    this.deviceID = deviceID;
    this.command = CommandType.resolve(command);
  }


  // Object Overrides -----------------------------------------------------------------------------


  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    String status;

    if(command == CommandType.STATUS_ROCKER_A)
    {
      return "EEP (Type = '" + eepType + "', Rocker A = '" + (statusRockerA ? "On" : "Off") + "')";
    }

    else if(command == CommandType.STATUS_ROCKER_B)
    {
      return "EEP (Type = '" + eepType + "', Rocker B = '" + (statusRockerB ? "On" : "Off") + "')";
    }

    else
    {
      return "EEP (Type = '" + eepType + "', Command = '" + command + "')";
    }
  }


  // Implements Eep -------------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public EepType getType()
  {
    return eepType;
  }


  // Implements EepTransceive ---------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void send(RadioInterface radioInterface) throws ConfigurationException, ConnectionException
  {
    if(command == CommandType.STATUS_ROCKER_A ||
       command == CommandType.STATUS_ROCKER_B)
    {
      // TODO : log
      return;
    }

    CommandType releaseCommand = command.releaseCommand;


    radioInterface.sendRadio(
        eepType.getRORG(), deviceID, command.getPayload(), command.getStatusByte()
    );

    if(releaseCommand != null)
    {
      radioInterface.sendRadio(
          eepType.getRORG(), deviceID, releaseCommand.getPayload(), releaseCommand.getStatusByte()
      );
    }
  }


  @Override public boolean update(EspRadioTelegram telegram)
  {

    if(!deviceID.equals(telegram.getSenderID()))
    {
      return false;
    }

    if(!eepType.isValidRadioTelegramRORG(telegram))
    {
      log.warn(
          "Discarded received radio telegram from device " +
          "with ID {0} because of a configuration error: " +
          "Command for device with ID {0} has been configured " +
          "with an invalid EEP {1} for this device.",
          deviceID, getType()
      );

      return false;
    }

    boolean oldStatusRockerA = statusRockerA;
    boolean oldStatusRockerB = statusRockerB;

    updateEnergyBowStatus(telegram.getPayload());

    if(isEnergyBowPressed())
    {
      updateRockerStatus(telegram.getPayload());

      if(rockerStatus.ordinalValue() == ROCKER_AI)
      {
        statusRockerA = true;
      }

      else if(rockerStatus.ordinalValue() == ROCKER_AO)
      {
        statusRockerA = false;
      }

      else if(rockerStatus.ordinalValue() == ROCKER_BI)
      {
        statusRockerB = true;
      }

      else if(rockerStatus.ordinalValue() == ROCKER_BO)
      {
        statusRockerB = false;
      }
    }

    return (statusRockerA != oldStatusRockerA) ||
           (statusRockerB != oldStatusRockerB);
  }

  @Override public void updateSensor(Sensor sensor) throws ConfigurationException
  {
    if(sensor instanceof SwitchSensor)
    {
      sensor.update(getStateValue());
    }

    else
    {
      this.rockerStatus.updateSensor(sensor);
    }
  }

  public boolean getBoolValue() throws ConfigurationException
  {
    if(command == CommandType.STATUS_ROCKER_A)
    {
      return statusRockerA;
    }

    else if(command == CommandType.STATUS_ROCKER_B)
    {
      return statusRockerB;
    }

    else
    {
      // TODO
      throw new ConfigurationException("");
    }
  }

  public String getStateValue() throws ConfigurationException
  {
    return getBoolValue() == true ? "on" : "off";
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private void updateEnergyBowStatus(byte[] data)
  {
    EepData eepData = new EepData(EepType.EEP_TYPE_F60201, 1, this.energyBowStatus);
    eepData.update(data);
  }

  private void updateRockerStatus(byte[] data)
  {
    EepData eepData = new EepData(EepType.EEP_TYPE_F60201, 1, this.rockerStatus);
    eepData.update(data);
  }

  private boolean isEnergyBowPressed()
  {
    return this.energyBowStatus.boolValue();
  }
}
