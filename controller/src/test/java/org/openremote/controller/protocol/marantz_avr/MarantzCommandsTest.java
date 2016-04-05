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
package org.openremote.controller.protocol.marantz_avr;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.exception.NoSuchCommandException;

/**
 * Tests validating the Marantz commands do generate the appropriate strings to send to device.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MarantzCommandsTest {

   private MockMarantzGateway gateway;
   
   @Before
   public void setUpGateway() {
      gateway = new MockMarantzGateway();
   }
   
   @Test
   public void testMainPowerCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_POWER", gateway, "ON", null);
      cmd.send();
      Assert.assertEquals("MAIN_POWER ON should send PWON", "PWON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_POWER", gateway, "OFF", null);
      cmd.send();
      Assert.assertEquals("MAIN_POWER OFF should send PWOFF", "PWSTANDBY", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_POWER", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("MAIN_POWER STATUS should send PW?", "PW?", gateway.getSentString());
   }
   
   @Test
   public void testPowerCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "ON", null);
      cmd.send();
      Assert.assertEquals("POWER ON with no zone specified should send ZMON", "ZMON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "OFF", null);
      cmd.send();
      Assert.assertEquals("POWER OFF with no zone specified should send ZMOFF", "ZMOFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("POWER STATUS with no zone specified should send ZM?", "ZM?", gateway.getSentString());
      gateway.reset();
      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "ON", "MAIN");
      cmd.send();
      Assert.assertEquals("POWER ON for main zone should send ZMON", "ZMON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "OFF", "MAIN");
      cmd.send();
      Assert.assertEquals("POWER OFF for main zone should send ZMOFF", "ZMOFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "STATUS", "MAIN");
      cmd.send();
      Assert.assertEquals("POWER STATUS for main zone should send ZM?", "ZM?", gateway.getSentString());
      gateway.reset();
      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "ON", "ZONE2");
      cmd.send();
      Assert.assertEquals("POWER ON for zone 2 should send Z2ON", "Z2ON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "OFF", "ZONE2");
      cmd.send();
      Assert.assertEquals("POWER OFF for zone 2 should send Z2OFF", "Z2OFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "STATUS", "ZONE2");
      cmd.send();
      Assert.assertEquals("POWER STATUS for zone 2 should send Z2?", "Z2?", gateway.getSentString());
      gateway.reset();

      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "ON", "ZONE3");
      cmd.send();
      Assert.assertEquals("POWER ON for zone 3 should send Z3ON", "Z3ON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "OFF", "ZONE3");
      cmd.send();
      Assert.assertEquals("POWER OFF for zone 3 should send Z3OFF", "Z3OFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("POWER", gateway, "STATUS", "ZONE3");
      cmd.send();
      Assert.assertEquals("POWER STATUS for zone 3 should send Z3?", "Z3?", gateway.getSentString());
   }
   
   @Test
   public void testMuteCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "ON", null);
      cmd.send();
      Assert.assertEquals("MUTE ON with no zone specified should send MUON", "MUON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "OFF", null);
      cmd.send();
      Assert.assertEquals("MUTE OFF with no zone specified should send MUOFF", "MUOFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("MUTE STATUS with no zone specified should send MU?", "MU?", gateway.getSentString());
      gateway.reset();
      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "ON", "MAIN");
      cmd.send();
      Assert.assertEquals("MUTE ON for main zone should send MUON", "MUON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "OFF", "MAIN");
      cmd.send();
      Assert.assertEquals("MUTE OFF for main zone should send MUOFF", "MUOFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "STATUS", "MAIN");
      cmd.send();
      Assert.assertEquals("MUTE STATUS for main zone should send MU?", "MU?", gateway.getSentString());
      gateway.reset();
      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "ON", "ZONE2");
      cmd.send();
      Assert.assertEquals("MUTE ON for zone 2 should send Z2MUON", "Z2MUON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "OFF", "ZONE2");
      cmd.send();
      Assert.assertEquals("MUTE OFF for zone 2 should send Z2MUOFF", "Z2MUOFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "STATUS", "ZONE2");
      cmd.send();
      Assert.assertEquals("MUTE STATUS for zone 2 should send Z2MU?", "Z2MU?", gateway.getSentString());
      gateway.reset();

      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "ON", "ZONE3");
      cmd.send();
      Assert.assertEquals("MUTE ON for zone 3 should send Z3MUON", "Z3MUON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "OFF", "ZONE3");
      cmd.send();
      Assert.assertEquals("MUTE OFF for zone 3 should send Z3MUOFF", "Z3MUOFF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MUTE", gateway, "STATUS", "ZONE3");
      cmd.send();
      Assert.assertEquals("MUTE STATUS for zone 3 should send Z3MU?", "Z3MU?", gateway.getSentString());
   }   
   
   @Test
   public void testInputCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "PHONO", null);
      cmd.send();
      Assert.assertEquals("INPUT PHONO with no zone specified should send SIPHONO", "SIPHONO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "CD", null);
      cmd.send();
      Assert.assertEquals("INPUT CD with no zone specified should send SICD", "SICD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "DVD", null);
      cmd.send();
      Assert.assertEquals("INPUT DVD with no zone specified should send SIDVD", "SIDVD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "BD", null);
      cmd.send();
      Assert.assertEquals("INPUT BD with no zone specified should send SIBD", "SIBD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TV", null);
      cmd.send();
      Assert.assertEquals("INPUT TV with no zone specified should send SITV", "SITV", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SAT/CBL", null);
      cmd.send();
      Assert.assertEquals("INPUT SAT/CBL with no zone specified should send SISAT/CBL", "SISAT/CBL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "VCR", null);
      cmd.send();
      Assert.assertEquals("INPUT VCR with no zone specified should send SIVCR", "SIVCR", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "GAME", null);
      cmd.send();
      Assert.assertEquals("INPUT GAME with no zone specified should send SIGAME", "SIGAME", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "V.AUX", null);
      cmd.send();
      Assert.assertEquals("INPUT V.AUX with no zone specified should send SIV.AUX", "SIV.AUX", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX1", null);
      cmd.send();
      Assert.assertEquals("INPUT AUX1 with no zone specified should send SIAUX1", "SIAUX1", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX2", null);
      cmd.send();
      Assert.assertEquals("INPUT AUX2 with no zone specified should send SIAUX2", "SIAUX2", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRADIO", null);
      cmd.send();
      Assert.assertEquals("INPUT IRADIO with no zone specified should send SIIRADIO", "SIIRADIO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TUNER", null);
      cmd.send();
      Assert.assertEquals("INPUT TUNER with no zone specified should send SITUNER", "SITUNER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "MEDIAPLAYER", null);
      cmd.send();
      Assert.assertEquals("INPUT MEDIAPLAYER with no zone specified should send SIMPLAY", "SIMPLAY", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "LASTFM", null);
      cmd.send();
      Assert.assertEquals("INPUT LASTFM with no zone specified should send SILASTFM", "SILASTFM", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FAVORITES", null);
      cmd.send();
      Assert.assertEquals("INPUT FAVORITES with no zone specified should send SIFAVORITES", "SIFAVORITES", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SERVER", null);
      cmd.send();
      Assert.assertEquals("INPUT SERVER with no zone specified should send SISERVER", "SISERVER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB/IPOD", null);
      cmd.send();
      Assert.assertEquals("INPUT USB/IPOD with no zone specified should send SIUSB/IPOD", "SIUSB/IPOD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB", null);
      cmd.send();
      Assert.assertEquals("INPUT USB with no zone specified should send SIUSB", "SIUSB", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IPOD", null);
      cmd.send();
      Assert.assertEquals("INPUT IPOD with no zone specified should send SIIPD", "SIIPD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRP", null);
      cmd.send();
      Assert.assertEquals("INPUT IRP with no zone specified should send SIIRP", "SIIRP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FVP", null);
      cmd.send();
      Assert.assertEquals("INPUT FVP with no zone specified should send SIFVP", "SIFVP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "NETWORK", null);
      cmd.send();
      Assert.assertEquals("INPUT NETWORK with no zone specified should send SINET", "SINET", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("INPUT STATUS with no zone specified should send SI?", "SI?", gateway.getSentString());
      gateway.reset();

      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "PHONO", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT PHONO for main zone should send SIPHONO", "SIPHONO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "CD", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT CD for main zone should send SICD", "SICD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "DVD", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT DVD for main zone should send SIDVD", "SIDVD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "BD", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT BD for main zone should send SIBD", "SIBD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TV", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT TV for main zone should send SITV", "SITV", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SAT/CBL", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT SAT/CBL for main zone should send SISAT/CBL", "SISAT/CBL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "VCR", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT VCR for main zone should send SIVCR", "SIVCR", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "GAME", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT GAME for main zone should send SIGAME", "SIGAME", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "V.AUX", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT V.AUX for main zone should send SIV.AUX", "SIV.AUX", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX1", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT AUX1 for main zone should send SIAUX1", "SIAUX1", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX2", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT AUX2 for main zone should send SIAUX2", "SIAUX2", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRADIO", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT IRADIO for main zone should send SIIRADIO", "SIIRADIO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TUNER", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT TUNER for main zone should send SITUNER", "SITUNER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "MEDIAPLAYER", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT MEDIAPLAYER for main zone should send SIMPLAY", "SIMPLAY", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "LASTFM", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT LASTFM for main zone should send SILASTFM", "SILASTFM", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FAVORITES", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT FAVORITES for main zone should send SIFAVORITES", "SIFAVORITES", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SERVER", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT SERVER for main zone should send SISERVER", "SISERVER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB/IPOD", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT USB/IPOD for main zone should send SIUSB/IPOD", "SIUSB/IPOD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT USB for main zone should send SIUSB", "SIUSB", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IPOD", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT IPOD for main zone should send SIIPD", "SIIPD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRP", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT IRP for main zone should send SIIRP", "SIIRP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FVP", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT FVP for main zone should send SIFVP", "SIFVP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "NETWORK", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT NETWORK for main zone should send SINET", "SINET", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "STATUS", "MAIN");
      cmd.send();
      Assert.assertEquals("INPUT STATUS for main zone should send SI?", "SI?", gateway.getSentString());
      gateway.reset();

      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "PHONO", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT PHONO for zone 2 should send Z2PHONO", "Z2PHONO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "CD", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT CD for zone 2 should send Z2CD", "Z2CD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "DVD", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT DVD for zone 2 should send Z2DVD", "Z2DVD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "BD", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT BD for zone 2 should send Z2BD", "Z2BD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TV", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT TV for zone 2 should send Z2TV", "Z2TV", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SAT/CBL", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT SAT/CBL for zone 2 should send Z2SAT/CBL", "Z2SAT/CBL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "VCR", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT VCR for zone 2 should send Z2VCR", "Z2VCR", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "GAME", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT GAME for zone 2 should send Z2GAME", "Z2GAME", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "V.AUX", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT V.AUX for zone 2 should send Z2V.AUX", "Z2V.AUX", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX1", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT AUX1 for zone 2 should send Z2AUX1", "Z2AUX1", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX2", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT AUX2 for zone 2 should send Z2AUX2", "Z2AUX2", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRADIO", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT IRADIO for zone 2 should send Z2IRADIO", "Z2IRADIO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TUNER", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT TUNER for zone 2 should send Z2TUNER", "Z2TUNER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "MEDIAPLAYER", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT MEDIAPLAYER for zone 2 should send Z2MPLAY", "Z2MPLAY", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "LASTFM", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT LASTFM for zone 2 should send Z2LASTFM", "Z2LASTFM", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FAVORITES", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT FAVORITES for zone 2 should send Z2FAVORITES", "Z2FAVORITES", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SERVER", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT SERVER for zone 2 should send Z2SERVER", "Z2SERVER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB/IPOD", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT USB/IPOD for zone 2 should send Z2USB/IPOD", "Z2USB/IPOD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT USB for zone 2 should send Z2USB", "Z2USB", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IPOD", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT IPOD for zone 2 should send Z2IPD", "Z2IPD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRP", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT IRP for zone 2 should send Z2IRP", "Z2IRP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FVP", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT FVP for zone 2 should send Z2FVP", "Z2FVP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "NETWORK", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT NETWORK for zone 2 should send Z2NET", "Z2NET", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "STATUS", "ZONE2");
      cmd.send();
      Assert.assertEquals("INPUT STATUS for zone 2 should send Z2?", "Z2?", gateway.getSentString());
      gateway.reset();
      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "PHONO", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT PHONO for zone 3 should send Z3PHONO", "Z3PHONO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "CD", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT CD for zone 3 should send Z3CD", "Z3CD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "DVD", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT DVD for zone 3 should send Z3DVD", "Z3DVD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "BD", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT BD for zone 3 should send Z3BD", "Z3BD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TV", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT TV for zone 3 should send Z3TV", "Z3TV", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SAT/CBL", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT SAT/CBL for zone 3 should send Z3SAT/CBL", "Z3SAT/CBL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "VCR", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT VCR for zone 3 should send Z3VCR", "Z3VCR", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "GAME", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT GAME for zone 3 should send Z3GAME", "Z3GAME", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "V.AUX", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT V.AUX for zone 3 should send Z3V.AUX", "Z3V.AUX", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX1", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT AUX1 for zone 3 should send Z3AUX1", "Z3AUX1", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "AUX2", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT AUX2 for zone 3 should send Z3AUX2", "Z3AUX2", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRADIO", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT IRADIO for zone 3 should send Z3IRADIO", "Z3IRADIO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "TUNER", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT TUNER for zone 3 should send Z3TUNER", "Z3TUNER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "MEDIAPLAYER", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT MEDIAPLAYER for zone 3 should send Z3MPLAY", "Z3MPLAY", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "LASTFM", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT LASTFM for zone 3 should send Z3LASTFM", "Z3LASTFM", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FAVORITES", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT FAVORITES for zone 3 should send Z3FAVORITES", "Z3FAVORITES", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "SERVER", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT SERVER for zone 3 should send Z3SERVER", "Z3SERVER", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB/IPOD", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT USB/IPOD for zone 3 should send Z3USB/IPOD", "Z3USB/IPOD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "USB", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT USB for zone 3 should send Z3USB", "Z3USB", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IPOD", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT IPOD for zone 3 should send Z3IPD", "Z3IPD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "IRP", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT IRP for zone 3 should send Z3IRP", "Z3IRP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "FVP", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT FVP for zone 3 should send Z3FVP", "Z3FVP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "NETWORK", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT NETWORK for zone 3 should send Z3NET", "Z3NET", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("INPUT", gateway, "STATUS", "ZONE3");
      cmd.send();
      Assert.assertEquals("INPUT STATUS for zone 3 should send Z3?", "Z3?", gateway.getSentString());
   }
   
   @Test
   public void testTunerFrequencyCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_FREQUENCY", gateway, "UP", null);
      cmd.send();
      Assert.assertEquals("TUNER_FREQUENCY UP should send TFANUP", "TFANUP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_FREQUENCY", gateway, "DOWN", null);
      cmd.send();
      Assert.assertEquals("TUNER_FREQUENCY DOWN should send TFANDOWN", "TFANDOWN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_FREQUENCY", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("TUNER_FREQUENCY STATUS should send TFAN?", "TFAN?", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_FREQUENCY", gateway, "105000", null);
      cmd.send();
      Assert.assertEquals("TUNER_FREQUENCY 105000 should send TFAN105000", "TFAN105000", gateway.getSentString());
   }

   // EBR : test is disabled for now as validation is not yet implemented
   //@Test(expected=NoSuchCommandException.class)
   public void testInvalidTunerFrequencyParameterCommand() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_FREQUENCY", gateway, "INVALID", null);
      cmd.send();
   }

   @Test
   public void testTunerPresetCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_PRESET", gateway, "UP", null);
      cmd.send();
      Assert.assertEquals("TUNER_PRESET UP should send TPANUP", "TPANUP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_PRESET", gateway, "DOWN", null);
      cmd.send();
      Assert.assertEquals("TUNER_PRESET DOWN should send TPANDOWN", "TPANDOWN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_PRESET", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("TUNER_PRESET STATUS should send TPAN?", "TPAN?", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("TUNER_PRESET", gateway, "A1", null);
      cmd.send();
      Assert.assertEquals("TUNER_PRESET A1 should send TPANA1", "TPANA1", gateway.getSentString());
   }

   @Test
   public void testOSDTextCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("OSD_LINE_TEXT", gateway, "0", null);
      cmd.send();
      Assert.assertEquals("OSD_LINE_TEXT 0 should send NSE", "NSE", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("OSD_LINE_TEXT", gateway, "8", null);
      cmd.send();
      Assert.assertEquals("OSD_LINE_TEXT 8 should send NSE", "NSE", gateway.getSentString());
   }
   
   @Test
   public void testOSDSelectedCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("OSD_LINE_SELECTED", gateway, "0", null);
      cmd.send();
      Assert.assertEquals("OSD_LINE_SELECTED 0 should send NSE", "NSE", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("OSD_LINE_SELECTED", gateway, "8", null);
      cmd.send();
      Assert.assertEquals("OSD_LINE_SELECTED 8 should send NSE", "NSE", gateway.getSentString());
   }

   @Test
   public void testSurroundModeCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "MOVIE", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE MOVIE should send MSMOVIE", "MSMOVIE", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "MUSIC", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE MUSIC should send MSMUSIC", "MSMUSIC", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "GAME", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE GAME should send MSGAME", "MSGAME", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "DIRECT", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE DIRECT should send MSDIRECT", "MSDIRECT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "PURE DIRECT", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE PURE DIRECT should send MSPURE DIRECT", "MSPURE DIRECT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "STEREO", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE STEREO should send MSSTEREO", "MSSTEREO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "AUTO", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE AUTO should send MSAUTO", "MSAUTO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "NEURAL", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE NEURAL should send MSNEURAL", "MSNEURAL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "STANDARD", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE STANDARD should send MSSTANDARD", "MSSTANDARD", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "DOLBY", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE DOLBY should send MSDOLBY DIGITAL", "MSDOLBY DIGITAL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "DTS", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE DTS should send MSDTS SURROUND", "MSDTS SURROUND", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "MCH STEREO", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE MCH STEREO should send MSMCH STEREO", "MSMCH STEREO", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "MATRIX", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE MATRIX should send MSMATRIX", "MSMATRIX", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "VIRTUAL", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE VIRTUAL should send MSVIRTUAL", "MSVIRTUAL", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "LEFT", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE LEFT should send MSLEFT", "MSLEFT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "RIGHT", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE RIGHT should send MSRIGHT", "MSRIGHT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("SURROUND_MODE", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("SURROUND_MODE STATUS should send MS?", "MS?", gateway.getSentString());
   }

   @Test
   public void testVolumeCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "UP", null);
      cmd.send();
      Assert.assertEquals("VOLUME UP with no zone specified should send MVUP", "MVUP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "DOWN", null);
      cmd.send();
      Assert.assertEquals("VOLUME DOWN with no zone specified should send MVDOWN", "MVDOWN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "STATUS", null);
      cmd.send();
      Assert.assertEquals("VOLUME STATUS with no zone specified should send MV?", "MV?", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42.5", null);
      cmd.send();
      Assert.assertEquals("VOLUME 42.5 with no zone specified should send MV425", "MV425", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42", null);
      cmd.send();
      Assert.assertEquals("VOLUME 42 with no zone specified should send MV420", "MV420", gateway.getSentString());
      gateway.reset();      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "7", null);
      cmd.send();
      Assert.assertEquals("VOLUME 7 with no zone specified should send MV070", "MV070", gateway.getSentString());
      gateway.reset();

      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "UP", "MAIN");
      cmd.send();
      Assert.assertEquals("VOLUME UP for main zone should send MVUP", "MVUP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "DOWN", "MAIN");
      cmd.send();
      Assert.assertEquals("VOLUME DOWN for main zone should send MVDOWN", "MVDOWN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "STATUS", "MAIN");
      cmd.send();
      Assert.assertEquals("VOLUME STATUS for main zone should send MV?", "MV?", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42.5", "MAIN");
      cmd.send();
      Assert.assertEquals("VOLUME 42.5 for main zone should send MV425", "MV425", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42", "MAIN");
      cmd.send();
      Assert.assertEquals("VOLUME 42 for main zone should send MV420", "MV420", gateway.getSentString());
      gateway.reset();      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "7", "MAIN");
      cmd.send();
      Assert.assertEquals("VOLUME 7 for main zone should send MV070", "MV070", gateway.getSentString());
      gateway.reset();
      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "UP", "ZONE2");
      cmd.send();
      Assert.assertEquals("VOLUME UP for zone 2 should send Z2UP", "Z2UP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "DOWN", "ZONE2");
      cmd.send();
      Assert.assertEquals("VOLUME DOWN for zone 2 should send Z2DOWN", "Z2DOWN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "STATUS", "ZONE2");
      cmd.send();
      Assert.assertEquals("VOLUME STATUS for zone 2 should send Z2?", "Z2?", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42.5", "ZONE2");
      cmd.send();
      Assert.assertEquals("VOLUME 42.5 for zone 2 should send Z243", "Z243", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42", "ZONE2");
      cmd.send();
      Assert.assertEquals("VOLUME 42 for zone 2 should send Z242", "Z242", gateway.getSentString());
      gateway.reset();      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "7", "ZONE2");
      cmd.send();
      Assert.assertEquals("VOLUME 7 for zone 2 should send Z207", "Z207", gateway.getSentString());
      gateway.reset();

      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "UP", "ZONE3");
      cmd.send();
      Assert.assertEquals("VOLUME UP for zone 3 should send Z3UP", "Z3UP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "DOWN", "ZONE3");
      cmd.send();
      Assert.assertEquals("VOLUME DOWN for zone 3 should send Z3DOWN", "Z3DOWN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "STATUS", "ZONE3");
      cmd.send();
      Assert.assertEquals("VOLUME STATUS for zone 3 should send Z3?", "Z3?", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42.5", "ZONE3");
      cmd.send();
      Assert.assertEquals("VOLUME 42.5 for zone 3 should send Z343", "Z343", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "42", "ZONE3");
      cmd.send();
      Assert.assertEquals("VOLUME 42 for zone 3 should send Z342", "Z342", gateway.getSentString());
      gateway.reset();      
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "7", "ZONE3");
      cmd.send();
      Assert.assertEquals("VOLUME 7 for zone 3 should send Z307", "Z307", gateway.getSentString());
      gateway.reset();
   }

   @Test(expected=NoSuchCommandException.class)
   public void testInvalidValueParameterCommand() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("VOLUME", gateway, "INVALID", null);
      cmd.send();
   }

   @Test
   public void testMainControlCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "CURSOR UP", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL CURSOR UP should send MNCUP", "MNCUP", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "CURSOR DOWN", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL CURSOR DOWN should send MNCDN", "MNCDN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "CURSOR LEFT", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL CURSOR LEFT should send MNCLT", "MNCLT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "CURSOR RIGHT", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL CURSOR RIGHT should send MNCRT", "MNCRT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "ENTER", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL ENTER should send MNENT", "MNENT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "RETURN", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL RETURN should send MNCUP", "MNRTN", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "OPTION", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL OPTION should send MNOPT", "MNOPT", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "INFO", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL INFO should send MNINF", "MNINF", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "SETUP MENU ON", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL SETUP MENU ON should send MNMEN ON", "MNMEN ON", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("MAIN_CONTROL", gateway, "SETUP MENU OFF", null);
      cmd.send();
      Assert.assertEquals("MAIN_CONTROL SETUP MENU OFF should send MNMEN OFF", "MNMEN OFF", gateway.getSentString());
   }
   
   @Test
   public void testNetworkControlCommands() {
      ExecutableCommand cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "CURSOR UP", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL CURSOR UP should send NS90", "NS90", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "CURSOR DOWN", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL CURSOR DOWN should send NS91", "NS91", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "CURSOR LEFT", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL CURSOR LEFT should send NS92", "NS92", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "CURSOR RIGHT", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL CURSOR RIGHT should send NS93", "NS93", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "ENTER", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL ENTER should send NS94", "NS94", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "PLAY", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL PLAY should send NS9A", "NS9A", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "PAUSE", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL PAUSE should send NS9B", "NS9B", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "STOP", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL STOP should send NS9C", "NS9C", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "SKIP PLUS", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL SKIP PLUS should send NS9D", "NS9D", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "SKIP MINUS", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL SKIP MINUS should send NS9E", "NS9E", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "REPEAT ONE", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL REPEAT ONE should send NS9H", "NS9H", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "REPEAT ALL", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL REPEAT ALL should send NS9I", "NS9I", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "REPEAT OFF", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL REPEAT OFF should send NS9J", "NS9J", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "RANDOM ON", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL RANDOM ON should send NS9K", "NS9K", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "RANDOM OFF", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL RANDOM OFF should send NS9M", "NS9M", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "TOGGLE BROWSE REMOTE", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL TOGGLE BROWSE REMOTE should send NS9W", "NS9W", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "PAGE NEXT", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL PAGE NEXT should send NS9X", "NS9X", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "PAGE PREVIOUS", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL PAGE PREVIOUS should send NS9Y", "NS9Y", gateway.getSentString());
      gateway.reset();
      cmd = (ExecutableCommand) MarantzAVRCommand.createCommand("NETWORK_CONTROL", gateway, "TOGGLE PARTY MODE", null);
      cmd.send();
      Assert.assertEquals("NETWORK_CONTROL TOGGLE PARTY MODE should send NSPT", "NSPT", gateway.getSentString());
   }

}