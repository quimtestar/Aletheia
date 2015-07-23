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
package aletheia.peertopeer.base.phase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.base.dialog.Dialog;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.dialog.ValedictionDialog;
import aletheia.persistence.PersistenceManager;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.io.NonBlockingSocketChannelStream;

public abstract class Phase
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final PeerToPeerConnection peerToPeerConnection;

	public Phase(PeerToPeerConnection peerToPeerConnection)
	{
		super();
		this.peerToPeerConnection = peerToPeerConnection;
	}

	public PeerToPeerConnection getPeerToPeerConnection()
	{
		return peerToPeerConnection;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return getPeerToPeerConnection().getPersistenceManager();
	}

	protected PeerToPeerNode getPeerToPeerNode()
	{
		return getPeerToPeerConnection().getPeerToPeerNode();
	}

	protected SocketChannel getSocketChannel()
	{
		return getPeerToPeerConnection().getSocketChannel();
	}

	protected PeerToPeerConnection.Gender getGender()
	{
		return getPeerToPeerConnection().getGender();
	}

	protected DataInputStream getDataIn()
	{
		return getPeerToPeerConnection().getDataIn();
	}

	protected DataOutputStream getDataOut()
	{
		return getPeerToPeerConnection().getDataOut();
	}

	protected long getInputRemainingTime()
	{
		return getPeerToPeerConnection().getInputRemainingTime();
	}

	protected void setRemainingTime(long remainingTime)
	{
		getPeerToPeerConnection().setRemainingTime(remainingTime);
		if (remainingTime > 0)
			logger.debug(getClass().getName() + ": remaining time set to: " + remainingTime + " millis");
	}

	protected boolean expiredRemainingTime()
	{
		return getPeerToPeerConnection().expiredRemainingTime();
	}

	protected long extendRemainingTime(long extend) throws NonBlockingSocketChannelStream.TimeoutException
	{
		long remainingTime = getPeerToPeerConnection().extendRemainingTime(extend);
		if (remainingTime > 0)
			logger.debug(getClass().getName() + ": remaining time extended to: " + remainingTime + " millis");
		return remainingTime;
	}

	protected long getOutputRemainingTime()
	{
		return getPeerToPeerConnection().getOutputRemainingTime();
	}

	protected void interruptStreams()
	{
		getPeerToPeerConnection().interruptStreams();
	}

	protected void valedictionDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		dialog(ValedictionDialog.class, this);
	}

	public abstract void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException;

	public void shutdown(boolean fast)
	{
	}

	private <D extends Dialog> D dialog(Constructor<D> dialogConstructor, Object... initargs)
			throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		D dialog;
		try
		{
			dialog = dialogConstructor.newInstance(initargs);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getTargetException());
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		}
		dialog.run();
		return dialog;
	}

	protected <D extends Dialog> Constructor<D> dialogConstructor(Class<D> dialogClass, Object... initargs)
	{
		Constructor<D> constructor = MiscUtilities.matchingConstructor(dialogClass, initargs);
		if (constructor == null)
			throw new Error("Missing constructor for class " + dialogClass.getName() + " " + Arrays.toString(initargs));
		return constructor;
	}

	protected <D extends Dialog> D dialog(Class<D> dialogClass, Object... initargs)
			throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		Constructor<D> constructor = dialogConstructor(dialogClass, initargs);
		return dialog(constructor, initargs);
	}

	protected InetAddress getRemoteAddress()
	{
		return getPeerToPeerConnection().getRemoteAddress();
	}

	protected boolean currentThreadConnection()
	{
		return Thread.currentThread().equals(getPeerToPeerConnection());
	}

	protected UUID getNodeUuid()
	{
		return getPeerToPeerNode().getNodeUuid();
	}

	private Iterator<Phase> ancestorIterator()
	{
		return new Iterator<Phase>()
		{
			private Phase phase = Phase.this;

			@Override
			public boolean hasNext()
			{
				return phase != null;
			}

			@Override
			public Phase next()
			{
				Phase old = phase;
				if (phase instanceof SubPhase)
					phase = ((SubPhase) phase).getParentPhase();
				else
					phase = null;
				return old;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	protected Iterable<Phase> ancestors()
	{
		return new Iterable<Phase>()
		{

			@Override
			public Iterator<Phase> iterator()
			{
				return ancestorIterator();
			}
		};
	}

	protected <P extends Phase> P ancestor(Class<? extends P> ancestorClass)
	{
		for (Phase p : ancestors())
		{
			if (ancestorClass.isInstance(p))
				return ancestorClass.cast(p);
		}
		return null;
	}

	protected RootPhase rootPhase()
	{
		return ancestor(RootPhase.class);
	}

	protected int getProtocolVersion()
	{
		return rootPhase().getProtocolVersion();
	}

}
