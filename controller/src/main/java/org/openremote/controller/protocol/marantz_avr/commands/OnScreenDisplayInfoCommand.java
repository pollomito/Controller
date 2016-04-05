package org.openremote.controller.protocol.marantz_avr.commands;

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.marantz_avr.CommandConfig;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRCommand;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRCommandBuilder;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway.MarantzResponse;
import org.openremote.controller.utils.Logger;

/**
 * Specific command to handle return of OSD information.
 * 
 * OSD_INFO_TEXT returns the text of the line and supports Custom sensor only.
 * OSD_INFO_SELECTED returns the cursor/selection information and supports Switch sensor only.
 * The parameter is used to indicate the line of information to retrieve, from 0 to 8.
 * 
 * This command does NOT support zones.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class OnScreenDisplayInfoCommand extends MarantzAVRCommand implements ExecutableCommand, EventListener {

      /**
       * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
       */
      protected final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

      public static OnScreenDisplayInfoCommand createCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
         // Check for mandatory attributes
         if (parameter == null) {
           throw new NoSuchCommandException("A parameter is always required for the OSD info command.");
         }

         return new OnScreenDisplayInfoCommand(name, gateway, parameter);
       }

      public OnScreenDisplayInfoCommand(String name, MarantzAVRGateway gateway, String parameter) {
         super(name, gateway);
         this.parameter = parameter;
      }

      // Private Instance Fields ----------------------------------------------------------------------

      /**
       * Parameter used by this command.
       */
      private String parameter;

      // Implements ExecutableCommand -----------------------------------------------------------------

      /**
       * {@inheritDoc}
       */
      public void send() {
        // Only supported command is the request of information
        gateway.sendCommand("NSE",  "");
      }

      // Implements EventListener -------------------------------------------------------------------

      @Override
      public void setSensor(Sensor sensor) {
          if (sensors.isEmpty()) {
             
             // First sensor registered, we also need to register ourself with the gateway
             gateway.registerCommand("NSE", this);
             addSensor(sensor);

             // Trigger a query to get the initial value
             send();
          } else {
             addSensor(sensor);
          }
      }
      
      @Override
      public void stop(Sensor sensor) {
         removeSensor(sensor);
         if (sensors.isEmpty()) {
            // Last sensor removed, we may unregister ourself from gateway
            gateway.unregisterCommand("NSE", this);
         }
      }
      
      @Override
      protected void updateWithResponse(MarantzResponse response)
      {
         if (response.parameter.length() < 1) {
            return;
         }
         String lineNumber = response.parameter.substring(0, 1); 
         
         // Just pass info if line matches
         if (parameter.equals(lineNumber)) {
            
            int startOfInfo = 2;
            boolean lineSelected = false;
            
            // Line 0 doesn't have a "prefix" information in 1st byte
            if ("0".equals(parameter)) {
               startOfInfo = 1;
            } else {
               char statusByte = response.parameter.charAt(1);
               if ((statusByte & 0x08) == 0x08) { // bit 4 is line selection
                  lineSelected = true;
               }
            }
            
            // All strings are zero terminated, search for it
            int endOfString = response.parameter.indexOf((int)0);
            
            if ("OSD_LINE_TEXT".equals(name)) {
               // Pass information to sensor
               if (endOfString > startOfInfo) {
                  updateSensorsWithValue(response.parameter.substring(startOfInfo, endOfString));
               } else {
                  // end of string not found, consider an empty line
                  updateSensorsWithValue("-"); // Update needs a char, empty string gets turned to N/A somewhere in the sensor processing chain 
               }
            } else if ("OSD_LINE_SELECTED".equals(name)) {
               updateSensorsWithValue(lineSelected);
            }
         }
      }
      
      @Override
      protected void updateSensorWithValue(Sensor sensor, Object value) {
         if ("OSD_LINE_TEXT".equals(name)) {
            // Text, only support Custom sensor and pass string
            if (sensor instanceof StateSensor && !(sensor instanceof SwitchSensor)) { // SwitchSensor not supported
               sensor.update((String)value);
            } else {
               log.warn("Query value for incompatible sensor type (" + sensor + ")");
            }
         } else if ("OSD_LINE_SELECTED".equals(name)) {
            // Selected flag, only support Switch sensor
            if (sensor instanceof SwitchSensor) {
               sensor.update((Boolean)value?"on":"off");
            } else {
               log.warn("Query value for incompatible sensor type (" + sensor + ")");
            }
         }
      }
}
