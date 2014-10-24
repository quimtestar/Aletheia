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
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.conjugal.ConjugalMalePeerToPeerConnection;
import aletheia.peertopeer.conjugal.phase.FemaleConjugalPhase;
import aletheia.peertopeer.conjugal.phase.GenderedConjugalPhase.OpenConnectionException;
import aletheia.peertopeer.conjugal.phase.MaleConjugalPhase;
import aletheia.peertopeer.spliced.SplicedMalePeerToPeerConnection;
import aletheia.persistence.PersistenceManager;
import aletheia.utilities.AsynchronousInvoker;

public class MalePeerToPeerNode extends PeerToPeerNode
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final InetSocketAddress surrogateAddress;

	private class ConjugalMaintainer extends Thread
	{
		private final static int conjugalConnectionInterval = 5000;

		private boolean shutdown;

		private ConjugalMaintainer()
		{
			super("ConjugalMaintainer " + getNodeUuid().toString());
			setDaemon(true);
			this.shutdown = false;
		}

		@Override
		public synchronized void run()
		{
			while (!shutdown)
			{
				try
				{
					wait(conjugalConnectionInterval);
					if (shutdown)
						break;
					conjugalConnect();
					break;
				}
				catch (InterruptedException | ConnectException | IOException e)
				{
				}
			}
		}

		public synchronized void shutdown() throws InterruptedException
		{
			shutdown = true;
			notifyAll();
			join();
		}

	}

	private class ConjugalManager
	{
		private ConjugalMalePeerToPeerConnection conjugalMalePeerToPeerConnection;
		private MaleConjugalPhase maleConjugalPhase;
		private ConjugalMaintainer conjugalMaintainer;

		private ConjugalManager()
		{
			this.conjugalMalePeerToPeerConnection = null;
			this.maleConjugalPhase = null;
			this.conjugalMaintainer = null;
		}

		private void init()
		{
			try
			{
				conjugalConnect();
			}
			catch (ConnectException | IOException | InterruptedException e)
			{
				conjugalMaintainer = new ConjugalMaintainer();
				conjugalMaintainer.start();
			}
		}

		private synchronized void conjugalConnectionEnded(ConjugalMalePeerToPeerConnection connection)
		{
			if (conjugalMalePeerToPeerConnection != null && conjugalMalePeerToPeerConnection == connection && !isDisconnectingAll())
			{
				conjugalMalePeerToPeerConnection = null;
				maleConjugalPhase = null;
				conjugalMaintainer = new ConjugalMaintainer();
				conjugalMaintainer.start();
			}
		}

		private synchronized void conjugalConnect() throws ConnectException, IOException, InterruptedException
		{
			conjugalMalePeerToPeerConnection = MalePeerToPeerNode.this.conjugalConnect(surrogateAddress);
			maleConjugalPhase = conjugalMalePeerToPeerConnection.waitForMaleConjugalPhase();
			if (maleConjugalPhase == null)
				throw new ConjugalConnectException();
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{
				@Override
				public void invoke()
				{
					try
					{
						asynchronousNetworkJoin(null, null);
					}
					catch (ConnectException | IOException | InterruptedException e)
					{
						logger.error("Exception caught", e);
					}
				}
			});
		}

		private synchronized MaleConjugalPhase getMaleConjugalPhase() throws NotConjugalConnectedException
		{
			if (maleConjugalPhase == null)
				throw new NotConjugalConnectedException();
			return maleConjugalPhase;
		}

		private synchronized UUID getFemaleNodeUuid()
		{
			if (maleConjugalPhase == null)
				return null;
			else
				return maleConjugalPhase.getPeerNodeUuid();
		}

		private synchronized void shutdown() throws InterruptedException
		{
			if (conjugalMaintainer != null)
				conjugalMaintainer.shutdown();
		}

		private synchronized void updateMaleNodeUuids(Collection<UUID> addUuids, Collection<UUID> removeUuids)
		{
			if (maleConjugalPhase != null)
				maleConjugalPhase.updateMaleNodeUuids(addUuids, removeUuids);
		}

	}

	private final ConjugalManager conjugalManager;

	private final ConjugalNetworkJoinThread conjugalNetworkJoinThread;

	public MalePeerToPeerNode(PersistenceManager persistenceManager, InetSocketAddress surrogateAddress) throws ConnectException, IOException,
			InterruptedException
	{
		super(persistenceManager);
		this.surrogateAddress = surrogateAddress;
		this.conjugalManager = new ConjugalManager();
		this.conjugalNetworkJoinThread = new ConjugalNetworkJoinThread(this);

		init();
	}

	@Override
	protected void connectionEnded(PeerToPeerConnection connection) throws IOException
	{
		super.connectionEnded(connection);
		if (connection instanceof ConjugalMalePeerToPeerConnection)
			conjugalConnectionEnded((ConjugalMalePeerToPeerConnection) connection);
	}

	@Override
	protected void init()
	{
		super.init();
		conjugalManager.init();
		conjugalNetworkJoinThread.start();
	}

	private void conjugalConnectionEnded(ConjugalMalePeerToPeerConnection connection)
	{
		conjugalManager.conjugalConnectionEnded(connection);
	}

	public InetSocketAddress getSurrogateAddress()
	{
		return surrogateAddress;
	}

	private void conjugalConnect() throws ConnectException, IOException, InterruptedException
	{
		conjugalManager.conjugalConnect();
	}

	private UUID getFemaleNodeUuid()
	{
		return conjugalManager.getFemaleNodeUuid();
	}

	private NodeAddress getFemaleNodeAddress()
	{
		return new NodeAddress(getFemaleNodeUuid(), surrogateAddress);
	}

	@Override
	public void shutdown(boolean fast) throws IOException, InterruptedException
	{
		conjugalNetworkJoinThread.shutdown();
		conjugalManager.shutdown();
		super.shutdown(fast);
	}

	public class MaleConnectException extends ConnectException
	{
		private static final long serialVersionUID = -5087356933662658116L;

		private MaleConnectException()
		{
			super();
		}

		private MaleConnectException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private MaleConnectException(String message)
		{
			super(message);
		}

		private MaleConnectException(Throwable cause)
		{
			super(cause);
		}
	}

	public class NotConjugalConnectedException extends MaleConnectException
	{
		private static final long serialVersionUID = 4665884446671988328L;

	}

	private MaleConjugalPhase getMaleConjugalPhase() throws NotConjugalConnectedException
	{
		return conjugalManager.getMaleConjugalPhase();
	}

	@Override
	protected ConnectSocketChannel connectSocketChannel(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid) throws MaleConnectException
	{
		try
		{
			SplicedConnectionId connectionIdAddress = getMaleConjugalPhase().maleOpenConnection(socketAddress, expectedPeerNodeUuid);
			SplicedMalePeerToPeerConnection splicedMalePeerToPeerConnection = splicedConnect(getFemaleNodeAddress(), connectionIdAddress.getConnectionId());
			splicedMalePeerToPeerConnection.join();
			return new ConnectSocketChannel(splicedMalePeerToPeerConnection.getSocketChannel(), connectionIdAddress.getRemoteAddress());
		}
		catch (IOException | ConnectException | InterruptedException | CancelledCommandException | OpenConnectionException e)
		{
			throw new MaleConnectException(e);
		}
	}

	@Override
	public void updateMaleNodeUuids(FemaleConjugalPhase femaleConjugalPhase, Collection<UUID> addUuids, Collection<UUID> removeUuids)
	{
		super.updateMaleNodeUuids(femaleConjugalPhase, addUuids, removeUuids);
		conjugalManager.updateMaleNodeUuids(addUuids, removeUuids);
	}

	public void splicedAccept(SplicedConnectionId splicedConnectionId) throws MaleConnectException
	{
		try
		{
			SplicedMalePeerToPeerConnection splicedMalePeerToPeerConnection = splicedConnect(getFemaleNodeAddress(), splicedConnectionId.getConnectionId());
			splicedMalePeerToPeerConnection.join();
			SocketChannel socketChannel = splicedMalePeerToPeerConnection.getSocketChannel();
			boolean added = false;
			try
			{
				addConnection(new FemalePeerToPeerConnection(this, socketChannel, splicedConnectionId.getRemoteAddress(), externalAcceptedSubRootPhaseTypes));
				added = true;
			}
			finally
			{
				if (!added)
					socketChannel.close();
			}
		}
		catch (ConnectException | IOException | InterruptedException e)
		{
			throw new MaleConnectException(e);
		}

	}

}
