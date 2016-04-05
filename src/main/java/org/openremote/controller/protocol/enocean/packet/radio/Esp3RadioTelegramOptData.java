package org.openremote.controller.protocol.enocean.packet.radio;

import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.InvalidDeviceIDException;

/**
 * Represents the optional data group of a common ESP3 radio telegram.
 *
 * The EnOcean Serial Protocol 3 specification chapter 1.7: Packet Type 1:RADIO defines
 * the optional data group structure for all radio telegrams as follows:
 *
 * <pre>
 *    |--------------- optional data ---------------|
 *    +---------+-------...-------+--------+--------+
 *    |SubTelNum|   Destination   |   dBm  |Security|
 *    |         |       ID        |        | Level  |
 *    +---------+-------...-------+--------+--------+
 *      1 byte        4 bytes       1 byte   1 byte
 * </pre>
 *
 * EnOcean radio telegrams are commonly transmitted 3 times in a row. The SubTelNum field
 * contains the number of telegrams that have been received. In case of a transmit telegram, the
 * field has to be initialized with the value 3. <p>
 *
 * The destination ID field is related to ADT radio telegrams. According to the EnOcean Serial
 * Protocol specification ADT is an abbreviation for 'Addressing Destination Telegram'.
 * In case of a transmit telegram, the field has to be initialized with the
 * {@link DeviceID#ENOCEAN_BROADCAST_ID broadcast device ID}. <p>
 *
 * The dBm field contains the best receive signal strength (RSSI) value of all received
 * telegrams. In case of a transmit telegram, the field has to be initialized with the
 * value 0xFF. <p>
 *
 * The security level field indicates the type of security (0: unencrypted, n: type of
 * encryption). <p>
 *
 *
 * @see AbstractEsp3RadioTelegram
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RadioTelegramOptData
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of subtelegram number field: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_SUB_TEL_NUM_INDEX = 0x00;

  /**
   * Byte order index of destination ID field: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_DEST_ID_INDEX = 0x01;

  /**
   * Byte order index of receive signal strength (RSSI) field: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_DBM_INDEX = 0x05;

  /**
   * Byte order index of security level field: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_SECURITY_INDEX = 0x06;

  /**
   * Optional data length: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_LENGTH = 0x07;

  /**
   * Subtelegram number field initialization value if telegram has to be transmitted: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_SUB_TEL_NUM_TX = 0x03;

  /**
   * Receive signal strength (RSSI) field initialization value if telegram has to be
   * transmitted: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_DBM_TX = 0xFF;

  /**
   * Security level initialization value if telegram has to be transmitted
   * without security: {@value}
   */
  public static final int ESP3_RADIO_OPT_DATA_NO_SECURITY_TX = 0x00;

  /**
   * Optional data if radio telegram has to be transmitted.
   */
  public static final Esp3RadioTelegramOptData ESP3_RADIO_OPT_DATA_TX;

  static {
    ESP3_RADIO_OPT_DATA_TX = new Esp3RadioTelegramOptData(
        ESP3_RADIO_OPT_DATA_SUB_TEL_NUM_TX, DeviceID.ENOCEAN_BROADCAST_ID,
        ESP3_RADIO_OPT_DATA_DBM_TX, ESP3_RADIO_OPT_DATA_NO_SECURITY_TX);
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Number of subtelegrams.
   */
  private int subTelNum;

  /**
   * Destination ID.
   */
  private DeviceID destinationID;

  /**
   * Receive signal strength (RSSI).
   */
  private int dBm;

  /**
   * Security level.
   */
  private int securityLevel;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an optional data group instance with given optional data parameters.
   *
   * @param subTelNum  number of subtelegrams
   *
   * @param destID     destination ID
   *
   * @param dBm        receive signal strength
   *
   * @param secLevel   security level (0: telegram unencrypted)
   */
  private Esp3RadioTelegramOptData(int subTelNum, DeviceID destID, int dBm, int secLevel)
  {
    this.subTelNum = subTelNum;
    this.destinationID = destID;
    this.dBm = dBm;
    this.securityLevel = secLevel;
  }

  /**
   * Constructs an optional data group instance with given optional data byte array.
   *
   * @param data optional data group as byte array
   */
  public Esp3RadioTelegramOptData(byte[] data)
  {
    if(data == null)
    {
      throw new IllegalArgumentException("null optional data");
    }

    if(data.length != ESP3_RADIO_OPT_DATA_LENGTH)
    {
      throw new IllegalArgumentException("Invalid optional data length.");
    }

    this.subTelNum = data[ESP3_RADIO_OPT_DATA_SUB_TEL_NUM_INDEX] & 0xFF;

    this.dBm = data[ESP3_RADIO_OPT_DATA_DBM_INDEX] & 0xFF;

    this.securityLevel = data[ESP3_RADIO_OPT_DATA_SECURITY_INDEX] & 0xFF;

    byte[] destIDBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH];
    System.arraycopy(
        data, ESP3_RADIO_OPT_DATA_DEST_ID_INDEX,
        destIDBytes, 0, DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    this.destinationID = DeviceID.fromByteArray(destIDBytes);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the optional data group as a byte array.
   *
   * @return optional data group as byte array
   */
  public byte[] asByteArray()
  {
    byte[] optionalDataBytes = new byte[ESP3_RADIO_OPT_DATA_LENGTH];

    optionalDataBytes[ESP3_RADIO_OPT_DATA_SUB_TEL_NUM_INDEX] = (byte)subTelNum;

    optionalDataBytes[ESP3_RADIO_OPT_DATA_DBM_INDEX] = (byte)dBm;

    optionalDataBytes[ESP3_RADIO_OPT_DATA_SECURITY_INDEX] = (byte)securityLevel;

    System.arraycopy(
        destinationID.asByteArray(), 0,
        optionalDataBytes, ESP3_RADIO_OPT_DATA_DEST_ID_INDEX,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return optionalDataBytes;
  }

  /**
   * Returns number of subtelegrams.
   *
   * @return number of subtelegrams
   */
  public int getSubTelNum()
  {
    return subTelNum;
  }

  /**
   * Returns destination ID.
   *
   * @return destination ID
   */
  public DeviceID getDestinationID()
  {
    return destinationID;
  }

  /**
   * Returns receive signal strength (RSSI) value.
   *
   * @return receive signal strength value
   */
  public int getdBm()
  {
    return dBm;
  }

  /**
   * Returns the security level.
   *
   * @return  security level (0: telegram unencrypted)
   */
  public int getSecurityLevel()
  {
    return securityLevel;
  }
}
