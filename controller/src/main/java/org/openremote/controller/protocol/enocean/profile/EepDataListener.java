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

/**
 * The listener interface for receiving EnOcean equipment profile (EEP) data events.
 *
 * @see EepData
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface EepDataListener
{
  /**
   * Invoked when the EnOcean equipment profile (EEP) data has been
   * updated.
   *
   * @param data  the EnOcean equipment profile (EEP) data instance
   *              which has been updated
   *
   * @throws EepOutOfRangeException
   *              if a data field value is out of range
   */
  void didUpdateData(EepData data) throws EepOutOfRangeException;

  /**
   * This method gets called when the EnOcean equipment profile (EEP) data has to be
   * updated.
   *
   * @param data  the EnOcean equipment profile (EEP) data instance
   *              which has to be updated
   *
   * @throws EepOutOfRangeException
   *              if a data field value cannot be stored to the EEP data because it is
   *              out of range
   */
  void updateData(EepData data) throws EepOutOfRangeException;

}
