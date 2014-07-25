/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.base.dialog;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerNodeProperties;
import aletheia.peertopeer.base.message.Message;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.base.phase.SubPhase;
import aletheia.peertopeer.base.protocol.MessageProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream;

public abstract class Dialog extends SubPhase
{
	private final static Logger logger = LoggerManager.logger();

	private final static float initialRemainingTime = PeerToPeerNodeProperties.instance.isDebug() ? 0f : 60f; // in seconds

	public class DialogStreamException extends Exception
	{
		private static final long serialVersionUID = 8236307834018141784L;

		public DialogStreamException(NonBlockingSocketChannelStream.StreamException cause)
		{
			super(cause);
		}

		@Override
		public synchronized NonBlockingSocketChannelStream.StreamException getCause()
		{
			return (NonBlockingSocketChannelStream.StreamException) super.getCause();
		}
	}

	protected class AsynchronousMessageSender extends Thread
	{
		class QueueEntry
		{
			final Message message;

			public QueueEntry(Message message)
			{
				super();
				this.message = message;
			}
		}

		private final Queue<QueueEntry> queue;
		private boolean shutdown;
		private Exception exception;

		public AsynchronousMessageSender()
		{
			super(getPeerToPeerConnection().getName() + " " + "AsynchronousMessageSender");
			this.queue = new ArrayDeque<>();
			this.shutdown = false;
			this.exception = null;
			this.start();
		}

		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					QueueEntry entry;
					synchronized (this)
					{
						while (queue.isEmpty() && !shutdown)
							this.wait();
						if (queue.isEmpty() && shutdown)
							break;
						entry = queue.remove();
					}
					getMessageProtocol().send(getDataOut(), entry.message);
				}
			}
			catch (Exception e)
			{
				this.exception = e;
				interruptStreams();
			}
		}

		public void shutdown() throws IOException, InterruptedException
		{
			synchronized (this)
			{
				shutdown = true;
				notifyAll();
			}
			join();
			if (exception != null)
			{
				if (exception instanceof IOException)
					throw (IOException) exception;
				else if (exception instanceof RuntimeException)
					throw (RuntimeException) exception;
				else
					throw new Error(exception);
			}
		}

		public synchronized void send(Message message) throws IOException, InterruptedException
		{
			if (exception != null)
				shutdown();
			queue.add(new QueueEntry(message));
			notifyAll();
		}

	}

	private final AsynchronousMessageSender sender;

	public Dialog(Phase phase)
	{
		super(phase);
		this.sender = new AsynchronousMessageSender();
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug(getClass().getName() + ": starting");
		try
		{
			try
			{
				setRemainingTime((long) (initialRemainingTime * 1000));
				dialogate();
			}
			finally
			{
				sender.shutdown();
			}
		}
		catch (NonBlockingSocketChannelStream.StreamException e)
		{
			throw new DialogStreamException(e);
		}
		finally
		{
			logger.debug(getClass().getName() + ": ending");
		}
	}

	protected abstract MessageProtocol getMessageProtocol();

	protected AsynchronousMessageSender getSender()
	{
		return sender;
	}

	protected abstract void dialogate() throws IOException, ProtocolException, InterruptedException, NonBlockingSocketChannelStream.TimeoutException;

	protected <M extends Message> M recvMessage(Class<? extends M> messageClass) throws IOException, ProtocolException
	{
		return getMessageProtocol().recv(getDataIn(), messageClass);
	}

	protected <M extends Message> M recvMessage(Collection<Class<? extends M>> messageClasses) throws IOException, ProtocolException
	{
		return getMessageProtocol().recvClasses(getDataIn(), messageClasses);
	}

	protected Message recvMessage() throws IOException, ProtocolException
	{
		return recvMessage(Message.class);
	}

	protected <M extends Message> M skipToMessage(Class<M> messageClass) throws IOException, ProtocolException
	{
		return getMessageProtocol().skipTo(getDataIn(), messageClass);
	}

	protected void sendMessage(Message m) throws IOException, InterruptedException
	{
		getSender().send(m);
	}

}
