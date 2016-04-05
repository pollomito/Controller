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
package org.openremote.controller.protocol.enocean.packet.command;


import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;

import java.text.MessageFormat;

/**
 * Response for {@link Esp3RdVersionCommand read version command}. <p>
 *
 * The EnOcean Serial Protocol 3 specification chapter 1.11.5 Code 03: CO_RD_VERSION defines
 * the response structure as follows:
 *
 * <pre>
 *         +--------+------...------+--------+--------+-----...-----+-----...-----+-- ...
 *         |  Sync  |    Header     |  CRC8  | Return |     APP     |     API     |
 *         |  Byte  |               | Header |  Code  |   Version   |   Version   |
 *         +--------+------...------+--------+--------+-----...-----+-----...-----+-- ...
 *           1 byte      4 bytes      1 byte   1 byte     4 bytes       4 bytes
 *
 *   ... --+-----...-----+-----...-----+---------------...---------------+--------+
 *         |   Chip ID   |    Chip     |           Application           |  CRC8  |
 *         |             |   Version   |           Description           |  Data  |
 *   ... --+-----...-----+-----...-----+---------------...---------------+--------+
 *             4 bytes       4 bytes                16 bytes               1 byte
 *</pre>
 *
 *
 * @see Esp3RdVersionCommand
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdVersionResponse extends Esp3ResponsePacket
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of the APP version field relative to the data group: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_APP_VERSION_INDEX = 0x01;

  /**
   * Byte order index of the API version field relative to the data group: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_API_VERSION_INDEX = 0x05;

  /**
   * Byte order index of the chip ID field relative to the data group: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_CHIP_ID_INDEX = 0x09;

  /**
   * Byte order index of the chip version field relative to the data group: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_CHIP_VERSION_INDEX = 0x0D;

  /**
   * Byte order index of the application description field relative to the data group: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_APP_DESC_INDEX = 0x11;

  /**
   * Length of the APP version field: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_APP_VERSION_LENGTH = 0x04;

  /**
   *  Length of the API version field: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_API_VERSION_LENGTH = 0x04;

  /**
   *  Length of the chip ID field: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_CHIP_ID_LENGTH = DeviceID.ENOCEAN_ESP_ID_LENGTH;

  /**
   *  Length of the chip version field: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_CHIP_VERSION_LENGTH = 0x04;

  /**
   *  Length of the application description field: {@value}
   */
  public static final int ESP3_RESPONSE_RD_VERSION_APP_DESC_LENGTH = 0x10;

  /**
   * Length of the data group.
   */
  public static final int ESP3_RESPONSE_RD_VERSION_DATA_LENGTH =
      ESP3_RESPONSE_RETURN_CODE_LENGTH +
      ESP3_RESPONSE_RD_VERSION_APP_VERSION_LENGTH +
      ESP3_RESPONSE_RD_VERSION_API_VERSION_LENGTH +
      ESP3_RESPONSE_RD_VERSION_CHIP_ID_LENGTH +
      ESP3_RESPONSE_RD_VERSION_CHIP_VERSION_LENGTH +
      ESP3_RESPONSE_RD_VERSION_APP_DESC_LENGTH;


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Application version.
   */
  private Version appVersion;

  /**
   * API version.
   */
  private Version apiVersion;

  /**
   * Chip ID.
   */
  private DeviceID chipID;

  /**
   * Application description.
   */
  private String appDescription;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a read version response instance with given data group.
   *
   * @param  data data group
   *
   * @throws EspException
   *           if the {@link Esp3ResponsePacket.ReturnCode return code} is unknown or
   *           the data group does not have the expected length
   */
  public Esp3RdVersionResponse(byte[] data) throws EspException
  {
    super(data, null);

    if(data.length != ESP3_RESPONSE_RD_VERSION_DATA_LENGTH)
    {
      throw new EspException(
          EspException.ErrorCode.RESP_INVALID_RESPONSE,
          "Failed to create CO_RD_VERSION response instance. " +
          "Expected {0} data bytes, got {1}.",
          ESP3_RESPONSE_RD_VERSION_DATA_LENGTH, data.length
      );
    }

    appVersion = getAppVersionFromData(data);

    apiVersion = getApiVersionFromData(data);

    chipID = getChipIDFromData(data);

    appDescription = getAppDescriptionFromData(data);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the application version.
   *
   * @return application version
   */
  public Version getAppVersion()
  {
    return appVersion;
  }

  /**
   * Returns the API version.
   *
   * @return API version
   */
  public Version getApiVersion()
  {
    return apiVersion;
  }

  /**
   * Returns the chip ID.
   *
   * @return chip ID
   */
  public DeviceID getChipID()
  {
    return chipID;
  }

  /**
   * Returns the application description.
   *
   * @return application description
   */
  public String getAppDescription()
  {
    return appDescription;
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Retrieves the application version from the data group and returns it.
   *
   * @param  data data group
   *
   * @return application version
   */
  private Version getAppVersionFromData(byte[] data)
  {
    byte[] appVersionBytes = new byte[ESP3_RESPONSE_RD_VERSION_APP_VERSION_LENGTH];

    System.arraycopy(
        data, ESP3_RESPONSE_RD_VERSION_APP_VERSION_INDEX,
        appVersionBytes, 0, ESP3_RESPONSE_RD_VERSION_APP_VERSION_LENGTH
    );

    return new Version(Version.VersionType.APP, appVersionBytes);
  }

  /**
   * Retrieves the API version from the data group and returns it.
   *
   * @param  data data group
   *
   * @return API version
   */
  private Version getApiVersionFromData(byte[] data)
  {
    byte[] apiVersionBytes = new byte[ESP3_RESPONSE_RD_VERSION_API_VERSION_LENGTH];

    System.arraycopy(
        data, ESP3_RESPONSE_RD_VERSION_API_VERSION_INDEX,
        apiVersionBytes, 0, ESP3_RESPONSE_RD_VERSION_API_VERSION_LENGTH
    );

    return new Version(Version.VersionType.API, apiVersionBytes);
  }

  /**
   * Retrieves the chip ID from the data group and returns it.
   *
   * @param  data data group
   *
   * @return chip ID
   */
  private DeviceID getChipIDFromData(byte[] data)
  {
    byte[] chipIDBytes = new byte[ESP3_RESPONSE_RD_VERSION_CHIP_ID_LENGTH];

    System.arraycopy(
        data, ESP3_RESPONSE_RD_VERSION_CHIP_ID_INDEX,
        chipIDBytes, 0, ESP3_RESPONSE_RD_VERSION_CHIP_ID_LENGTH
    );

    return DeviceID.fromByteArray(chipIDBytes);
  }

  /**
   * Retrieves the application description from the data group and returns it.
   *
   * @param  data data group
   *
   * @return application description
   */
  private String getAppDescriptionFromData(byte[] data)
  {
    int offset = ESP3_RESPONSE_RD_VERSION_APP_DESC_INDEX;
    int length = ESP3_RESPONSE_RD_VERSION_APP_DESC_LENGTH;

    for(int index = offset; index < offset + ESP3_RESPONSE_RD_VERSION_APP_DESC_LENGTH; index++)
    {
      if(data[index] == '\0')
      {
        length = index - offset;
        break;
      }
    }

    return new String(data, offset, length);
  }

  // Inner Classes --------------------------------------------------------------------------------

  /**
   * Represents the content of the application or API version field.
   *
   * <pre>
   *     |--- APP/API version field ----|
   *     +-------+-------+-------+------+
   *     | Main  | Beta  | Alpha |Build |
   *     |Version|Version|Version|      |
   *     +-------+-------+-------+------+
   *      1 byte  1 byte  1 byte  1 byte
   * </pre>
   *
   */
  public static class Version
  {

    // Enums --------------------------------------------------------------------------------------

    /**
     * Indicates if it's an application version or an API version instance.
     */
    enum VersionType
    {
      /**
       * Application version.
       */
      APP,

      /**
       * API version.
       */
      API
    }

    // Constants ----------------------------------------------------------------------------------

    /**
     * Byte order index of the main version field relative to the version field: {@value}
     */
    public static final int ESP3_RESPONSE_RD_VERSION_MAIN_VERSION_INDEX = 0x00;

    /**
     * Byte order index of the beta version field relative to the version field: {@value}
     */
    public static final int ESP3_RESPONSE_RD_VERSION_BETA_VERSION_INDEX = 0x01;

    /**
     * Byte order index of the alpha version field relative to the version field: {@value}
     */
    public static final int ESP3_RESPONSE_RD_VERSION_ALPHA_VERSION_INDEX = 0x02;

    /**
     * Byte order index of the build field relative to the version field: {@value}
     */
    public static final int ESP3_RESPONSE_RD_VERSION_BUILD_INDEX = 0x03;


    // Private Instance Fields --------------------------------------------------------------------

    /**
     * Version type.
     */
    private VersionType type;

    /**
     * Main version.
     */
    private int mainVersion;

    /**
     * Beta version.
     */
    private int betaVersion;

    /**
     * Alpha version.
     */
    private int alphaVersion;

    /**
     * Build version.
     */
    private int build;


    // Constructors -------------------------------------------------------------------------------

    /**
     * Constructs a new version instance with given type and version bytes.
     *
     * @param type         version type
     *
     * @param versionBytes version bytes (4 bytes)
     */
    public Version(VersionType type, byte[] versionBytes)
    {
      if(versionBytes == null)
      {
        throw new IllegalArgumentException("null version bytes");
      }

      if(versionBytes.length != ESP3_RESPONSE_RD_VERSION_APP_VERSION_LENGTH)
      {
        throw new IllegalArgumentException(
            "Invalid number of version bytes."
        );
      }

      this.type = type;

      mainVersion = versionBytes[ESP3_RESPONSE_RD_VERSION_MAIN_VERSION_INDEX];
      betaVersion = versionBytes[ESP3_RESPONSE_RD_VERSION_BETA_VERSION_INDEX];
      alphaVersion = versionBytes[ESP3_RESPONSE_RD_VERSION_ALPHA_VERSION_INDEX];
      build = versionBytes[ESP3_RESPONSE_RD_VERSION_BUILD_INDEX];
    }

    // Object Overrides ---------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override public boolean equals(Object o)
    {
      if(o == null)
        return false;

      if(!o.getClass().equals(this.getClass()))
        return false;

      Version version = (Version)o;

      return this.mainVersion == version.mainVersion &&
             this.betaVersion == version.betaVersion &&
             this.alphaVersion == version.alphaVersion &&
             this.build == version.build;
    }

    /**
     * {@inheritDoc}
     */
    @Override public int hashCode()
    {
      return (int)((long)(mainVersion & 0xFF) +
             (((long)(betaVersion & 0xFF)) << 8) +
             (((long)(alphaVersion & 0xFF)) << 16) +
             (((long)(build & 0xFF)) << 24));
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString()
    {
      String typeString;

      if(type == VersionType.APP)
      {
        typeString = "APP Version";
      }
      else
      {
        typeString = "API Version";
      }

      return MessageFormat.format(
          "{0}: [Main={1}, Beta={2}, Alpha={3}, Build={4}]",
          typeString, mainVersion, betaVersion, alphaVersion, build
      );
    }


    // Public Instance Methods --------------------------------------------------------------------

    /**
     * Returns main version.
     *
     * @return main version
     */
    public int getMainVersion()
    {
      return mainVersion;
    }

    /**
     * Returns beta version.
     *
     * @return beta version
     */
    public int getBetaVersion()
    {
      return betaVersion;
    }

    /**
     * Returns alpha version.
     *
     * @return alpha version
     */
    public int getAlphaVersion()
    {
      return alphaVersion;
    }

    /**
     * Returns build version.
     *
     * @return build version
     */
    public int getBuild()
    {
      return build;
    }
  }
}
