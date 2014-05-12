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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.PersistenceManager;
import aletheia.utilities.MiscUtilities;

public class FemalePeerToPeerNode extends PeerToPeerNode
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.logger();

	private final HookNetworkJoinThread hookNetworkJoinThread;

	private class ExternalServerSocketManager extends ServerSocketManager
	{

		private ExternalServerSocketManager(InetSocketAddress bindSocketAddress) throws IOException
		{
			super("PeerToPeerNode.ExternalServerSocketManager " + getNodeUuid().toString() + " " + bindSocketAddress.toString(), bindSocketAddress);
		}

		@Override
		protected void processSocketChannel(SocketChannel socketChannel) throws IOException, DisconnectingException
		{
			boolean added = false;
			try
			{
				if (checkSocketChannelAddress(socketChannel))
				{
					addConnection(new FemalePeerToPeerConnection(FemalePeerToPeerNode.this, socketChannel,
							MiscUtilities.socketChannelRemoteInetAddress(socketChannel), externalAcceptedSubRootPhaseTypes));
					added = true;
				}
			}
			finally
			{
				if (!added)
					socketChannel.close();
			}
		}

	}

	public class ExternalServerSocketManagerException extends ServerSocketManagerException
	{
		private static final long serialVersionUID = -3654884483195707139L;

		protected ExternalServerSocketManagerException()
		{
			super();
		}

		protected ExternalServerSocketManagerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected ExternalServerSocketManagerException(String message)
		{
			super(message);
		}

		protected ExternalServerSocketManagerException(Throwable cause)
		{
			super(cause);
		}
	}

	private ExternalServerSocketManager externalServerSocketManager;

	public FemalePeerToPeerNode(PersistenceManager persistenceManager, InetSocketAddress bindSocketAddress) throws ExternalServerSocketManagerException
	{
		super(persistenceManager);
		this.hookNetworkJoinThread = new HookNetworkJoinThread(this);
		this.externalServerSocketManager = externalServerSocketManagerStart(bindSocketAddress);
		setExternalBindSocketPort(bindSocketAddress.getPort());
		init();
	}

	@Override
	protected void init()
	{
		super.init();
		hookNetworkJoinThread.start();
	}

	public synchronized void externalBind(InetSocketAddress bindSocketAddress) throws ExternalServerSocketManagerException
	{
		if (!bindSocketAddress.equals(getExternalBindSocketAddress()))
			setExternalBindSocketAddress(bindSocketAddress);
	}

	public synchronized InetSocketAddress externalBindSocketAddress()
	{
		return getExternalBindSocketAddress();
	}

	public class ExpectingMyOwnUuidException extends ConnectException
	{
		private static final long serialVersionUID = 6344071330809116435L;

		protected ExpectingMyOwnUuidException()
		{
			super();
		}

	}

	@Override
	protected <C extends MalePeerToPeerConnection> C connect(InetSocketAddress socketAddress, Class<? extends C> connectionClass, UUID expectedPeerNodeUuid,
			Object... extraArgs) throws IOException, ConnectException
	{
		if (getNodeUuid().equals(expectedPeerNodeUuid))
			throw new ExpectingMyOwnUuidException();
		return super.connect(socketAddress, connectionClass, expectedPeerNodeUuid, extraArgs);
	}

	@Override
	public void shutdown(boolean fast) throws IOException, InterruptedException
	{
		hookNetworkJoinThread.shutdown();
		externalServerSocketManager.shutdown();
		super.shutdown(fast);
	}

	protected boolean checkSocketChannelAddress(SocketChannel socketChannel) throws IOException
	{
		SocketAddress localAddress = socketChannel.getLocalAddress();
		if (!(localAddress instanceof InetSocketAddress))
			return false;
		InetAddress localInetAddress = ((InetSocketAddress) localAddress).getAddress();
		InetSocketAddress bindSocketAddress = getExternalBindSocketAddress();
		InetAddress bindInetAddress = bindSocketAddress != null ? bindSocketAddress.getAddress() : null;
		if (!localInetAddress.equals(bindInetAddress))
			return false;
		return true;
	}

	public InetSocketAddress getExternalBindSocketAddress()
	{
		return externalServerSocketManager.getBindSocketAddress();
	}

	private ExternalServerSocketManager externalServerSocketManagerStart(InetSocketAddress bindSocketAddress) throws ExternalServerSocketManagerException
	{
		try
		{
			ExternalServerSocketManager serverSocketManager = new ExternalServerSocketManager(bindSocketAddress);
			serverSocketManager.start();
			return serverSocketManager;
		}
		catch (IOException e)
		{
			throw new ExternalServerSocketManagerException(e);
		}
	}

	private void setExternalBindSocketAddress(InetSocketAddress bindSocketAddress) throws ExternalServerSocketManagerException
	{
		try
		{
			InetAddress oldAddress = externalServerSocketManager.getBindSocketAddress().getAddress();
			externalServerSocketManager.shutdown();
			externalServerSocketManager = externalServerSocketManagerStart(bindSocketAddress);
			setExternalBindSocketPort(bindSocketAddress.getPort());
			if (!bindSocketAddress.getAddress().equals(oldAddress))
				closeAllSocketChannels();
			getResourceTreeNodeSet().updateLocalResourcesMetadata();
		}
		catch (IOException | InterruptedException e)
		{
			throw new ExternalServerSocketManagerException(e);
		}
	}

	@Override
	protected ConnectSocketChannel connectSocketChannel(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid) throws ConnectException, IOException
	{
		ConnectSocketChannel connectSocketChannel = directConnectSocketChannel(socketAddress);
		SocketChannel socketChannel = connectSocketChannel.getSocketChannel();
		if (!checkSocketChannelAddress(socketChannel))
		{
			socketChannel.close();
			throw new InvalidSocketAddressConnectException();
		}
		return connectSocketChannel;
	}

}
