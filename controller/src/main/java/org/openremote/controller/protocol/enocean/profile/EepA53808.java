/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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

import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.exception.ConversionException;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.enocean.*;
import org.openremote.controller.protocol.enocean.datatype.*;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Represents the EnOcean equipment profile (EEP) 'A5-38-08'. <p>
 *
 *  Communication between gateway and actuator uses byte DB_3 to identify commands. Commands
 *  0x01 to 0x7F shall be common to all types belonging to this profile. Commands 0x80 to 0xFE
 *  can be defined individually for each device type.
 *
 * <pre>
 *
 *     +------+------+-----------------------------------------+
 *     | RORG |  A5  |             4BS Telegram                |
 *     +------+------+-----------------------------------------+
 *     | FUNC |  38  |           Central Command               |
 *     +------+------+-----------------------------------------+
 *     | TYPE |  08  |               Gateway                   |
 *     +------+------+-----------------------------------------+
 *
 * </pre>
 *
 * The 'A5-38-08' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.5 specification defines the profile
 * structure as follows:
 *
 * <h2>Command 0x02 Dimming</h2>
 *
 * <pre>
 *                                                                      |Dimming Range
 *                                                                      |EDIM R
 *                                                                      | |Store Final Value
 *                                                                      | |STR
 *                                                             Learn Bit| | |Switch Command ON/OFF
 *                                                                  LRNB| | |SW
 *                                                                    | | | | |
 *            +-------------------------------------------------------+ + + + +
 *            |    Command    | Dimming Value |  Ramping Time |       | | | | |
 *            |      COM      |     EDIM      |       RMP     |       | | | | |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     bits   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *            +---------------------------------------------------------------+
 *     offset |0              |8              |16             |24             |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     byte   |      DB3      |      DB2      |      DB1      |      DB0      |
 *            +-------------------------------+-------------------------------+
 *
 *
 *     +-------------------------------------------------------------------------------------------------+
 *     |Offset|Size|  Bitrange  |    Data      |ShortCut|   Description      |Valid Range| Scale    |Unit|
 *     +-------------------------------------------------------------------------------------------------+
 *     |0     |8   |DB3.7..DB3.0|Command       |  COM   |                    |Enum:                      |
 *     |      |    |            |              |        |                    |---------------------------|
 *     |      |    |            |              |        |                    |0x02 (Dimming)             |
 *     +-------------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Dimming value |  EDIM  |Dimming value       |   0..255  | 0..100   |%   |
 *     |      |    |            |              |        |(absolute[0...255]or|           |          |    |
 *     |      |    |            |              |        | relative[0...100]  |           |          |    |
 *     +-------------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Ramping time  |  RMP   |Ramping time in     |   0..255  | 0..255   |s   |
 *     |      |    |            |              |        |seconds,            |           |          |    |
 *     |      |    |            |              |        |0 = no  ramping,    |           |          |    |
 *     |      |    |            |              |        |1...255 = seconds   |           |          |    |
 *     |      |    |            |              |        |to 100 %            |           |          |    |
 *     +-------------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit     |  LRNB  |Learn bit           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Teach-in telegram       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Data telegram           |
 *     +-------------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2       |Dimming Range | EDIM R |Dimming Range       |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Absolute value          |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Relative value          |
 *     +-------------------------------------------------------------------------------------------------+
 *     |30    |1   |DB0.1       |Store final   |  STR   |Store final value   |Enum:                      |
 *     |      |    |            |value         |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: No                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Yes                     |
 *     +-------------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Swichting     |  SW    |                    |Enum:                      |
 *     |      |    |            |Command       |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Off                     |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: On                      |
 *     +-------------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA53808 implements EepTransceive
{

  // TODO : introduce Eltako subclass and as a result remove special cases from this class
  // TODO : think about a new command parameter (device type) in order to be able to
  //        handle special cases for certain devices like the Eltako ones.

  // Constants ------------------------------------------------------------------------------------

  /**
   * Regular expression pattern that is used to extract the static dim value from a
   * dim command (e.g. DIM 25).
   */
  static final Pattern DIM_VALUE_REGEX = Pattern.compile("DIM\\s*(\\d+)");

  /**
   * Regular expression pattern that is used to extract the static dim value from a
   * dim command (e.g. DIM 25) which is used to send dim values to a Eltako device.
   */
  static final Pattern DIM_VALUE_REGEX_ELTAKO = Pattern.compile("DIM_ELTAKO\\s*(\\d+)");

  /**
   * EnOcean equipment profile (EEP) command ID data field name.
   */
  static final String EEP_A53808_COM_DATA_FIELD_NAME = "COM";

  /**
   * Start bit of command ID field.
   */
  static final int EEP_A53808_COM_OFFSET = 0;

  /**
   * Bit size of command ID data field.
   */
  static final int EEP_A53808_COM_SIZE = 8;

  /**
   * Name of 'Central Command' with the value {@link #EEP_A53808_COM_DIM_COMMAND} which is
   * used to dim lights.
   */
  static final String EEP_A53808_COM_DIM_COMMAND_Name = "Dimming";

  /**
   * Begin of a value range in order to extract the {@link #EEP_A53808_COM_DIM_COMMAND} value
   * from a received command.
   */
  static final int EEP_A53808_COM_DIM_RAW_VALUE_RANGE_MIN = 2;

  /**
   * End of a value range in order to extract the {@link #EEP_A53808_COM_DIM_COMMAND} value
   * from a received command.
   */
  static final int EEP_A53808_COM_DIM_RAW_VALUE_RANGE_MAX = 2;

  /**
   * 'Central Command' value for commands used to dim the lights.
   */
  static final int EEP_A53808_COM_DIM_COMMAND = 2;

  /**
   * EnOcean equipment profile (EEP) dim value field name.
   */
  static final String EEP_A53808_EDIM_DATA_FIELD_NAME = "EDIM";

  /**
   * Start bit of dim value data field.
   */
  static final int EEP_A53808_EDIM_OFFSET = 8;

  /**
   * Bit size of dim value data field.
   */
  static final int EEP_A53808_EDIM_SIZE = 8;

  /**
   * Begin of absolute raw dim value data range.
   */
  static final int EEP_A53808_EDIM_ABSOLUTE_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of absolute raw dim value data range.
   */
  static final int EEP_A53808_EDIM_ABSOLUTE_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of relative raw dim value data range.
   */
  static final int EEP_A53808_EDIM_RELATIVE_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of absolute raw dim value data range.
   */
  static final int EEP_A53808_EDIM_RELATIVE_RAW_DATA_RANGE_MAX = 100;

  /**
   * Begin of scaled dim value range.
   */
  static final int EEP_A53808_EDIM_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled dim value range.
   */
  static final int EEP_A53808_EDIM_UNITS_DATA_RANGE_MAX = 100;

  /**
   * Number of fractional digits to be used for dim values.
   */
  public static final int EEP_A53808_EDIM_FRACTIONAL_DIGITS = 0;

  /**
   * EnOcean equipment profile (EEP) dim speed field name.
   */
  static final String EEP_A53808_RMP_DATA_FIELD_NAME = "RMP";

  /**
   * Start bit of dim speed data field.
   */
  static final int EEP_A53808_RMP_OFFSET = 16;

  /**
   * Bit size of dim speed data field.
   */
  static final int EEP_A53808_RMP_SIZE = 8;

  /**
   * Begin of absolute raw dim speed data range.
   */
  static final int EEP_A53808_RMP_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of absolute raw dim speed data range.
   */
  static final int EEP_A53808_RMP_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled dim speed value range.
   */
  static final int EEP_A53808_RMP_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled dim speed value range.
   */
  static final int EEP_A53808_RMP_UNITS_DATA_RANGE_MAX = 255;

  /**
   * Number of fractional digits to be used for dim speed values.
   */
  static final int EEP_A53808_RMP_FRACTIONAL_DIGITS = 0;

  /**
   * Start bit of dim range data field.
   */
  static final int EEP_A53808_EDIMR_OFFSET = 29;

  /**
   * Bit size of dim range data field.
   */
  static final int EEP_A53808_EDIMR_SIZE = 1;

  /**
   * EnOcean equipment profile (EEP) dim range field name.
   */
  static final String EEP_A53808_EDIMR_DATA_FIELD_NAME = "EDIMR";

  /**
   * Description for the relative dim range value indicated by the
   * {@link #EEP_A53808_EDIMR_RELATIVE_VALUE} value.
   */
  static final String EEP_A53808_EDIMR_RELATIVE_DESC = "Relative dim range";

  /**
   * EnOcean equipment profile (EEP) dim range data field value
   * which indicates that the radio telegram contains a relative dim value.
   */
  static final int EEP_A53808_EDIMR_RELATIVE_VALUE = 1;

  /**
   * Description for the absolute dim range value indicated by the
   * {@link #EEP_A53808_EDIMR_ABSOLUTE_VALUE} value.
   */
  static final String EEP_A53808_EDIMR_ABSOLUTE_DESC = "Absolute dim range";

  /**
   * EnOcean equipment profile (EEP) dim range data field value
   * which indicates that the radio telegram contains an absolute dim value.
   */
  static final int EEP_A53808_EDIMR_ABSOLUTE_VALUE = 0;

  /**
   * Start bit of store dim value data field.
   */
  static final int EEP_A53808_STR_OFFSET = 30;

  /**
   * Bit size of store dim value data field.
   */
  static final int EEP_A53808_STR_SIZE = 1;

  /**
   * EnOcean equipment profile (EEP) store dim value field name.
   */
  static final String EEP_A53808_STR_DATA_FIELD_NAME = "STR";

  /**
   * Description for the store dim value indicated by the
   * {@link #EEP_A53808_STR_STORE_VALUE} value.
   */
  static final String EEP_A53808_STR_STORE_DESC = "Store dim value";

  /**
   * EnOcean equipment profile (EEP) store dim value data field value
   * which indicates that the dim value should be stored.
   */
  static final int EEP_A53808_STR_STORE_VALUE = 1;

  /**
   * Description for the do not store dim value indicated by the
   * {@link #EEP_A53808_STR_DO_NOT_STORE_VALUE} value.
   */
  static final String EEP_A53808_STR_DO_NOT_STORE_DESC = "Do not store dim value";

  /**
   * EnOcean equipment profile (EEP) store dim value data field value
   * which indicates that the dim value should not be stored.
   */
  static final int EEP_A53808_STR_DO_NOT_STORE_VALUE = 0;

  /**
   * Start bit of the switch data field.
   */
  static final int EEP_A53808_SW_OFFSET = 31;

  /**
   * Bit size of the switch data field.
   */
  static final int EEP_A53808_SW_SIZE = 1;

  /**
   * EnOcean equipment profile (EEP) switch field name.
   */
  static final String EEP_A53808_SW_DATA_FIELD_NAME = "SW";

  /**
   * Description for the switch value indicated by the
   * {@link #EEP_A53808_SW_ON_VALUE} value.
   */
  static final String EEP_A53808_SW_ON_DESC = "Switch on";

  /**
   * EnOcean equipment profile (EEP) switch data field value
   * which indicates that the switch is turned on.
   */
  static final int EEP_A53808_SW_ON_VALUE = 1;

  /**
   * Description for the switch value indicated by the
   * {@link #EEP_A53808_SW_OFF_VALUE} value.
   */
  static final String EEP_A53808_SW_OFF_DESC = "Switch off";

  /**
   * EnOcean equipment profile (EEP) switch data field value
   * which indicates that the switch is turned off.
   */
  static final int EEP_A53808_SW_OFF_VALUE = 0;

  /**
   * Command for configuring a command which is used to receive dim values. The name is
   * based on the data field name in the EEP specification.
   */
  static final String DIM_STATUS_COMMAND = EEP_A53808_EDIM_DATA_FIELD_NAME;

  /**
   * Command for configuring a command which is used to receive dim values. The name is
   * based on the standard naming convention used by other protocol implementation.
   */
  static final String DIM_STATUS_COMMAND_STANDARD = "STATUS";

  /**
   * Command string for configuring a command which is used to receive dim values from
   * Eltako devices like the FUD14 or FUD61.
   */
  static final String DIM_STATUS_COMMAND_ELTAKO = "STATUS_ELTAKO";

  /**
   * Command string for configuring a command which is used to send the dim value to a
   * dimmer device.
   */
  static final String DIM_COMMAND = "DIM";

  /**
   * Command string for configuring a command which is used to send the dim value to a
   * Eltako dimmer device like the FUD14 or FUD61.
   */
  static final String DIM_COMMAND_ELTAKO = "DIM_ELTAKO";

  /**
   * Command string for configuring a command which receives the dim value.
   */
  public static final String DIM_SPEED_STATUS_COMMAND = EEP_A53808_RMP_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the dim range flag.
   */
  public static final String DIM_RANGE_STATUS_COMMAND = EEP_A53808_EDIMR_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the dim value store flag.
   */
  public static final String DIM_VALUE_STORE_STATUS_COMMAND = EEP_A53808_STR_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the switch status.
   */
  public static final String SWITCH_STATUS_COMMAND = EEP_A53808_SW_DATA_FIELD_NAME;

  /**
   * Indicates that the telegram contains EEP data as opposed to a teach-in telegram.
   */
  static final int EEP_DATA_TELEGRAM = 0x01;

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Enums ----------------------------------------------------------------------------------------

  private enum Command
  {

    /**
     * Send dim value.
     */
    DIM(DIM_COMMAND),

    /**
     * Send dim value to Eltako device.
     */
    DIM_ELTAKO(DIM_COMMAND_ELTAKO),

    /**
     * Receive dimmer status value.
     */
    DIM_STATUS(DIM_STATUS_COMMAND),

    /**
     * Receive dimmer status value with special handling for Eltako dimmers like FUD14 and FUD61.
     */
    DIM_STATUS_ELTAKO(DIM_STATUS_COMMAND_ELTAKO),

    /**
     * Receive dim speed value.
     */
    DIM_SPEED_STATUS(DIM_SPEED_STATUS_COMMAND),

    /**
     * Receive dim range status.
     */
    DIM_RANGE_STATUS(DIM_RANGE_STATUS_COMMAND),

    /**
     * Receive store dim value status.
     */
    DIM_VALUE_STORE_STATUS(DIM_VALUE_STORE_STATUS_COMMAND),

    /**
     * Receive switch status.
     */
    SWITCH_STATUS(SWITCH_STATUS_COMMAND);


    // Members ------------------------------------------------------------------------------------

    public static Command toCommand(String value, EepType eepType) throws ConfigurationException
    {

      value = value.toUpperCase().trim();

      if(value.equals(DIM_STATUS.toString()) ||
         value.equals(DIM_STATUS_COMMAND_STANDARD))
      {
        return DIM_STATUS;
      }

      if(value.equals(DIM_STATUS_ELTAKO.toString()))
      {
        return DIM_STATUS_ELTAKO;
      }

      else if(value.equals(DIM_SPEED_STATUS.toString()))
      {
        return DIM_SPEED_STATUS;
      }

      else if(value.equals(DIM_RANGE_STATUS.toString()))
      {
        return DIM_RANGE_STATUS;
      }

      else if(value.equals(DIM_VALUE_STORE_STATUS.toString()))
      {
        return DIM_VALUE_STORE_STATUS;
      }

      else if(value.equals(SWITCH_STATUS.toString()))
      {
        return SWITCH_STATUS;
      }

      else if(value.startsWith(DIM_ELTAKO.toString()))
      {
        return DIM_ELTAKO;
      }

      else if(value.startsWith(DIM.toString()))
      {
        return DIM;
      }

      else
      {
        throw new ConfigurationException(
            "Command ''{0}'' is an invalid EnOcean equipment " +
            "profile (EEP) ''{1}'' command.", value, eepType
        );
      }
    }

    private String commandString;

    private Command(String command)
    {
      this.commandString = command;
    }

    @Override public String toString()
    {
      return commandString;
    }
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Type safe command from command configuration.
   */
  private Command command;

  /**
   * EnOcean equipment profile (EEP) type.
   */
  private EepType eepType;

  /**
   * EnOcean device ID for filtering received radio telegrams.
   */
  private DeviceID deviceID;

  /**
   * Dynamic command parameter (e.g slider value).
   */
  private CommandParameter parameter;

  /**
   * Indicates if a teach in telegram has been received.
   */
  private Bool teachInFlag;

  /**
   * Contains the EnOcean equipment profile (EEP) sensor data.
   */
  private EepData sensorData;

  /**
   * Contains the EnOcean equipment profile (EEP) data which is used
   * to analyze the profile {@link #sensorData}.
   */
  private EepData controlData;

  /**
   * Contains the command ID of the 'Central Command'.
   */
  private Ordinal commandID;

  /**
   * Contains the received dim value based on an absolute range (0...255).
   *
   * @see #isRelativeDimRange
   */
  private Range absoluteDim;

  /**
   * Contains the received dim value based on a relative range (0...100).
   *
   * @see #isRelativeDimRange
   */
  private Range relativeDim;

  /**
   * Contains the received dim speed value.
   */
  private Range dimSpeed;

  /**
   * Flag which indicates if the received dim value is based on an absolute or
   * relative range.
   *
   * @see #absoluteDim
   * @see #relativeDim
   */
  private Bool isRelativeDimRange;

  /**
   * Flag which indicates if the device stores the dim value.
   */
  private Bool isStoreDimValue;

  /**
   * Flag which indicates if the dimmer is turned on or off.
   */
  private Bool isSwitch;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-38-08' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID, command string and dim value parameter.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @param  parameter      dim value parameter. May be null if static dim value is part of
   *                        the command (commandString parameter).
   *
   * @throws org.openremote.controller.protocol.enocean.ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA53808(DeviceID deviceID, String commandString, CommandParameter parameter) throws ConfigurationException
  {
    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    if(commandString == null)
    {
      throw new IllegalArgumentException("null command string");
    }

    commandString = commandString.toUpperCase().trim();

    this.parameter = parameter;
    this.eepType = EepType.EEP_TYPE_A53808;
    this.deviceID = deviceID;

    this.command = Command.toCommand(commandString, eepType);

    if(this.parameter == null && (this.command == Command.DIM || this.command == Command.DIM_ELTAKO))
    {
      this.parameter = extractDimValueFromCommand(commandString, command);
    }

    this.commandID = createCommandID();
    this.teachInFlag = Bool.createTeachInFlag4BS();
    this.isRelativeDimRange = createRelativeDimValueFlag();

    this.controlData = new EepData(
        this.eepType, 4, commandID, teachInFlag, isRelativeDimRange
    );

    this.absoluteDim = createAbsoluteDimRange();
    this.relativeDim = createRelativeDimRange();
    this.dimSpeed = createDimSpeedRange();
    this.isStoreDimValue = createStoreDimValueFlag();
    this.isSwitch = createSwitchFlag();

    this.sensorData = new EepData(
        eepType, 4, absoluteDim, relativeDim,
        dimSpeed, isStoreDimValue, isSwitch
    );

  }

  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    return "EEP ('" + eepType + "' : FUNC = 'Central Command', TYPE = 'Gateway')";
  }


  // Implements EepTransmit -----------------------------------------------------------------------

  @Override public void send(RadioInterface radioInterface) throws ConfigurationException, ConnectionException
  {
    if(parameter == null)
    {
      return;
    }

    if(command == Command.DIM)
    {
      int dimValue = parameter.getValue().intValue();

      byte[] payload = new byte[4];
      payload[0] = (byte)(EEP_A53808_COM_DIM_COMMAND & 0xFF);
      payload[1] = (byte)(dimValue & 0xFF);
      payload[2] = 0;  // Use dim speed of device

      if(dimValue == 0)
      {
        payload[3] = (byte)(((EEP_A53808_SW_OFF_VALUE << 0) |
                             (EEP_A53808_EDIMR_RELATIVE_VALUE << 2) |
                             (EEP_DATA_TELEGRAM << 3)) & 0xFF);
      }

      else
      {
        payload[3] = (byte)(((EEP_A53808_SW_ON_VALUE << 0) |
                             (EEP_A53808_EDIMR_RELATIVE_VALUE << 2) |
                             (EEP_DATA_TELEGRAM << 3)) & 0xFF);
      }

      radioInterface.sendRadio(eepType.getRORG(), deviceID, payload, (byte)0x00);
    }

    else if(command == Command.DIM_ELTAKO)
    {
      int dimValue = parameter.getValue().intValue();

      byte[] payload = new byte[4];
      payload[0] = (byte)(EEP_A53808_COM_DIM_COMMAND & 0xFF);
      payload[1] = (byte)(dimValue & 0xFF);
      payload[2] = 0;  // Use dim speed of device

      if(dimValue == 0)
      {
        payload[3] = (byte)(((EEP_A53808_SW_OFF_VALUE << 0) |
                             (EEP_A53808_EDIMR_ABSOLUTE_VALUE << 2) |
                             (EEP_DATA_TELEGRAM << 3)) & 0xFF);
      }

      else
      {
        payload[3] = (byte)(((EEP_A53808_SW_ON_VALUE << 0) |
                             (EEP_A53808_EDIMR_ABSOLUTE_VALUE << 2) |
                             (EEP_DATA_TELEGRAM << 3)) & 0xFF);
      }

      radioInterface.sendRadio(eepType.getRORG(), deviceID, payload, (byte)0x00);
    }
  }

  // Implements EepReceive ------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public EepType getType()
  {
    return eepType;
  }

  /**
   * {@inheritDoc}
   */
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


    this.controlData.update(telegram.getPayload());

    if(isTeachInTelegram())
    {
      return false;
    }


    boolean isUpdate = false;

    if(isDimCmd())
    {
      if(command == Command.DIM_STATUS)
      {
        if(isRelativeDimRange.boolValue())
        {
          isUpdate = updateRangeVariable(relativeDim, telegram);
        }

        else
        {
          isUpdate = updateRangeVariable(absoluteDim, telegram);
        }
      }

      else if(command == Command.DIM_STATUS_ELTAKO)
      {
        isUpdate = updateRangeVariable(relativeDim, telegram);
      }

      else if(command == Command.DIM_SPEED_STATUS)
      {
        isUpdate = updateRangeVariable(dimSpeed, telegram);
      }

      else if(command == Command.DIM_RANGE_STATUS)
      {
        isUpdate = true;
      }

      else if(command == Command.DIM_VALUE_STORE_STATUS)
      {
        isUpdate = updateBoolVariable(isStoreDimValue, telegram);
      }

      else if(command == Command.SWITCH_STATUS)
      {
        isUpdate = updateBoolVariable(isSwitch, telegram);
      }
    } // end if(isDimCmd())

    return isUpdate;
  }

  /**
   * {@inheritDoc}
   */
  @Override public void updateSensor(Sensor sensor) throws ConfigurationException
  {
    if(isTeachInTelegram())
    {
      return;
    }

    if(isDimCmd())
    {
      if(command == Command.DIM_STATUS)
      {
        if(isRelativeDimRange.boolValue())
        {
          relativeDim.updateSensor(sensor);
        }

        else
        {
          absoluteDim.updateSensor(sensor);
        }
      }

      else if(command == Command.DIM_STATUS_ELTAKO)
      {
        relativeDim.updateSensor(sensor);
      }

      else if(command == Command.DIM_SPEED_STATUS)
      {
        dimSpeed.updateSensor(sensor);
      }

      else if(command == Command.DIM_RANGE_STATUS)
      {
        isRelativeDimRange.updateSensor(sensor);
      }

      else if(command == Command.DIM_VALUE_STORE_STATUS)
      {
        isStoreDimValue.updateSensor(sensor);
      }

      else if(command == Command.SWITCH_STATUS)
      {
        isSwitch.updateSensor(sensor);
      }
    } // end if(isDimCmd())
  }


  // Package Private Methods ----------------------------------------------------------------------

  Integer getCommandID()
  {
    return commandID.ordinalValue();
  }

  Integer getAbsoluteDimValue()
  {
    return (absoluteDim.rangeValue() == null ? null : absoluteDim.rangeValue().intValue());
  }

  Integer getRelativeDimValue()
  {
    return (relativeDim.rangeValue() == null ? null : relativeDim.rangeValue().intValue());
  }

  Integer getDimSpeedValue()
  {
    return (dimSpeed.rangeValue() == null ? null : dimSpeed.rangeValue().intValue());
  }

  Boolean isRelativeDimRange()
  {
    return isRelativeDimRange.boolValue();
  }

  Boolean isStoreDimValue()
  {
    return isStoreDimValue.boolValue();
  }

  Boolean isSwitchOn()
  {
    return isSwitch.boolValue();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Checks if the last received radio telegram was a teach in telegram.
   *
   * @return <tt>true</tt> if the last received radio telegram was a tech in telegram,
   *         <tt>false</tt> otherwise
   */
  private boolean isTeachInTelegram()
  {
    return (teachInFlag.boolValue() != null && teachInFlag.boolValue());
  }

  private Range createAbsoluteDimRange()
  {
    return Range.createRange(
        EEP_A53808_EDIM_DATA_FIELD_NAME, EEP_A53808_EDIM_OFFSET, EEP_A53808_EDIM_SIZE,
        EEP_A53808_EDIM_ABSOLUTE_RAW_DATA_RANGE_MIN, EEP_A53808_EDIM_ABSOLUTE_RAW_DATA_RANGE_MAX,
        EEP_A53808_EDIM_UNITS_DATA_RANGE_MIN, EEP_A53808_EDIM_UNITS_DATA_RANGE_MAX,
        EEP_A53808_EDIM_FRACTIONAL_DIGITS
    );
  }

  private Range createRelativeDimRange()
  {
    return Range.createRange(
        EEP_A53808_EDIM_DATA_FIELD_NAME, EEP_A53808_EDIM_OFFSET, EEP_A53808_EDIM_SIZE,
        EEP_A53808_EDIM_RELATIVE_RAW_DATA_RANGE_MIN, EEP_A53808_EDIM_RELATIVE_RAW_DATA_RANGE_MAX,
        EEP_A53808_EDIM_UNITS_DATA_RANGE_MIN, EEP_A53808_EDIM_UNITS_DATA_RANGE_MAX,
        EEP_A53808_EDIM_FRACTIONAL_DIGITS
    );
  }

  private Range createDimSpeedRange()
  {
    return Range.createRange(
        EEP_A53808_RMP_DATA_FIELD_NAME, EEP_A53808_RMP_OFFSET, EEP_A53808_RMP_SIZE,
        EEP_A53808_RMP_RAW_DATA_RANGE_MIN, EEP_A53808_RMP_RAW_DATA_RANGE_MAX,
        EEP_A53808_RMP_UNITS_DATA_RANGE_MIN, EEP_A53808_RMP_UNITS_DATA_RANGE_MAX,
        EEP_A53808_RMP_FRACTIONAL_DIGITS
    );
  }

  private Bool createRelativeDimValueFlag()
  {
    return Bool.createBool(
        EEP_A53808_EDIMR_DATA_FIELD_NAME, EEP_A53808_EDIMR_OFFSET, EEP_A53808_EDIMR_SIZE,
        EEP_A53808_EDIMR_RELATIVE_DESC, EEP_A53808_EDIMR_RELATIVE_VALUE,
        EEP_A53808_EDIMR_ABSOLUTE_DESC, EEP_A53808_EDIMR_ABSOLUTE_VALUE
    );
  }

  private Bool createStoreDimValueFlag()
  {
    return Bool.createBool(
        EEP_A53808_STR_DATA_FIELD_NAME, EEP_A53808_STR_OFFSET, EEP_A53808_STR_SIZE,
        EEP_A53808_STR_STORE_DESC, EEP_A53808_STR_STORE_VALUE,
        EEP_A53808_STR_DO_NOT_STORE_DESC, EEP_A53808_STR_DO_NOT_STORE_VALUE
    );
  }

  private Bool createSwitchFlag()
  {
    return Bool.createBool(
        EEP_A53808_SW_DATA_FIELD_NAME, EEP_A53808_SW_OFFSET, EEP_A53808_SW_SIZE,
        EEP_A53808_SW_ON_DESC, EEP_A53808_SW_ON_VALUE,
        EEP_A53808_SW_OFF_DESC, EEP_A53808_SW_OFF_VALUE
    );
  }

  private Ordinal createCommandID()
  {
    ScaleCategory dimCategory = new ScaleCategory(
        EEP_A53808_COM_DIM_COMMAND_Name,
        EEP_A53808_COM_DIM_RAW_VALUE_RANGE_MIN, EEP_A53808_COM_DIM_RAW_VALUE_RANGE_MAX,
        EEP_A53808_COM_DIM_COMMAND_Name, EEP_A53808_COM_DIM_COMMAND
    );

    CategoricalScale scale = new CategoricalScale(dimCategory);
    EepDataField dataField = new EepDataField(
        EEP_A53808_COM_DATA_FIELD_NAME, EEP_A53808_COM_OFFSET, EEP_A53808_COM_SIZE
    );
    Ordinal dim = new Ordinal(dataField, scale);

    return dim;
  }

  /**
   * Updates the given range variable with EnOcean equipment profile data from
   * a received radio telegram.
   *
   * @param range           the range variable to be updated
   *
   * @param telegram        received radio telegram
   *
   * @return                <tt>true</tt> if the sensor variable has been updated with a new value,
   *                        <tt>false</tt> otherwise
   */
  private boolean updateRangeVariable(Range range, EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Double oldValue = range.rangeValue();

    sensorData.update(telegram.getPayload());

    Double newValue = range.rangeValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null  &&
                 newValue.doubleValue() != oldValue.doubleValue()));

    return isUpdate;
  }

  /**
   * Updates the given bool variable with EnOcean equipment profile data from
   * a received radio telegram.
   *
   * @param  bool      the bool variable to be updated
   *
   * @param  telegram  received radio telegram
   *
   * @return <tt>true</tt> if the sensor variable has been updated with a new value,
   *         <tt>false</tt> otherwise
   */
  private boolean updateBoolVariable(Bool bool, EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Boolean oldValue = bool.boolValue();

    sensorData.update(telegram.getPayload());

    Boolean newValue = bool.boolValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null  &&
                 newValue.booleanValue() != oldValue.booleanValue()));

    return isUpdate;
  }

  private boolean isDimCmd()
  {
    boolean isDim = false;

    Integer commandIDValue = commandID.ordinalValue();

    if(commandIDValue != null && commandIDValue == EEP_A53808_COM_DIM_COMMAND)
    {
      isDim = true;
    }

    return isDim;
  }

  private CommandParameter extractDimValueFromCommand(String commandString, Command command) throws NoSuchCommandException
  {
    CommandParameter parameter = null;

    Matcher m;

    if(command == Command.DIM_ELTAKO)
    {
      m = DIM_VALUE_REGEX_ELTAKO.matcher(commandString);
    }
    else
    {
      m = DIM_VALUE_REGEX.matcher(commandString);
    }

    if(!m.matches())
    {
      throw new NoSuchCommandException("Missing value parameter for DIM command");
    }

    try
    {
      parameter = new CommandParameter(m.group(1));
    }

    catch(ConversionException e)
    {
      throw new NoSuchCommandException(e.getMessage(), e);
    }

    return parameter;
  }
}
