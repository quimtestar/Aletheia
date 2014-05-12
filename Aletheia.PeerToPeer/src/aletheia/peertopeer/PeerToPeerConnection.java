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
package aletheia.peertopeer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.phase.RootPhase;
import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.peertopeer.io.TimeLimitNonBlockingSocketChannelInputStream;
import aletheia.peertopeer.io.TimeLimitNonBlockingSocketChannelOutputStream;
import aletheia.persistence.PersistenceManager;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.io.NonBlockingSocketChannelStream;

public abstract class PeerToPeerConnection extends Thread
{
	private final static Logger logger = LoggerManager.logger();

	private final static int socketChannelExtraTime = 100;

	private final PeerToPeerNode peerToPeerNode;
	private final SocketChannel socketChannel;
	private final InetAddress remoteAddress;

	@ExportableEnumInfo(availableVersions = 0)
	public static enum Gender implements ByteExportableEnum<Gender>
	{
		MALE((byte) 0), FEMALE((byte) 1);

		private final byte code;

		private Gender(byte code)
		{
			this.code = code;
		}

		@Override
		public Byte getCode(int version)
		{
			return code;
		}

		@ProtocolInfo(availableVersions = 0)
		public static class Protocol extends ByteExportableEnumProtocol<Gender>
		{

			public Protocol(int requiredVersion)
			{
				super(0, Gender.class, 0);
				checkVersionAvailability(Protocol.class, requiredVersion);
			}

		}

	}

	private final TimeLimitNonBlockingSocketChannelInputStream inputStream;
	private final DataInputStream dataIn;
	private final TimeLimitNonBlockingSocketChannelOutputStream outputStream;
	private final DataOutputStream dataOut;

	private Exception caughtException;
	private boolean shutdownSocketWhenFinish;

	protected PeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress) throws IOException
	{
		super("PeerToPeerConnection " + socketChannel.toString());
		this.peerToPeerNode = peerToPeerNode;
		this.socketChannel = socketChannel;
		this.socketChannel.configureBlocking(false);
		this.remoteAddress = remoteAddress;
		this.inputStream = new TimeLimitNonBlockingSocketChannelInputStream(socketChannel, socketChannelExtraTime);
		this.dataIn = new DataInputStream(inputStream);
		this.outputStream = new TimeLimitNonBlockingSocketChannelOutputStream(socketChannel, socketChannelExtraTime);
		this.dataOut = new DataOutputStream(outputStream);

		this.caughtException = null;
		this.shutdownSocketWhenFinish = true;
	}

	protected PeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel) throws IOException
	{
		this(peerToPeerNode, socketChannel, null);
	}

	public PeerToPeerNode getPeerToPeerNode()
	{
		return peerToPeerNode;
	}

	public PersistenceManager getPersistenceManager()
	{
		return getPeerToPeerNode().getPersistenceManager();
	}

	public SocketChannel getSocketChannel()
	{
		return socketChannel;
	}

	public InetAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	public abstract Gender getGender();

	public DataInputStream getDataIn()
	{
		return dataIn;
	}

	public DataOutputStream getDataOut()
	{
		return dataOut;
	}

	public long getInputRemainingTime()
	{
		return inputStream.getRemainingTime();
	}

	private void setInputRemainingTime(long remainingTime)
	{
		inputStream.setRemainingTime(remainingTime);
	}

	private boolean expiredInputRemainingTime()
	{
		return inputStream.expiredRemainingTime();
	}

	private long extendInputRemainingTime(long extend) throws NonBlockingSocketChannelStream.TimeoutException
	{
		return inputStream.extendRemainingTime(extend);
	}

	private void interruptInputStream()
	{
		inputStream.interrupt();
	}

	public long getOutputRemainingTime()
	{
		return outputStream.getRemainingTime();
	}

	private void setOutputRemainingTime(long remainingTime)
	{
		outputStream.setRemainingTime(remainingTime);
	}

	private boolean expiredOutputRemainingTime()
	{
		return outputStream.expiredRemainingTime();
	}

	private long extendOutputRemainingTime(long extend) throws NonBlockingSocketChannelStream.TimeoutException
	{
		return outputStream.extendRemainingTime(extend);
	}

	private void interruptOutputStream()
	{
		outputStream.interrupt();
	}

	public void setRemainingTime(long remainingTime)
	{
		setInputRemainingTime(remainingTime);
		setOutputRemainingTime(remainingTime);
	}

	public boolean expiredRemainingTime()
	{
		return expiredInputRemainingTime() || expiredOutputRemainingTime();
	}

	public long extendRemainingTime(long extend) throws NonBlockingSocketChannelStream.TimeoutException
	{
		long extended = extendInputRemainingTime(extend);
		extendOutputRemainingTime(extend);
		return extended;
	}

	public void interruptStreams()
	{
		interruptInputStream();
		interruptOutputStream();
	}

	public abstract RootPhase getRootPhase();

	public Exception getCaughtException()
	{
		return caughtException;
	}

	public boolean isShutdownSocketWhenFinish()
	{
		return shutdownSocketWhenFinish;
	}

	public void setShutdownSocketWhenFinish(boolean shutdownSocketWhenFinish)
	{
		this.shutdownSocketWhenFinish = shutdownSocketWhenFinish;
	}

	@Override
	public void run()
	{
		try
		{
			logger.info("Starting Aletheia P2P " + getGender() + " connection with " + remoteAddress);
			try
			{
				getRootPhase().run();
			}
			catch (EOFException e)
			{
				logger.info(e + " captured");
			}
			catch (Exception e)
			{
				logger.error("Exception caught", e);
				this.caughtException = e;
			}
			finally
			{
				logger.info("Terminating Aletheia P2P Connection with " + remoteAddress);
				closeStreams();
				getPeerToPeerNode().connectionEnded(PeerToPeerConnection.this);
			}
		}
		catch (IOException e)
		{
			logger.error("Exception caught", e);
		}

	}

	private void closeStreams() throws IOException
	{
		inputStream.close();
		outputStream.close();
	}

	public void shutdown(boolean fast, boolean join) throws IOException, InterruptedException
	{
		if (fast)
			closeStreams();
		getRootPhase().shutdown(fast);
		if (join)
		{
			if (equals(currentThread()))
				throw new IllegalStateException();
			join();
		}
	}

	public SubRootPhase waitForSubRootPhase() throws InterruptedException
	{
		return getRootPhase().waitForSubRootPhase();
	}

	public SubRootPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return getRootPhase().waitForSubRootPhase(aborter);
	}

}
