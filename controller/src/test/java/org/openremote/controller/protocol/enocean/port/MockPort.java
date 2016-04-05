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

import junit.framework.Assert;
import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.PortException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Configurable test double for mocking a {@link org.openremote.controller.protocol.port.Port}
 * implementation.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class MockPort implements Port
{

  // Enums ----------------------------------------------------------------------------------------

  public enum Method
  {
    START,
    STOP,
    CONFIGURE
  }


  // Private Instance Fields ----------------------------------------------------------------------

  private List<Method> expectedMethodCalls = new ArrayList<Method>();
  private List<Method> actualMethodCalls = new ArrayList<Method>();

  private List<ArrayList<Byte>> expectedDataToSend = new ArrayList<ArrayList<Byte>>();
  private List<ArrayList<Byte>> actualDataToSend = new ArrayList<ArrayList<Byte>>();
  private LinkedList<LinkedList<Message>> messagesToReturn = new LinkedList<LinkedList<Message>>();

  private Exception configureException = null;
  private Exception startException = null;
  private Exception stopException = null;
  private Exception sendException = null;
  private IOException receiveException = null;

  private boolean requestResponseMode = false;

  private BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<Message>();

  // Public Instance Methods ----------------------------------------------------------------------

  public void setRequestResponseMode()
  {
    requestResponseMode = true;
  }

  public void addExpectedMethodCall(Method method)
  {
    expectedMethodCalls.add(method);
  }

  public void addExpectedDataToSend(byte[] rawData)
  {
    addData(expectedDataToSend, rawData);
  }

  public void addDataToReturn(byte[] rawData)
  {
    ArrayList<byte[]> list = new ArrayList<byte[]>();
    list.add(rawData);

    addDataToReturn(list);
  }

  public void addDataToReturn(List<byte[]> rawDataList)
  {
    LinkedList<Message> messages = new LinkedList<Message>();

    for(byte[] rawData : rawDataList)
    {
      messages.add(new Message(rawData.clone()));
    }

    messagesToReturn.addLast(messages);
  }


  public void setupThrowExceptionOnConfigure(Exception exception)
  {
    configureException = exception;
  }

  public void setupThrowExceptionOnStart(Exception exception)
  {
    startException = exception;
  }

  public void setupThrowExceptionOnStop(Exception exception)
  {
    stopException = exception;
  }

  public void setupThrowExceptionOnSend(Exception exception)
  {
    sendException = exception;
  }

  public void setupThrowExceptionOnReceive(IOException exception)
  {
    receiveException = exception;
  }


  public void verifyMethodCalls()
  {
    Assert.assertEquals(expectedMethodCalls, actualMethodCalls);
  }

  public void verifyData()
  {
    Assert.assertEquals(expectedDataToSend, actualDataToSend);

    Assert.assertEquals(messagesToReturn.size(), 0);
  }


  // Implements Port ------------------------------------------------------------------------------

  @Override public void configure(Map<String, Object> configuration) throws IOException, PortException
  {
    actualMethodCalls.add(Method.CONFIGURE);

    if(null != configureException)
    {
      triggerException(configureException);
    }
  }


  @Override public void start() throws IOException, PortException
  {
    actualMethodCalls.add(Method.START);

    if(null != startException)
    {
      triggerException(startException);
    }
  }


  @Override public void stop() throws IOException, PortException
  {
    actualMethodCalls.add(Method.STOP);

    if(null != stopException)
    {
      triggerException(stopException);
    }
  }


  @Override public void send(Message message) throws IOException, PortException
  {
    addData(actualDataToSend, message.getContent());

    if(null != sendException)
    {
      triggerException(sendException);
    }

    if(requestResponseMode && messagesToReturn.size() > 0)
    {
      LinkedList<Message> messages = messagesToReturn.removeFirst();

      try
      {
        for(Message msg : messages)
        {
          responseQueue.put(msg);
        }
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
    }
  }


  @Override public Message receive() throws IOException
  {
    Message msg = null;

    if(requestResponseMode)
    {
      try
      {
        msg = responseQueue.take();
      }

      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
    }
    else
    {
      if(messagesToReturn.size() > 0)
      {
        LinkedList<Message> messages = messagesToReturn.getFirst();
        msg = messages.removeFirst();

        if(messages.size() == 0)
        {
          messagesToReturn.removeFirst();
        }
      }
    }


    if(null != receiveException)
    {
      throw receiveException;
    }

    return msg;
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private void addData(List<ArrayList<Byte>> list, byte[] rawData)
  {
    ArrayList<Byte> data = new ArrayList<Byte>(rawData.length);

    for (byte dataByte : rawData)
    {
      data.add(dataByte);
    }

    list.add(data);
  }


  private void triggerException(Exception exception) throws IOException, PortException
  {
    if(exception instanceof IOException)
    {
      throw (IOException)exception;
    }

    else if(exception instanceof PortException)
    {
      throw (PortException)exception;
    }
  }
}
