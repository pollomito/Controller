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
package org.openremote.controller.utils;

import java.util.Locale;

/**
 * String related utilities.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author <a href="mailto:marcus@openremote.org">Marcus Redeker</a>
 */
public class Strings
{

   private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

  /**
   * Translates a signed Java byte into an unsigned hex string.
   *
   * For example:
   *
   * <pre>{@code
   *
   *    1  -> 0x01
   *   15  -> 0x0F
   *   16  -> 0x10
   *   -1  -> 0xFF
   * -128  -> 0xC0
   *
   * }</pre>
   *
   * @param b   byte to convert
   *
   * @return    Unsigned hex representation of the byte value with a '0x' prefix. Returned values
   *            are always padded to two digits, so '1' becomes '0x01'.
   */
  public static String byteToUnsignedHexString(byte b)
  {
    int value = b;

    value &= 0xFF;
    
    return ( value < 16 ? "0x0" + toUpperCase(Integer.toHexString(value))
                        : "0x"  + toUpperCase(Integer.toHexString(value)));
  }

  /**
   * Translates an array of signed Java bytes into an unsigned hex string.
   * 
   * Hex values are always padded on 2 characters, no prefix is added,
   * no spacing between the different byte values.
   *  
   * @param bytes Array of bytes to convert
   * @return Unsigned hex representations of input bytes.
   */
  public static String bytesToHex(byte[] bytes) {
     char[] hexChars = new char[bytes.length * 2];
     for ( int j = 0; j < bytes.length; j++ ) {
         int v = bytes[j] & 0xFF;
         hexChars[j * 2] = hexArray[v >>> 4];
         hexChars[j * 2 + 1] = hexArray[v & 0x0F];
     }
     return new String(hexChars);
 }
  
  /**
   * Locale independent string to upper case method. This should be used by all code related
   * string case changes to avoid issues with system default locales that can have unpredictable
   * results when string conversions are made. <p>
   *
   * String.toUpperCase() is locale dependent. This causes issue with turkmen language locale
   * (ISO-639 "tr") for example where character 'i' is not converted as expected. Therefore the
   * String.toUpperCase() should *not* be used by implementation code except where explicitly
   * locale dependent behavior is desired (mostly user-facing messages). <p>
   *
   * This implementation uses the ENGLISH locale on all conversions.
   *
   * @see <a href="http://jira.openremote.org/browse/ORCJAVA-332">ORCJAVA-332</a>
   *
   * @param str   String to convert to uppercase using ENGLISH locale.
   *
   * @return      Upper case string.
   */
  public static String toUpperCase(String str)
  {
    /*
     * IMPLEMENTATION NOTES:
     *
     *   - regression tests confirm the differing behavior between this method and
     *     String.toUpperCase() when turkmen locale is being used
     *
     *   - All use of String.toUpperCase() should delegate to this implementation instead so
     *     changes become centralized in the codebase
     *
     *   - References:
     *      http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug
     *      http://mail.openjdk.java.net/pipermail/jdk8-dev/2011-July/000060.html
     */
    return str.toUpperCase(Locale.ENGLISH);
  }

  /**
   * Locale independent string to lower case method. This should be used by all code related
   * string case changes to avoid issues with system default locales that can have unpredictable
   * results when string conversions are made. <p>
   *
   * String.toLowerCase() is locale dependent. This causes issue with turkmen language locale
   * (ISO-639 "tr") for example where character 'i' is not converted as expected. Therefore the
   * String.toLowerCase() should *not* be used by implementation code except where explicitly
   * locale dependent behavior is desired (mostly user-facing messages). <p>
   *
   * This implementation uses the ENGLISH locale on all conversions.
   *
   * @see <a href="http://jira.openremote.org/browse/ORCJAVA-332">ORCJAVA-332</a>
   *
   * @param str   String to convert to lower case using ENGLISH locale.
   *
   * @return      Lower case string.
   */
  public static String toLowerCase(String str)
  {
    /*
     * IMPLEMENTATION NOTES:
     *
     *   - regression tests confirm the differing behavior between this method and
     *     String.toLowerCase() when turkmen locale is being used
     *
     *   - All use of String.toLowerCase() should delegate to this implementation instead so
     *     changes become centralized in the codebase
     *
     *   - References:
     *      http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug
     *      http://mail.openjdk.java.net/pipermail/jdk8-dev/2011-July/000060.html
     */
    return str.toLowerCase(Locale.ENGLISH);
  }


  /**
   * Converts a String which represents a pollingInterval into an int which can be used as delay
   * for Thread.sleep();  <p>
   *
   * Following conversion will happen:  <p>
   *
   * null || "" = -1                  <br>
   * 500 = 500                        <br>
   * 1s = 1000 * 1 = 1000             <br>
   * 1m = 1000 * 60 = 60000           <br>
   * 1h = 1000 * 60 * 60 = 3600000    <br>
   * 
   * @param pollingInterval   interval string, such as '1m' as millisecond value
   *
   * @return  interval in milliseconds
   */
  public static int convertPollingIntervalString(String pollingInterval)
  {
    if ((pollingInterval != null) && (pollingInterval.trim().length() != 0))
    {
      pollingInterval = pollingInterval.trim();
      char lastChar = pollingInterval.charAt(pollingInterval.length()-1);
      String timePart = pollingInterval.substring(0, pollingInterval.length()-1);

      switch (lastChar)
      {
        case 's':
          return Integer.parseInt(timePart) * 1000;
        case 'm':
          return Integer.parseInt(timePart) * 1000 * 60;
        case 'h':
          return Integer.parseInt(timePart) * 1000 * 60 * 60;
        default:
          return Integer.parseInt(pollingInterval);
      }
    }

    return -1;
  }


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Only utility class methods.
   */
  private Strings()
  {}

}
