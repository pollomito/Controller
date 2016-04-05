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
package org.openremote.controller.protocol.enocean.port;

import gnu.io.*;
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.PortException;
import org.openremote.controller.protocol.port.pad.AbstractPort;
import org.openremote.controller.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Serial port implementation based on the native library RXTX
 * (see http://rxtx.qbang.org/).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class RXTXPort implements Port
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Amount of time in milliseconds to wait until the RXTX serial port has been opened: {@value}
   */
  public static final int RXTX_PORT_OPEN_TIMEOUT = 2000;

  /**
   * Amount of time in milliseconds to wait until data has been received: {@value}
   */
  public static final int RXTX_PORT_RECEIVE_TIMEOUT = 100;

  /**
   * Number of bytes allocated for the receive buffer: {@value}
   */
  public static final int RXTX_PORT_RECEIVE_BUFFER_SIZE = 4096;

  /**
   * Number of bytes allocated for the transmit buffer: {@value}
   */
  public static final int RXTX_PORT_TRANSMIT_BUFFER_SIZE = 1024;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * RXTX com port identifier.
   */
  private CommPortIdentifier portIdentifier;

  /**
   * RXTX serial port.
   */
  private SerialPort serialPort;

  /**
   * Input stream for receiving data from the serial port.
   */
  private InputStream inputStream;

  /**
   * Output stream for sending data to the serial port.
   */
  private OutputStream outputStream;

  /**
   * Queue used as inter-thread communication mechanism for receiving
   * data from the serial port event listener.
   */
  private BlockingQueue<Byte> inputQueue;

  /**
   * Queue used as inter-thread communication mechanism for sending data
   * to the serial port writer thread.
   */
  private BlockingQueue<Byte> outputQueue;

  /**
   * Thread for sending data to the serial port.
   */
  private Thread writerThread;


  /**
   * Thread for reading data from serial port.
   */
  private Thread readerThread;

  /**
   * Serial port configuration.
   */
  private Map<String, Object> configuration;


  // Implements Port ------------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void configure(Map<String, Object> configuration) throws IOException, PortException
  {
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  @Override public void start() throws IOException, PortException
  {
    String portName = (String)configuration.get(AbstractPort.PORT_ID);
    String speedAsString = (String)configuration.get(AbstractPort.PORT_SPEED);

    try
    {
      portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
    }
    catch (NoSuchPortException e)
    {
      log.error("Error while configuring serial port : {0}", e, e.getMessage());

      throw new PortException(PortException.INVALID_CONFIGURATION);
    }

    CommPort commPort = null;

    try
    {
      commPort = portIdentifier.open(this.getClass().getName(), RXTX_PORT_OPEN_TIMEOUT);
    }
    catch (PortInUseException e)
    {
      log.error("Error while configuring serial port : {0}", e, e.getMessage());

      throw new PortException(PortException.INVALID_CONFIGURATION);
    }

    if(commPort instanceof SerialPort)
    {
      serialPort = (SerialPort)commPort;
    }
    else
    {
      log.error(
          "Error while configuring port ''{0}'' because it''s not a serial port type.", portName
      );

      throw new PortException(PortException.INVALID_CONFIGURATION);
    }

    try
    {
      serialPort.setSerialPortParams(
          Integer.parseInt(speedAsString), SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1, SerialPort.PARITY_NONE
      );
    }
    catch (UnsupportedCommOperationException e)
    {
      log.error("Error while configuring serial port : {0}", e, e.getMessage());

      throw new PortException(PortException.INVALID_CONFIGURATION);
    }

    try
    {
      serialPort.enableReceiveTimeout(RXTX_PORT_RECEIVE_TIMEOUT);
    }
    catch (UnsupportedCommOperationException e)
    {
      log.warn("Error while configuring serial port : {0}", e, e.getMessage());
    }

    inputQueue = new LinkedBlockingQueue<Byte>(RXTX_PORT_RECEIVE_BUFFER_SIZE);
    outputQueue = new LinkedBlockingQueue<Byte>(RXTX_PORT_TRANSMIT_BUFFER_SIZE);

    inputStream = serialPort.getInputStream();
    outputStream = serialPort.getOutputStream();


    // TODO : OpenRemoteRuntime.createThread

    writerThread = new Thread(new SerialWriter(outputStream, outputQueue), "RXTX port writer");
    writerThread.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler()
        {
          @Override public void uncaughtException(Thread t, Throwable e)
          {
            log.error(
                "Thread ''{0}'' terminated with exception : {1}",
                e, t.getName(), e.getMessage()
            );
          }
        }
    );

    writerThread.start();

    readerThread = new Thread(new SerialReader(inputStream, inputQueue), "RXTX port reader");
    readerThread.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler()
        {
          @Override public void uncaughtException(Thread t, Throwable e)
          {
            log.error(
                "Thread ''{0}'' terminated with exception : {1}",
                e, t.getName(), e.getMessage()
            );
          }
        }
    );

    readerThread.start();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void stop() throws IOException, PortException
  {
    if(writerThread != null)
    {
      writerThread.interrupt();

      try
      {
        writerThread.join();
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
    }

    if(readerThread != null)
    {
      readerThread.interrupt();

      try
      {
        readerThread.join();
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
    }

    if(outputStream != null)
    {
      outputStream.close();
    }

    if(inputStream != null)
    {
      inputStream.close();
    }

    if(serialPort != null)
    {
      serialPort.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public void send(Message message) throws IOException, PortException
  {
    byte[] data = message.getContent();

    if(data == null)
    {
      return;
    }

    for(byte dataByte : data)
    {
      try
      {
        outputQueue.put(dataByte);
      }

      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public Message receive() throws IOException
  {
    byte[] data = null;
    int size = inputQueue.size();

    try
    {
      if(size > 0)
      {
        data = new byte[size];

        for(int i = 0; i < size; i++)
        {
          data[i] = inputQueue.take();
        }
      }

      else
      {
        byte dataByte = inputQueue.take();

        size = inputQueue.size();

        if(size > 0)
        {
          data = new byte[size + 1];
          data[0] = dataByte;

          for(int i = 0; i < size; i++)
          {
            data[i + 1] = inputQueue.take();
          }
        }

        else
        {
          data = new byte[1];
          data[0] = dataByte;
        }
      }
    }

    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();

      return new Message(new byte[0]);
    }

    Message msg = new Message(data);

    return msg;
  }


  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Thread implementation for sending data to the serial port.
   */
  private static class SerialWriter implements Runnable
  {

    // Private Instance Fields --------------------------------------------------------------------

    /**
     * Output stream for sending data to serial port.
     */
    private OutputStream outputStream;

    /**
     * Queue used as inter-thread communication mechanism for transferring
     * data bytes to the writer thread.
     */
    private BlockingQueue<Byte> outputQueue;


    // Constructors -------------------------------------------------------------------------------

    /**
     * Constructs a new serial writer instance with given output stream and data queue.
     *
     * @param stream  output stream for sending data to the serial port
     *
     * @param queue   queue for transferring data to the serial writer thread
     */
    public SerialWriter(OutputStream stream, BlockingQueue<Byte> queue)
    {
      this.outputStream = stream;
      this.outputQueue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void run()
    {
      byte dataByte;

      while(true)
      {
        try
        {
          dataByte = outputQueue.take();
        }

        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
          break;
        }

        try
        {
          outputStream.write(dataByte);
        }
        catch (IOException e)
        {
          log.error(
              "Thread ''{0}'' terminated with exception : {1}",
              e, Thread.currentThread().getName(), e.getMessage());

          break;
        }
      }
    }
  }

  /**
   * Thread implementation for sending data to the serial port.
   */
  private static class SerialReader implements Runnable
  {

    // Private Instance Fields --------------------------------------------------------------------

    /**
     * Input stream for reading data fom the serial port.
     */
    private InputStream inputStream;

    /**
     * Queue used as inter-thread communication mechanism for transferring
     * data bytes from reader thread to a different worker thread.
     */
    private BlockingQueue<Byte> inputQueue;


    // Constructors -------------------------------------------------------------------------------

    /**
     * Constructs a serial port reader instance with given input stream and queue.
     *
     * @param stream  input stream for reading data from the serial port
     *
     * @param queue   queue for transferring data from the serial port reader thread
     *                to a different worker thread.
     */
    public SerialReader(InputStream stream, BlockingQueue<Byte> queue)
    {
      this.inputStream = stream;
      this.inputQueue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void run()
    {
      byte[] buffer = new byte[1024];
      int len = -1;

      try
      {
        while ((len = inputStream.read(buffer)) > -1)
        {
          if(Thread.currentThread().isInterrupted())
          {
            break;
          }

          try
          {
            for(int i = 0; i < len; i++)
            {
              inputQueue.put(buffer[i]);
            }
          }

          catch (InterruptedException e)
          {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }

      catch(IOException e)
      {
        log.error(
            "Thread ''{0}'' terminated with exception : {1}",
            e, Thread.currentThread().getName(), e.getMessage()
        );
      }
    }
  }
}
