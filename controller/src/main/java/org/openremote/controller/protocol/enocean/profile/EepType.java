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

import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram.RORG;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the EnOcean equipment profile (EEP) number as defined in the EnOcean equipment
 * profiles specification 2.1 chapter: 1.2) General. <p>
 *
 * <pre>{@code
 *
 *            +------+------+------+
 * EEP 2.0    | ORG  | FUNC | TYPE |
 *            +------+------+------+
 * EEP 2.1    | RORG | FUNC | TYPE |
 *            +------+------+------+
 * Range(hex) |00..FF|00..3F|00..7F|
 *            +------+------+------+
 *             8 bit  6 bit  7 bit
 *
 * }</pre>
 *
 * Each EnOcean equipment profile number consists of 3 parts. The EEP number starts with
 * the radio telegram type ({@link EspRadioTelegram.RORG} followed by the "basic functionality
 * of the data content (FUNC)". The last part denotes the "type of device in its individual
 * characteristics (TYPE)".
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class EepType
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Lookup table for all supported EnOcean equipment profiles.
   */
  private final static Map<String, EepType> lookup = new HashMap<String, EepType>(100);

  /**
   * EnOcean equipment profile (EEP) 'F6-02-01' type.
   */
  public final static EepType EEP_TYPE_F60201 = new EepTypeF60201();

  /**
   * EnOcean equipment profile (EEP) 'D5-00-01' type.
   */
  public final static EepType EEP_TYPE_D50001 = new EepTypeD50001();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-01' type.
   */
  public final static EepType EEP_TYPE_A50201 = new EepTypeA50201();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-02' type.
   */
  public final static EepType EEP_TYPE_A50202 = new EepTypeA50202();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-03' type.
   */
  public final static EepType EEP_TYPE_A50203 = new EepTypeA50203();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-04' type.
   */
  public final static EepType EEP_TYPE_A50204 = new EepTypeA50204();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-05' type.
   */
  public final static EepType EEP_TYPE_A50205 = new EepTypeA50205();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-06' type.
   */
  public final static EepType EEP_TYPE_A50206 = new EepTypeA50206();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-07' type.
   */
  public final static EepType EEP_TYPE_A50207 = new EepTypeA50207();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-08' type.
   */
  public final static EepType EEP_TYPE_A50208 = new EepTypeA50208();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-09' type.
   */
  public final static EepType EEP_TYPE_A50209 = new EepTypeA50209();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-0A' type.
   */
  public final static EepType EEP_TYPE_A5020A = new EepTypeA5020A();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-0B' type.
   */
  public final static EepType EEP_TYPE_A5020B = new EepTypeA5020B();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-10' type.
   */
  public final static EepType EEP_TYPE_A50210 = new EepTypeA50210();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-11' type.
   */
  public final static EepType EEP_TYPE_A50211 = new EepTypeA50211();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-12' type.
   */
  public final static EepType EEP_TYPE_A50212 = new EepTypeA50212();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-13' type.
   */
  public final static EepType EEP_TYPE_A50213 = new EepTypeA50213();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-14' type.
   */
  public final static EepType EEP_TYPE_A50214 = new EepTypeA50214();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-15' type.
   */
  public final static EepType EEP_TYPE_A50215 = new EepTypeA50215();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-16' type.
   */
  public final static EepType EEP_TYPE_A50216 = new EepTypeA50216();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-17' type.
   */
  public final static EepType EEP_TYPE_A50217 = new EepTypeA50217();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-18' type.
   */
  public final static EepType EEP_TYPE_A50218 = new EepTypeA50218();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-19' type.
   */
  public final static EepType EEP_TYPE_A50219 = new EepTypeA50219();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-1A' type.
   */
  public final static EepType EEP_TYPE_A5021A = new EepTypeA5021A();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-1B' type.
   */
  public final static EepType EEP_TYPE_A5021B = new EepTypeA5021B();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-20' type.
   */
  public final static EepType EEP_TYPE_A50220 = new EepTypeA50220();

  /**
   * EnOcean equipment profile (EEP) 'A5-02-30' type.
   */
  public final static EepType EEP_TYPE_A50230 = new EepTypeA50230();

  /**
   * EnOcean equipment profile (EEP) 'A5-04-01' type.
   */
  public final static EepType EEP_TYPE_A50401 = new EepTypeA50401();

  /**
   * EnOcean equipment profile (EEP) 'A5-06-01' type.
   */
  public final static EepType EEP_TYPE_A50601 = new EepTypeA50601();

  /**
   * EnOcean equipment profile (EEP) 'A5-06-02' type.
   */
  public final static EepType EEP_TYPE_A50602 = new EepTypeA50602();

  /**
   * EnOcean equipment profile (EEP) 'A5-07-01' type.
   */
  public final static EepType EEP_TYPE_A50701 = new EepTypeA50701();

  /**
   * EnOcean equipment profile (EEP) 'A5-08-01' type.
   */
  public final static EepType EEP_TYPE_A50801 = new EepTypeA50801();

  /**
   * EnOcean equipment profile (EEP) 'A5-08-02' type.
   */
  public final static EepType EEP_TYPE_A50802 = new EepTypeA50802();

  /**
   * EnOcean equipment profile (EEP) 'A5-08-03' type.
   */
  public final static EepType EEP_TYPE_A50803 = new EepTypeA50803();

  /**
   * EnOcean equipment profile (EEP) 'A5-09-04' type.
   */
  public final static EepType EEP_TYPE_A50904 = new EepTypeA50904();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-01' type.
   */
  public final static EepType EEP_TYPE_A51001 = new EepTypeA51001();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-02' type.
   */
  public final static EepType EEP_TYPE_A51002 = new EepTypeA51002();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-03' type.
   */
  public final static EepType EEP_TYPE_A51003 = new EepTypeA51003();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-04' type.
   */
  public final static EepType EEP_TYPE_A51004 = new EepTypeA51004();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-05' type.
   */
  public final static EepType EEP_TYPE_A51005 = new EepTypeA51005();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-06' type.
   */
  public final static EepType EEP_TYPE_A51006 = new EepTypeA51006();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-07' type.
   */
  public final static EepType EEP_TYPE_A51007 = new EepTypeA51007();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-08' type.
   */
  public final static EepType EEP_TYPE_A51008 = new EepTypeA51008();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-09' type.
   */
  public final static EepType EEP_TYPE_A51009 = new EepTypeA51009();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-0A' type.
   */
  public final static EepType EEP_TYPE_A5100A = new EepTypeA5100A();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-0B' type.
   */
  public final static EepType EEP_TYPE_A5100B = new EepTypeA5100B();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-0C' type.
   */
  public final static EepType EEP_TYPE_A5100C = new EepTypeA5100C();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-0D' type.
   */
  public final static EepType EEP_TYPE_A5100D = new EepTypeA5100D();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-10' type.
   */
  public final static EepType EEP_TYPE_A51010 = new EepTypeA51010();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-11' type.
   */
  public final static EepType EEP_TYPE_A51011 = new EepTypeA51011();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-12' type.
   */
  public final static EepType EEP_TYPE_A51012 = new EepTypeA51012();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-13' type.
   */
  public final static EepType EEP_TYPE_A51013 = new EepTypeA51013();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-14' type.
   */
  public final static EepType EEP_TYPE_A51014 = new EepTypeA51014();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-15' type.
   */
  public final static EepType EEP_TYPE_A51015 = new EepTypeA51015();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-16' type.
   */
  public final static EepType EEP_TYPE_A51016 = new EepTypeA51016();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-17' type.
   */
  public final static EepType EEP_TYPE_A51017 = new EepTypeA51017();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-18' type.
   */
  public final static EepType EEP_TYPE_A51018 = new EepTypeA51018();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-19' type.
   */
  public final static EepType EEP_TYPE_A51019 = new EepTypeA51019();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-1A' type.
   */
  public final static EepType EEP_TYPE_A5101A = new EepTypeA5101A();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-1B' type.
   */
  public final static EepType EEP_TYPE_A5101B = new EepTypeA5101B();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-1C' type.
   */
  public final static EepType EEP_TYPE_A5101C = new EepTypeA5101C();

  /**
   * EnOcean equipment profile (EEP) 'A5-10-1D' type.
   */
  public final static EepType EEP_TYPE_A5101D = new EepTypeA5101D();

  /**
   * EnOcean equipment profile (EEP) 'A5-12-00' type.
   */
  public final static EepType EEP_TYPE_A51200 = new EepTypeA51200();

  /**
   * EnOcean equipment profile (EEP) 'A5-12-01' type.
   */
  public final static EepType EEP_TYPE_A51201 = new EepTypeA51201();

  /**
   * EnOcean equipment profile (EEP) 'A5-12-02' type.
   */
  public final static EepType EEP_TYPE_A51202 = new EepTypeA51202();

  /**
   * EnOcean equipment profile (EEP) 'A5-12-03' type.
   */
  public final static EepType EEP_TYPE_A51203 = new EepTypeA51203();

  /**
   * EnOcean equipment profile (EEP) 'A5-20-01' type.
   */
  public final static EepType EEP_TYPE_A52001 = new EepTypeA52001();

  /**
   * EnOcean equipment profile (EEP) 'A5-38-08' type.
   */
  public final static EepType EEP_TYPE_A53808 = new EepTypeA53808();

  /**
   * End of FUNC value range: {@value}
   */
  public final static int EEP_MAX_FUNC = 0x3F;

  /**
   * End of TYPE value range: {@value}
   */
  public final static int EEP_MAX_TYPE = 0x7F;

  /**
   * Regex pattern for parsing EEP numbers.
   */
  private final static Pattern EEP_NUMBER = Pattern.compile("^([0-9A-F]{2})-([0-9A-F]{2})-([0-9A-F]{2})$");


  /**
   * Returns an EEP type instance if the given EnOcean equipment profile is supported.
   *
   * @param  eep  EnOcean equipment profile number with following format: XX-XX-XX
   *
   * @return EEP type instance if profile is supported otherwise <tt>null</tt>
   *
   * @throws InvalidEepTypeExpception
   *           if the EEP parameter cannot be parsed
   *
   */
  public static EepType lookup(String eep) throws InvalidEepTypeExpception
  {
    if(eep == null)
    {
      throw new IllegalArgumentException("null EEP");
    }

    eep = eep.toUpperCase().trim();

    EepType eepType;

    Matcher matcher = EEP_NUMBER.matcher(eep);

    if(matcher.matches())
    {
      eepType = lookup.get(eep);
    }

    else
    {
      throw new InvalidEepTypeExpception(
          "Cannot parse EEP number '" + eep +
          "' (assuming hexadecimal representation - e.g. A5-02-1A)"
      );
    }

    return eepType;
  }

  /**
   * Returns string representation of EEP type with the common EEP format XX-XX-XX.
   *
   * @param rorg  First part of the EEP number: RORG-XX-XX.
   *
   * @param func  Second part of the EEP number: XX-FUNC-XX.
   *
   * @param type  Third part of the EEP number: XX-XX-TYPE.
   *
   * @return EEP number
   */
  public static String getEepTypeAsString(EspRadioTelegram.RORG rorg, int func, int type)
  {
    return String.format("%02X-%02X-%02X", rorg.getValue(), func, type);
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * First part of the EEP number: RORG-XX-XX.
   */
  private final EspRadioTelegram.RORG rorg;

  /**
   * Second part of the EEP number: XX-FUNC-XX.
   */
  private final int func;

  /**
   * Third part of the EEP number: XX-XX-TYPE.
   */
  private final int type;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new EEP type instance and puts it into the EEP lookup table of all supported
   * profiles.
   *
   * @param rorg  first part (RORG-XX-XX) of the EEP number
   *
   * @param func  second part (XX-FUNC-XX) of the EEP number
   *
   * @param type  third part (XX-XX-TYPE) of the EEP number
   */
  protected EepType(EspRadioTelegram.RORG rorg, int func, int type)
  {
    this.rorg = rorg;
    this.func = func;
    this.type = type;

    lookup.put(EepType.getEepTypeAsString(rorg, func, type), this);

    if(rorg == EspRadioTelegram.RORG.RPS)
    {
      lookup.put(EepType.getEepTypeAsString(EspRadioTelegram.RORG.RPS_ESP2, func, type), this);
    }

    else if(rorg == EspRadioTelegram.RORG.BS1)
    {
      lookup.put(EepType.getEepTypeAsString(EspRadioTelegram.RORG.BS1_ESP2, func, type), this);
    }

    else if(rorg == EspRadioTelegram.RORG.BS4)
    {
      lookup.put(EepType.getEepTypeAsString(EspRadioTelegram.RORG.BS4_ESP2, func, type), this);
    }
  }

  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public boolean equals(Object o)
  {
    if(o == null)
      return false;

    if(!o.getClass().equals(this.getClass()))
      return false;

    EepType eep = (EepType)o;

    return (this.rorg == eep.rorg ||
           (this.rorg == EspRadioTelegram.RORG.RPS && eep.rorg == EspRadioTelegram.RORG.RPS_ESP2) ||
           (this.rorg == EspRadioTelegram.RORG.BS1 && eep.rorg == EspRadioTelegram.RORG.BS1_ESP2) ||
           (this.rorg == EspRadioTelegram.RORG.BS4 && eep.rorg == EspRadioTelegram.RORG.BS4_ESP2)) &&
            this.func == eep.func &&
            this.type == eep.type;
  }

  /**
   * {@inheritDoc}
   */
  @Override public int hashCode()
  {
    return ((rorg.getValue() & 0xFF) << 16) +
           (func << 8) + type;
  }

  /**
   * Returns string representation of this EEP type.
   *
   * @return EEP number with following format: RORG-FUNC-TYPE (e.g. F6-02-01)
   */
  @Override public String toString()
  {
    return EepType.getEepTypeAsString(rorg, func, type);
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Creates an EnOcean equipment profile instance which relates to this EEP type.
   *
   * @param  deviceID  EnOcean device ID
   *
   * @param  command   command string from the command configuration this EEP type relates to
   *
   * @return new EnOcean equipment profile instance
   *
   * @throws ConfigurationException
   *           if the command is unknown
   */
  public abstract Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException;

  /**
   * Returns the RORG-XX-XX part of the EEP type.
   *
   * @return the RORG-XX-XX part
   */
  public EspRadioTelegram.RORG getRORG()
  {
    return rorg;
  }

  /**
   * Returns the XX-FUNC-XX part of the EEP type.
   *
   * @return the value of the XX-FUNC-XX part
   */
  public int getFunc()
  {
    return func;
  }

  /**
   * Returns the XX-XX-TYPE part of the EEP type.
   *
   * @return the value of the XX-XX-TYPE part
   */
  public int getType()
  {
    return type;
  }

  /**
   * Checks if the RORG type of the given radio telegram is compatible
   * with the EEP type.
   *
   * @param   telegram  radio telegram
   *
   * @return  <tt>true</tt> if valid, <tt>false</tt> otherwise
   */
  public boolean isValidRadioTelegramRORG(EspRadioTelegram telegram)
  {

    if(this.rorg == telegram.getRORG())
    {
      return true;
    }

    else if(EspRadioTelegram.RORG.RPS == this.rorg &&
            EspRadioTelegram.RORG.RPS_ESP2 == telegram.getRORG())
    {
      return true;
    }

    else if(EspRadioTelegram.RORG.BS1 == this.rorg &&
            EspRadioTelegram.RORG.BS1_ESP2 == telegram.getRORG())
    {
      return true;
    }

    else if(EspRadioTelegram.RORG.BS4 == this.rorg &&
            EspRadioTelegram.RORG.BS4_ESP2 == telegram.getRORG())
    {
      return true;
    }

    return false;
  }


  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Indicates an invalid EnOcean equipment profile (EEP) string.
   */
  public static class InvalidEepTypeExpception extends Exception
  {
    public InvalidEepTypeExpception(String msg)
    {
      super(msg);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'F6-02-01'.
   */
  private static class EepTypeF60201 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number F6-02-01 and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeF60201()
    {
      super(EspRadioTelegram.RORG.RPS, 0x02, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepF60201(this, deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'D5-00-01'.
   */
  private static class EepTypeD50001 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number D5-00-01 and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeD50001()
    {
      super(EspRadioTelegram.RORG.BS1, 0x00, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepD50001(deviceID, command);
    }
  }


  /**
   * EnOcean equipment profile (EEP) type 'A5-02-01'.
   */
  private static class EepTypeA50201 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50201()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50201(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-02'.
   */
  private static class EepTypeA50202 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-02' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50202()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50202(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-03'.
   */
  private static class EepTypeA50203 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-03' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50203()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x03);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50203(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-04'.
   */
  private static class EepTypeA50204 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-04' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50204()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x04);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50204(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-05'.
   */
  private static class EepTypeA50205 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number A5-02-05 and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50205()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x05);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50205(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-06'.
   */
  private static class EepTypeA50206 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-06' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50206()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x06);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50206(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-07'.
   */
  private static class EepTypeA50207 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-07' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50207()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x07);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50207(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-08'.
   */
  private static class EepTypeA50208 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-08' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50208()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x08);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50208(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-09'.
   */
  private static class EepTypeA50209 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-09' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50209()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x09);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50209(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-0A'.
   */
  private static class EepTypeA5020A extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-0A' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5020A()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x0A);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5020A(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-0B'.
   */
  private static class EepTypeA5020B extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-0B' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5020B()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x0B);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5020B(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-10'.
   */
  private static class EepTypeA50210 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-10' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50210()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x10);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50210(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-11'.
   */
  private static class EepTypeA50211 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-11' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50211()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x11);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50211(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-12'.
   */
  private static class EepTypeA50212 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-12' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50212()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x12);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50212(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-13'.
   */
  private static class EepTypeA50213 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-13' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50213()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x13);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50213(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-14'.
   */
  private static class EepTypeA50214 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-14' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50214()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x14);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50214(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-15'.
   */
  private static class EepTypeA50215 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-15' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50215()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x15);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50215(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-16'.
   */
  private static class EepTypeA50216 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-16' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50216()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x16);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50216(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-17'.
   */
  private static class EepTypeA50217 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-17' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50217()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x17);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50217(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-18'.
   */
  private static class EepTypeA50218 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-18' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50218()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x18);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50218(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-19'.
   */
  private static class EepTypeA50219 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-19' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50219()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x19);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50219(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-1A'.
   */
  private static class EepTypeA5021A extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-1A' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5021A()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x1A);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5021A(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-1B'.
   */
  private static class EepTypeA5021B extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-1B' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5021B()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x1B);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5021B(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-20'.
   */
  private static class EepTypeA50220 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-20' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50220()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x20);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50220(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-02-30'.
   */
  private static class EepTypeA50230 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-02-30' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50230()
    {
      super(EspRadioTelegram.RORG.BS4, 0x02, 0x30);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50230(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-04-01'.
   */
  private static class EepTypeA50401 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-04-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50401()
    {
      super(EspRadioTelegram.RORG.BS4, 0x04, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50401(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-06-01'.
   */
  private static class EepTypeA50601 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-06-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50601()
    {
      super(EspRadioTelegram.RORG.BS4, 0x06, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50601(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-06-02'.
   */
  private static class EepTypeA50602 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-06-02' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50602()
    {
      super(EspRadioTelegram.RORG.BS4, 0x06, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50602(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-07-01'.
   */
  private static class EepTypeA50701 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-07-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50701()
    {
      super(EspRadioTelegram.RORG.BS4, 0x07, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50701(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-08-01'.
   */
  private static class EepTypeA50801 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-08-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50801()
    {
      super(EspRadioTelegram.RORG.BS4, 0x08, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50801(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-08-02'.
   */
  private static class EepTypeA50802 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-08-02' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50802()
    {
      super(EspRadioTelegram.RORG.BS4, 0x08, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50802(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-08-03'.
   */
  private static class EepTypeA50803 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-08-03' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50803()
    {
      super(EspRadioTelegram.RORG.BS4, 0x08, 0x03);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50803(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-09-04'.
   */
  private static class EepTypeA50904 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-09-04' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA50904()
    {
      super(EspRadioTelegram.RORG.BS4, 0x09, 0x04);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA50904(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-01'.
   */
  private static class EepTypeA51001 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51001()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51001(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-02'.
   */
  private static class EepTypeA51002 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-02' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51002()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51002(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-03'.
   */
  private static class EepTypeA51003 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-03' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51003()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x03);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51003(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-04'.
   */
  private static class EepTypeA51004 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-04' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51004()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x04);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51004(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-05'.
   */
  private static class EepTypeA51005 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-05' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51005()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x05);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51005(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-06'.
   */
  private static class EepTypeA51006 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-06' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51006()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x06);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51006(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-07'.
   */
  private static class EepTypeA51007 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-07' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51007()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x07);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51007(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-08'.
   */
  private static class EepTypeA51008 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-08' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51008()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x08);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51008(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-09'.
   */
  private static class EepTypeA51009 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-09' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51009()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x09);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51009(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-0A'.
   */
  private static class EepTypeA5100A extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-0A' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5100A()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x0A);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5100A(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-0B'.
   */
  private static class EepTypeA5100B extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-0B' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5100B()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x0B);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5100B(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-0C'.
   */
  private static class EepTypeA5100C extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-0C' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5100C()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x0C);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5100C(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-0D'.
   */
  private static class EepTypeA5100D extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-0D' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5100D()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x0D);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5100D(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-10'.
   */
  private static class EepTypeA51010 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-10' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51010()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x10);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51010(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-11'.
   */
  private static class EepTypeA51011 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-11' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51011()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x11);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51011(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-12'.
   */
  private static class EepTypeA51012 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-12' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51012()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x12);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51012(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-13'.
   */
  private static class EepTypeA51013 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-13' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51013()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x13);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51013(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-14'.
   */
  private static class EepTypeA51014 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-14' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51014()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x14);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51014(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-15'.
   */
  private static class EepTypeA51015 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-15' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51015()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x15);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51015(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-16'.
   */
  private static class EepTypeA51016 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-16' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51016()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x16);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51016(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-17'.
   */
  private static class EepTypeA51017 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-17' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51017()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x17);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51017(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-18'.
   */
  private static class EepTypeA51018 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-18' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51018()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x18);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51018(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-19'.
   */
  private static class EepTypeA51019 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-19' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51019()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x19);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51019(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-1A'.
   */
  private static class EepTypeA5101A extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-1A' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5101A()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x1A);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5101A(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-1B'.
   */
  private static class EepTypeA5101B extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-1B' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5101B()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x1B);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5101B(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-1C'.
   */
  private static class EepTypeA5101C extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-1C' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5101C()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x1C);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5101C(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-10-1D'.
   */
  private static class EepTypeA5101D extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-10-1D' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA5101D()
    {
      super(EspRadioTelegram.RORG.BS4, 0x10, 0x1D);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA5101D(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-12-00'.
   */
  private static class EepTypeA51200 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-12-00' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51200()
    {
      super(EspRadioTelegram.RORG.BS4, 0x12, 0x00);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51200(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-12-01'.
   */
  private static class EepTypeA51201 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-12-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51201()
    {
      super(EspRadioTelegram.RORG.BS4, 0x12, 0x01);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51201(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-12-02'.
   */
  private static class EepTypeA51202 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-12-02' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51202()
    {
      super(EspRadioTelegram.RORG.BS4, 0x12, 0x02);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51202(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-12-03'.
   */
  private static class EepTypeA51203 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-12-03' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA51203()
    {
      super(EspRadioTelegram.RORG.BS4, 0x12, 0x03);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA51203(deviceID, command);
    }
  }
  
  private static class EepTypeA52001 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-20-01' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA52001() {
      super(EspRadioTelegram.RORG.BS4, 0x20, 0x01);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command) throws ConfigurationException
    {
      return new EepA52001(deviceID, command);
    }
  }

  /**
   * EnOcean equipment profile (EEP) type 'A5-38-08'.
   */
  private static class EepTypeA53808 extends EepType
  {
    /**
     * Constructs a new instance for the EEP number 'A5-38-08' and puts it into the
     * lookup table of all supported profiles.
     */
    public EepTypeA53808()
    {
      super(EspRadioTelegram.RORG.BS4, 0x38, 0x08);
    }

    /**
     * {@inheritDoc}
     */
    @Override public Eep createEep(DeviceID deviceID, String command, CommandParameter parameter) throws ConfigurationException
    {
      return new EepA53808(deviceID, command, parameter);
    }
  }

}

