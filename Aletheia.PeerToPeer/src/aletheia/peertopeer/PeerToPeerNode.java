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
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.deferredmessagecontent.CipheredDeferredMessageContent.DecipherException;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.model.peertopeer.deferredmessagecontent.PersonsDeferredMessageContent;
import aletheia.model.peertopeer.deferredmessagecontent.SignatureRequestDeferredMessageContent;
import aletheia.model.statement.RootContext;
import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.peertopeer.conjugal.ConjugalMalePeerToPeerConnection;
import aletheia.peertopeer.conjugal.phase.ConjugalPhase;
import aletheia.peertopeer.conjugal.phase.FemaleConjugalPhase;
import aletheia.peertopeer.conjugal.phase.GenderedConjugalPhase.OpenConnectionException;
import aletheia.peertopeer.ephemeral.EphemeralMalePeerToPeerConnection;
import aletheia.peertopeer.ephemeral.phase.EphemeralPhase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.BeltNetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.ComplementingNetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.CumulationSet;
import aletheia.peertopeer.network.DeferredMessageSet;
import aletheia.peertopeer.network.JoiningNetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.LocalRouterSet;
import aletheia.peertopeer.network.ResourceTreeNodeSet;
import aletheia.peertopeer.network.ResourceTreeNodeSet.Location;
import aletheia.peertopeer.network.RouteableSubMessageProcessor;
import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.message.routeablesubmessage.BeltConnectRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ClosestNodeResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ClosestNodeRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ComplementingInvitationRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.FoundLocateResourceResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.LocateResourceResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.LocateResourceRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ResourceMetadataRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.peertopeer.resource.PersonResource;
import aletheia.peertopeer.resource.PrivatePersonResource;
import aletheia.peertopeer.resource.Resource;
import aletheia.peertopeer.resource.RootContextSignatureResource;
import aletheia.peertopeer.resource.StatementProofResource;
import aletheia.peertopeer.resource.SubscribedStatementsContextResource;
import aletheia.peertopeer.spliced.SplicedMalePeerToPeerConnection;
import aletheia.peertopeer.spliced.phase.GenderedSplicedPhase;
import aletheia.peertopeer.spliced.phase.SplicedPhase;
import aletheia.peertopeer.statement.StatementMalePeerToPeerConnection;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.MiscUtilities.NoConstructorException;
import aletheia.utilities.SynchronizedFlag;
import aletheia.utilities.aborter.Aborter;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.CombinedCollection;

public abstract class PeerToPeerNode
{
	private final static Logger logger = LoggerManager.logger();

	private final static float connectTimeout = PeerToPeerNodeProperties.instance.isDebug() ? 0f : 5f; //secs
	private final static float joinedToNetworkTimeout = PeerToPeerNodeProperties.instance.isDebug() ? 0f : 5f; //secs

	protected final static EnumSet<SubRootPhaseType> externalAcceptedSubRootPhaseTypes = EnumSet.of(SubRootPhaseType.Statement, SubRootPhaseType.Network,
			SubRootPhaseType.Ephemeral);

	public abstract static class PeerToPeerNodeException extends Exception
	{
		private static final long serialVersionUID = -5810364866676144196L;

		protected PeerToPeerNodeException()
		{
			super();
		}

		protected PeerToPeerNodeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected PeerToPeerNodeException(String message)
		{
			super(message);
		}

		protected PeerToPeerNodeException(Throwable cause)
		{
			super(cause);
		}

	}

	private final PersistenceManager persistenceManager;

	public interface Listener
	{
		public void exception(String message, Exception e);
	}

	private final Collection<Listener> listeners;

	private class ConnectionManager
	{
		private final Set<PeerToPeerConnection> connectionSet;

		private ConnectionManager()
		{
			this.connectionSet = new HashSet<PeerToPeerConnection>();
		}

		public synchronized void addConnection(PeerToPeerConnection connection) throws DisconnectingException
		{
			if (isDisconnectingAll())
			{
				try
				{
					connection.shutdown(true, true);
				}
				catch (IOException | InterruptedException e)
				{
					logger.error("Exception caught", e);
				}
				throw new DisconnectingException();
			}
			if (connectionSet.add(connection))
				connection.start();
		}

		public synchronized boolean removeConnection(PeerToPeerConnection connection)
		{
			boolean removed = connectionSet.remove(connection);
			return removed;
		}

		public synchronized Collection<PeerToPeerConnection> connections()
		{
			return new BufferedList<>(connectionSet);
		}

	}

	private final ConnectionManager connectionManager;

	private class SubRootPhaseManager
	{
		private final Map<SubRootPhaseType, Set<SubRootPhase>> subRootPhasesMap;

		private SubRootPhaseManager()
		{
			this.subRootPhasesMap = new EnumMap<SubRootPhaseType, Set<SubRootPhase>>(SubRootPhaseType.class);
			for (SubRootPhaseType type : SubRootPhaseType.values())
				subRootPhasesMap.put(type, new HashSet<SubRootPhase>());
		}

		public synchronized <P extends SubRootPhase> boolean addSubRootPhase(P subRootPhase)
		{
			return subRootPhasesMap.get(SubRootPhaseType.objectType(subRootPhase)).add(subRootPhase);
		}

		public synchronized <P extends SubRootPhase> boolean removeSubRootPhase(P subRootPhase)
		{
			return subRootPhasesMap.get(SubRootPhaseType.objectType(subRootPhase)).remove(subRootPhase);
		}

		private synchronized <P extends SubRootPhase> Collection<P> getSubRootPhases(SubRootPhaseType type)
		{
			return new BijectionCollection<SubRootPhase, P>(new CastBijection<SubRootPhase, P>(), subRootPhasesMap.get(type));
		}

		public synchronized <P extends SubRootPhase> Collection<P> subRootPhases(SubRootPhaseType type)
		{
			return new BufferedList<P>(new BijectionCollection<SubRootPhase, P>(new CastBijection<SubRootPhase, P>(), subRootPhasesMap.get(type)));
		}

		public synchronized <P extends SubRootPhase> Collection<P> subRootPhases(Class<P> subRootPhaseClass)
		{
			return subRootPhases(SubRootPhaseType.classType(subRootPhaseClass));
		}

		public synchronized <P extends SubRootPhase> boolean isSubRootPhasesEmpty(SubRootPhaseType type)
		{
			return getSubRootPhases(type).isEmpty();
		}

		public synchronized <P extends SubRootPhase> boolean isSubRootPhasesEmpty(Class<P> subRootPhaseClass)
		{
			return isSubRootPhasesEmpty(SubRootPhaseType.classType(subRootPhaseClass));
		}

	}

	private final SubRootPhaseManager subRootPhaseManager;

	private final PeerToPeerConnectionLock persistentConnectionLock;

	private class FollowerListener implements PeerToPeerFollower.Listener, PeerToPeerStatementFollower.Listener, PeerToPeerPersonFollower.Listener
	{

		private void putLocalPersonResource(Person person)
		{
			PersonResource.Metadata personResourceMetadata = new PersonResource.Metadata(person);
			putLocalResource(personResourceMetadata);
			Location nextLocation = getNextLocation(personResourceMetadata.getResource());
			if (nextLocation != null)
			{
				PersonResource.Metadata nextPersonResourceMetadata = (PersonResource.Metadata) nextLocation.getResourceMetadata();
				PersonResource.Metadata.PersonInfo nextPersonInfo = nextPersonResourceMetadata.getPersonInfo();
				PersonResource.Metadata.PersonInfo personInfo = personResourceMetadata.getPersonInfo();
				if (personInfo.compareTo(nextPersonInfo) > 0)
					sendResourceMetadata(nextLocation.getNodeAddress().getUuid(), personResourceMetadata);
			}
		}

		private void removeLocalPersonResource(Person person)
		{
			removeLocalResource(new PersonResource(person.getUuid()));
		}

		@Override
		public void personAdded(Person person)
		{
			putLocalPersonResource(person);
			if (person instanceof PrivatePerson)
			{
				PrivatePerson privatePerson = (PrivatePerson) person;
				putLocalResource(new PrivatePersonResource.Metadata(privatePerson.getUuid()));
			}
		}

		@Override
		public void personModified(Person person)
		{
			putLocalPersonResource(person);
		}

		@Override
		public void personRemoved(Person person)
		{
			removeLocalPersonResource(person);
			if (person instanceof PrivatePerson)
			{
				PrivatePerson privatePerson = (PrivatePerson) person;
				removeLocalResource(new PrivatePersonResource(privatePerson.getUuid()));
			}
		}

		@Override
		public void contextLocalSubscribed(ContextLocal contextLocal)
		{
			putLocalResource(new SubscribedStatementsContextResource.Metadata(contextLocal.getStatementUuid()));
		}

		@Override
		public void contextLocalUnsubscribed(ContextLocal contextLocal)
		{
			removeLocalResource(new SubscribedStatementsContextResource(contextLocal.getStatementUuid()));
		}

		private StatementProofResource.Metadata createStatementProofResourceMetadata(UUID statementUuid)
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				StatementAuthority statementAuthority = getPersistenceManager().getStatementAuthority(transaction, statementUuid);
				boolean signedProof = statementAuthority == null ? false : statementAuthority.isSignedProof();
				StatementLocal statementLocal = getPersistenceManager().getStatementLocal(transaction, statementUuid);
				boolean subscribed = statementLocal == null ? false : statementLocal.isSubscribeProof();
				if (!signedProof && !subscribed)
					return null;
				return new StatementProofResource.Metadata(statementUuid, signedProof, subscribed);
			}
			finally
			{
				transaction.abort();
			}
		}

		private void statementProofResource(UUID statementUuid)
		{
			StatementProofResource.Metadata metadata = createStatementProofResourceMetadata(statementUuid);
			if (metadata != null)
				putLocalResource(metadata);
			else
				removeLocalResource(new StatementProofResource(statementUuid));

		}

		@Override
		public void statementAuthoritySignedProved(StatementAuthority statementAuthority)
		{
			statementProofResource(statementAuthority.getStatementUuid());
		}

		@Override
		public void statementAuthoritySignedUnproved(StatementAuthority statementAuthority)
		{
			statementProofResource(statementAuthority.getStatementUuid());
		}

		@Override
		public void statementLocalSubscribedProof(StatementLocal statementLocal)
		{
			statementProofResource(statementLocal.getStatementUuid());
		}

		@Override
		public void statementLocalUnsubscribedProof(StatementLocal statementLocal)
		{
			statementProofResource(statementLocal.getStatementUuid());
		}

		@Override
		public void rootContextSignatureUpdated(UUID rootContextUuid)
		{
			PeerToPeerNode.this.rootContextSignatureUpdated(rootContextUuid);
		}

	}

	private final FollowerListener followerListener;

	private final PeerToPeerStatementFollower peerToPeerStatementFollower;
	private final PeerToPeerPersonFollower peerToPeerPersonFollower;

	private class StatementMalePeerToPeerConnectionManager
	{
		private final Map<UUID, StatementMalePeerToPeerConnection> nodeUuidToStatementConnectionMap = new HashMap<UUID, StatementMalePeerToPeerConnection>();
		private final Map<StatementMalePeerToPeerConnection, UUID> statementConnectionToNodeUuidMap = new HashMap<StatementMalePeerToPeerConnection, UUID>();
		private final Map<Resource, UUID> resourceToNodeUuidMap = new HashMap<Resource, UUID>();
		private final Map<UUID, Set<Resource>> nodeUuidToResourceSetMap = new HashMap<UUID, Set<Resource>>();

		public synchronized StatementMalePeerToPeerConnection obtainConnection(Resource resource, NodeAddress nodeAddress) throws ConnectException,
				IOException, InterruptedException
		{
			StatementMalePeerToPeerConnection connect = nodeUuidToStatementConnectionMap.get(nodeAddress.getUuid());
			if (connect == null)
			{
				connect = statementConnect(nodeAddress);
				nodeUuidToStatementConnectionMap.put(nodeAddress.getUuid(), connect);
				statementConnectionToNodeUuidMap.put(connect, nodeAddress.getUuid());
			}
			registerResourceToNodeUuid(resource, nodeAddress.getUuid());
			return connect;
		}

		private synchronized void connectionEnded(StatementMalePeerToPeerConnection connection) throws ConnectException, IOException, InterruptedException
		{
			UUID uuid = statementConnectionToNodeUuidMap.remove(connection);
			if (uuid != null)
				nodeUuidToStatementConnectionMap.remove(uuid);
			Set<Resource> resources = nodeUuidToResourceSetMap.remove(uuid);
			if (resources != null)
			{
				resourceToNodeUuidMap.keySet().removeAll(resources);
				for (Resource resource : resources)
				{
					resourceToNodeUuidMap.remove(resource);
					ResourceTreeNodeSet.Location nextLocation = getNextLocation(resource);
					if (nextLocation != null && isLocalResource(resource))
						localResourceUpdatedNextLocation(nextLocation);
				}
			}
		}

		private synchronized void registerResourceToNodeUuid(Resource resource, UUID nodeUuid) throws IOException, InterruptedException
		{
			UUID oldNodeUuid = resourceToNodeUuidMap.put(resource, nodeUuid);
			if (oldNodeUuid != null && !oldNodeUuid.equals(nodeUuid))
				removeFromUuidToResourceSetMap(oldNodeUuid, resource);
			putIntoUuidToResourceSetMap(nodeUuid, resource);
		}

		public synchronized void unregisterResource(Resource resource) throws IOException, InterruptedException
		{
			UUID oldNodeUuid = resourceToNodeUuidMap.remove(resource);
			if (oldNodeUuid != null)
				removeFromUuidToResourceSetMap(oldNodeUuid, resource);
		}

		private synchronized void removeFromUuidToResourceSetMap(UUID nodeUuid, Resource resource) throws IOException, InterruptedException
		{
			Set<Resource> resources = nodeUuidToResourceSetMap.get(nodeUuid);
			if (resources != null)
			{
				resources.remove(resource);
				if (resources.isEmpty())
				{
					nodeUuidToResourceSetMap.remove(nodeUuid);
					StatementMalePeerToPeerConnection connection = nodeUuidToStatementConnectionMap.get(nodeUuid);
					if (connection != null)
						connection.shutdown(false, false);
				}
			}
		}

		private synchronized void putIntoUuidToResourceSetMap(UUID nodeUuid, Resource resource)
		{
			Set<Resource> resources = nodeUuidToResourceSetMap.get(nodeUuid);
			if (resources == null)
			{
				resources = new HashSet<Resource>();
				nodeUuidToResourceSetMap.put(nodeUuid, resources);
			}
			resources.add(resource);
		}

	}

	private final StatementMalePeerToPeerConnectionManager statementMalePeerToPeerConnectionManager;

	private class RootContextSignatureManager
	{
		private final Map<UUID, UUID> rootContextSignatureUuidMap = new HashMap<UUID, UUID>();

		public synchronized void rootContextSignatureUpdated(final UUID rootContextUuid)
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{
					UUID signatureUuid = signatureUuid(rootContextUuid);
					UUID oldSignatureUuid = rootContextSignatureUuidMap.get(rootContextUuid);
					if (oldSignatureUuid != null && !oldSignatureUuid.equals(signatureUuid))
						setRootContextSignatureResourceMetadata(oldSignatureUuid, false, null);
					if (signatureUuid != null)
					{
						setRootContextSignatureResourceMetadata(signatureUuid, true, null);
						rootContextSignatureUuidMap.put(rootContextUuid, signatureUuid);
					}
					else
						rootContextSignatureUuidMap.remove(rootContextUuid);
				}
			});
		}

		public synchronized void rootContextSignatureSubscribe(UUID signatureUuid, boolean subscribe)
		{
			setRootContextSignatureResourceMetadata(signatureUuid, null, subscribe);
		}

		private UUID signatureUuid(UUID rootContextUuid)
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				RootContextAuthority rootContextAuthority = persistenceManager.getRootContextAuthority(transaction, rootContextUuid);
				if (rootContextAuthority == null)
					return null;
				return rootContextAuthority.getSignatureUuid();
			}
			finally
			{
				transaction.abort();
			}
		}

		private void setRootContextSignatureResourceMetadata(UUID signatureUuid, Boolean provided, Boolean subscribed)
		{
			RootContextSignatureResource resource = new RootContextSignatureResource(signatureUuid);
			RootContextSignatureResource.Metadata metadata = (RootContextSignatureResource.Metadata) getLocalResourceMetadata(resource);
			boolean provided_ = provided != null ? provided : metadata != null ? metadata.isProvided() : false;
			boolean subscribed_ = subscribed != null ? subscribed : metadata != null ? metadata.isSubscribed() : false;
			if ((metadata == null && (provided_ || subscribed_))
					|| (metadata != null && (provided_ || subscribed_) && (metadata.isProvided() != provided_ || metadata.isSubscribed() != subscribed_)))
				putLocalResource(new RootContextSignatureResource.Metadata(resource, provided_, subscribed_));
			else if (metadata != null && !provided_ && !subscribed_)
				removeLocalResource(resource);
		}

	}

	private final RootContextSignatureManager rootContextSignatureManager;

	private class InitializeResourcesThread extends Thread
	{
		private boolean shutdown;

		public InitializeResourcesThread()
		{
			super("PeerToPeerNode.InitializeResourcesThread");
			this.shutdown = false;
		}

		public void shutdown() throws InterruptedException
		{
			shutdown = true;
			join();
		}

		@Override
		public void run()
		{
			Aborter aborter = new Aborter()
			{
				@Override
				public void checkAbort() throws AbortException
				{
					if (shutdown)
						throw new AbortException();
				}
			};
			try
			{
				peerToPeerStatementFollower.follow(aborter);
				peerToPeerPersonFollower.follow(aborter);
			}
			catch (AbortException e)
			{
			}
		}

	}

	private final InitializeResourcesThread initializeResourcesThread;

	private final UUID nodeUuid;

	protected abstract class ServerSocketManager extends Thread
	{
		private final InetSocketAddress bindSocketAddress;
		private final ServerSocketChannel serverSocketChannel;
		private final Selector selector;
		private final SelectionKey selectionKey;
		private boolean running;

		protected ServerSocketManager(String name, InetSocketAddress bindSocketAddress) throws IOException
		{
			super(name);
			this.bindSocketAddress = bindSocketAddress;
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.configureBlocking(false);
			this.selector = Selector.open();
			this.selectionKey = this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.serverSocketChannel.bind(bindSocketAddress);
			this.running = true;
		}

		public InetSocketAddress getBindSocketAddress()
		{
			return bindSocketAddress;
		}

		protected abstract void processSocketChannel(SocketChannel socketChannel) throws IOException, DisconnectingException;

		@Override
		public void run()
		{
			logger.info("Starting server socket manager: " + getName());
			logger.info("Listening to address: " + bindSocketAddress);
			try
			{
				while (running)
				{
					selector.select();
					if (running && selector.selectedKeys().remove(selectionKey))
					{
						try
						{
							SocketChannel socketChannel = serverSocketChannel.accept();
							processSocketChannel(socketChannel);
						}
						catch (IOException | DisconnectingException e)
						{
							logger.error("Exception caught", e);
						}
					}

				}
			}
			catch (IOException e)
			{
				logger.error("Exception caught", e);
			}
			finally
			{
				try
				{
					selector.close();
				}
				catch (IOException e)
				{
					logger.error("Exception caught", e);
				}
			}
			logger.info("Terminating server socket manager: " + getName());
		}

		public void shutdown() throws IOException, InterruptedException
		{
			running = false;
			serverSocketChannel.close();
			selector.wakeup();
			join();
		}

		@Override
		protected void finalize() throws Throwable
		{
			selector.close();
			super.finalize();
		}
	}

	public class ServerSocketManagerException extends PeerToPeerNodeException
	{
		private static final long serialVersionUID = 1967663461425545108L;

		protected ServerSocketManagerException()
		{
			super();
		}

		protected ServerSocketManagerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected ServerSocketManagerException(String message)
		{
			super(message);
		}

		protected ServerSocketManagerException(Throwable cause)
		{
			super(cause);
		}
	}

	private final static EnumSet<SubRootPhaseType> internalAcceptedSubRootPhaseTypes = EnumSet.of(SubRootPhaseType.Conjugal, SubRootPhaseType.Spliced);

	private class InternalServerSocketManager extends ServerSocketManager
	{
		protected InternalServerSocketManager(InetSocketAddress bindSocketAddress) throws IOException
		{
			super("PeerToPeerNode.InternalServerSocketManager " + nodeUuid.toString() + " " + bindSocketAddress.toString(), bindSocketAddress);
		}

		@Override
		protected void processSocketChannel(SocketChannel socketChannel) throws IOException, DisconnectingException
		{
			boolean added = false;
			try
			{
				addConnection(new FemalePeerToPeerConnection(PeerToPeerNode.this, socketChannel, internalAcceptedSubRootPhaseTypes));
				added = true;
			}
			finally
			{
				if (!added)
					socketChannel.close();
			}
		}

		@Override
		public void run()
		{
			super.run();
		}

		@Override
		public void shutdown() throws IOException, InterruptedException
		{
			super.shutdown();
		}
	}

	public class InternalServerSocketManagerException extends ServerSocketManagerException
	{
		private static final long serialVersionUID = 5224373991012836812L;

		protected InternalServerSocketManagerException()
		{
			super();
		}

		protected InternalServerSocketManagerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected InternalServerSocketManagerException(String message)
		{
			super(message);
		}

		protected InternalServerSocketManagerException(Throwable cause)
		{
			super(cause);
		}
	}

	private static class UuidGenerator
	{
		private final static UuidGenerator instance = new UuidGenerator();

		protected UUID generate()
		{
			return UUID.randomUUID();
		}

		@SuppressWarnings("unused")
		private static class DebugUuidGenerator extends UuidGenerator
		{
			private final Random random;
			{
				List<StackTraceElement> stackTraceList = MiscUtilities.stackTraceList(0);
				String mainClass = stackTraceList.get(stackTraceList.size() - 1).getClassName();
				random = new Random();
				long seed = 0;
				logger.debug("DebugUuidGenerator seed: " + seed);
				random.setSeed(seed + mainClass.hashCode());
			}

			@Override
			protected UUID generate()
			{
				byte[] bytes = new byte[20];
				random.nextBytes(bytes);
				return UUID.nameUUIDFromBytes(bytes);
			}
		}
	}

	private final static int requestResponseTimeout = 5000;

	private final RouteableSubMessageProcessor routeableSubMessageProcessor;
	private final Map<Integer, ResponseRouteableSubMessage> pendingResponses;

	private class BeltMaintainer extends Thread
	{
		private final static int beltCompletionTimeout = 5000;

		private final Belt belt;

		private boolean shutdown;

		private BeltMaintainer()
		{
			super("BeltMaintainer " + getNodeUuid().toString());
			this.belt = getBelt();
			this.shutdown = false;
		}

		@Override
		public void run()
		{
			logger.info("Starting Belt Maintainer");
			synchronized (belt)
			{
				while (!shutdown)
				{
					try
					{
						while (!shutdown && (belt.isComplete() || isNetworkIsolated()))
							belt.wait();
						{
							long t0 = System.currentTimeMillis();
							long t1 = t0;
							while (!shutdown && !belt.isComplete() && (t1 - t0 < beltCompletionTimeout))
							{
								belt.wait(beltCompletionTimeout - (t1 - t0));
								t1 = System.currentTimeMillis();
							}
						}
						while (!shutdown && !belt.isComplete() && !isNetworkIsolated())
						{
							logger.debug("sendBeltConnect");
							sendBeltConnect();
							{
								long t0 = System.currentTimeMillis();
								long t1 = t0;
								while (!shutdown && !belt.isComplete() && (t1 - t0 < beltCompletionTimeout))
								{
									belt.wait(beltCompletionTimeout - (t1 - t0));
									t1 = System.currentTimeMillis();
								}
							}
						}
					}
					catch (InterruptedException e1)
					{
						logger.info(e1.getMessage(), e1);
					}
				}
			}
			logger.info("Terminating Belt Maintainer");
		}

		public void shutdown() throws InterruptedException
		{
			synchronized (belt)
			{
				shutdown = true;
				belt.notifyAll();
			}
			join();
		}

	}

	private final BeltMaintainer beltMaintainer;

	private class ResourceTreeNodeSetListener implements ResourceTreeNodeSet.Listener
	{

		@Override
		public void setLocalMetadata(Resource.Metadata localMetadata)
		{
			ResourceTreeNodeSet.ResourceTreeNode resourceTreeNode = getResourceTreeNodeSet().getResourceTreeNode(localMetadata.getResource());
			if (resourceTreeNode != null)
			{
				final ResourceTreeNodeSet.Location nextLocation = resourceTreeNode.getNextLocation();
				if (nextLocation != null)
				{
					AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
					{

						@Override
						public void invoke()
						{
							try
							{
								localResourceUpdatedNextLocation(nextLocation);
							}
							catch (ConnectException | IOException | InterruptedException e)
							{
								logger.error(e.getMessage(), e);
							}
						}
					});
				}
			}
		}

		@Override
		public void clearLocalMetadata(final Resource resource)
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{
					try
					{
						localResourceRemovedNextLocation(resource);
					}
					catch (IOException | InterruptedException e)
					{
						logger.error(e.getMessage(), e);
					}
				}
			});
		}

		@Override
		public void updatedClosestLocation(ResourceTreeNodeSet.Location closestLocation)
		{
		}

		@Override
		public void removedClosestLocation(Resource resource)
		{
		}

		@Override
		public void updatedNextLocation(final ResourceTreeNodeSet.Location nextLocation)
		{
			if (getResourceTreeNodeSet().isLocalResource(nextLocation.getResourceMetadata().getResource()))
			{
				AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
				{
					@Override
					public void invoke()
					{
						try
						{
							localResourceUpdatedNextLocation(nextLocation);
						}
						catch (ConnectException | IOException | InterruptedException e)
						{
							logger.error(e.getMessage(), e);
						}
					}
				});
			}
		}

		@Override
		public void removedNextLocation(final Resource resource)
		{
			if (getResourceTreeNodeSet().isLocalResource(resource))
			{
				AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
				{

					@Override
					public void invoke()
					{
						try
						{
							localResourceRemovedNextLocation(resource);
						}
						catch (IOException | InterruptedException e)
						{
							logger.error(e.getMessage(), e);
						}
					}
				});
			}
		}

	}

	private final ResourceTreeNodeSetListener resourceTreeNodeSetListener;

	private enum JoinStatus
	{
		Pending, Joined, NotJoining
	}

	private final SynchronizedFlag<JoinStatus> joinedToNetworkFlag;

	private final static float spliceManagerPendingExpireTime = PeerToPeerNodeProperties.instance.isDebug() ? 0f : 60f; // in secs

	private class SpliceManager
	{

		private final SocketChannelSplicer socketChannelSplicer;

		private class SplicePendingException extends PeerToPeerNodeException
		{
			private static final long serialVersionUID = 7633245172637320861L;

			private SplicePendingException()
			{
				super();
			}

			private SplicePendingException(String message, Throwable cause)
			{
				super(message, cause);
			}

			private SplicePendingException(String message)
			{
				super(message);
			}

			private SplicePendingException(Throwable cause)
			{
				super(cause);
			}
		}

		private abstract class PendingEntry implements Comparable<PendingEntry>
		{
			private final int connectionId;
			private final long expires;
			private final FemaleConjugalPhase femaleConjugalPhase;
			private final byte[] pendingData;

			private PendingEntry(int connectionId, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData)
			{
				this.connectionId = connectionId;
				this.expires = spliceManagerPendingExpireTime > 0 ? System.currentTimeMillis() + (long) (spliceManagerPendingExpireTime * 1000)
						: Long.MAX_VALUE;
				this.femaleConjugalPhase = femaleConjugalPhase;
				this.pendingData = pendingData;
			}

			protected int getConnectionId()
			{
				return connectionId;
			}

			protected FemaleConjugalPhase getFemaleConjugalPhase()
			{
				return femaleConjugalPhase;
			}

			protected Collection<FemaleConjugalPhase> femaleConjugalPhases()
			{
				return Collections.singleton(femaleConjugalPhase);
			}

			protected byte[] getPendingData()
			{
				return pendingData;
			}

			protected abstract void splice(SocketChannel socketChannel) throws SplicePendingException, IOException;

			@Override
			public int compareTo(PendingEntry o)
			{
				return Long.compare(expires, o.expires);
			}

			private long expireWaitTime()
			{
				return expires - System.currentTimeMillis();
			}

			private boolean expired()
			{
				return expireWaitTime() <= 0;
			}

			protected abstract void close() throws IOException;

			@Override
			public String toString()
			{
				return "PendingEntry [getClass()="
						+ getClass()
						+ ", connectionId="
						+ connectionId
						+ ", expires="
						+ expires
						+ (pendingData != null ? ", pendingData="
								+ MiscUtilities.toHexString(pendingData.length <= 20 ? pendingData : Arrays.copyOf(pendingData, 20)) : "") + "]";
			}

		}

		private class SocketChannelPendingEntry extends PendingEntry
		{
			private final SocketChannel socketChannel;

			private SocketChannelPendingEntry(int connectionId, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData, SocketChannel socketChannel)
			{
				super(connectionId, femaleConjugalPhase, pendingData);
				this.socketChannel = socketChannel;
			}

			@Override
			protected void splice(SocketChannel socketChannel) throws SplicePendingException
			{
				try
				{
					socketChannelSplicer.addSplice(socketChannel, this.socketChannel);
					logger.debug("Splice added: " + socketChannel + " <-> " + this.socketChannel);
				}
				catch (ClosedChannelException e)
				{
					throw new SplicePendingException(e);
				}
			}

			@Override
			protected void close() throws IOException
			{
				socketChannel.close();
			}

			@Override
			public String toString()
			{
				return super.toString() + " [socketChannel=" + socketChannel + "]";
			}

		}

		private abstract class LocalPendingEntry extends PendingEntry
		{

			private LocalPendingEntry(int connectionId, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData)
			{
				super(connectionId, femaleConjugalPhase, pendingData);
			}

		}

		private class MaleLocalPendingEntry extends LocalPendingEntry
		{
			private SocketChannel socketChannel;
			private boolean closed;

			private MaleLocalPendingEntry(int connectionId, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData)
			{
				super(connectionId, femaleConjugalPhase, pendingData);
				this.socketChannel = null;
				this.closed = false;
			}

			@Override
			protected synchronized void splice(SocketChannel socketChannel) throws SplicePendingException
			{
				this.socketChannel = socketChannel;
				notifyAll();
			}

			private synchronized SocketChannel waitForSocketChannel(long timeout) throws InterruptedException
			{
				long limit = timeout > 0 ? System.currentTimeMillis() + timeout : -1;
				while (!closed && socketChannel == null)
				{
					if (timeout > 0)
					{
						long t1 = System.currentTimeMillis();
						if (t1 >= limit)
							return null;
						wait(limit - t1);
					}
					else
						wait();
				}
				return socketChannel;
			}

			@SuppressWarnings("unused")
			private synchronized SocketChannel waitForSocketChannel() throws InterruptedException
			{
				return waitForSocketChannel(0);
			}

			@Override
			protected synchronized void close() throws IOException
			{
				closed = true;
				if (socketChannel != null)
				{
					socketChannel.close();
					socketChannel = null;
				}
				notifyAll();
			}

			protected boolean isClosed()
			{
				return closed;
			}

		}

		private class FemaleLocalPendingEntry extends LocalPendingEntry
		{
			private FemaleLocalPendingEntry(int connectionId, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData)
			{
				super(connectionId, femaleConjugalPhase, pendingData);
			}

			@Override
			protected void splice(SocketChannel socketChannel) throws SplicePendingException, IOException
			{
				boolean added = false;
				try
				{
					addConnection(new FemalePeerToPeerConnection(PeerToPeerNode.this, socketChannel, externalAcceptedSubRootPhaseTypes));
					added = true;
				}
				catch (IOException | DisconnectingException e)
				{
					throw new SplicePendingException(e);
				}
				finally
				{
					if (!added)
						socketChannel.close();
				}
			}

			@Override
			protected void close() throws IOException
			{
			}

		}

		private class DuplexPendingEntry extends PendingEntry
		{

			private final FemaleConjugalPhase targetFemaleConjugalPhase;

			private DuplexPendingEntry(int connectionId, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData,
					FemaleConjugalPhase targetFemaleConjugalPhase)
			{
				super(connectionId, femaleConjugalPhase, pendingData);
				this.targetFemaleConjugalPhase = targetFemaleConjugalPhase;
			}

			@SuppressWarnings("unused")
			protected FemaleConjugalPhase getTargetFemaleConjugalPhase()
			{
				return targetFemaleConjugalPhase;
			}

			@Override
			protected Collection<FemaleConjugalPhase> femaleConjugalPhases()
			{
				return new CombinedCollection<FemaleConjugalPhase>(Collections.singleton(targetFemaleConjugalPhase), super.femaleConjugalPhases());
			}

			@Override
			protected void splice(SocketChannel socketChannel) throws SplicePendingException
			{
				addPending(new SocketChannelPendingEntry(getConnectionId(), getFemaleConjugalPhase(), getPendingData(), socketChannel));
			}

			@Override
			protected void close() throws IOException
			{
			}

		}

		private final HashMap<Integer, PendingEntry> pending;
		private final PriorityQueue<PendingEntry> pendingEntryExpirationQueue;
		private final HashMap<FemaleConjugalPhase, Set<PendingEntry>> pendingEntriesByConjugalPhase;

		private class ExpireThread extends Thread
		{
			private boolean shutdown = false;

			private ExpireThread()
			{
				super("SpliceManager.ExpireThread");
				setDaemon(true);
			}

			@Override
			public synchronized void run()
			{
				while (!shutdown)
				{
					try
					{
						long waitTime = nextExpireWaitTime();
						if (waitTime >= 0)
							wait(waitTime);
						expireSocketChannelEntries();
					}
					catch (InterruptedException | IOException e)
					{
						logger.warn("Exception caught", e);
					}
				}
			}

			private synchronized void shutdown() throws InterruptedException
			{
				shutdown = true;
				notifyAll();
				join();
			}

			private synchronized void myNotifyAll()
			{
				notifyAll();
			}

			private void asynchronousMyNotifyAll()
			{
				AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
				{

					@Override
					public void invoke()
					{
						myNotifyAll();
					}
				});
			}

		}

		private final ExpireThread expireThread;

		private int nextConnectionId;

		private SpliceManager() throws IOException
		{
			socketChannelSplicer = new SocketChannelSplicer("SpliceManager.socketChannelSplicer", 1024);
			pending = new HashMap<Integer, PendingEntry>();
			pendingEntryExpirationQueue = new PriorityQueue<>();
			pendingEntriesByConjugalPhase = new HashMap<FemaleConjugalPhase, Set<PendingEntry>>();
			expireThread = new ExpireThread();
			expireThread.start();
			nextConnectionId = 0;

			socketChannelSplicer.start();
		}

		private void shutdown() throws InterruptedException
		{
			socketChannelSplicer.shutdown();
			expireThread.shutdown();
		}

		private synchronized int generateConnectionId()
		{
			while (pending.containsKey(nextConnectionId))
			{
				nextConnectionId++;
				if (nextConnectionId < 0)
					nextConnectionId = 0;
			}
			return nextConnectionId++;
		}

		private synchronized void addPendingEntryByConjugalPhase(FemaleConjugalPhase femaleConjugalPhase, PendingEntry pendingEntry)
		{
			Set<PendingEntry> set = pendingEntriesByConjugalPhase.get(femaleConjugalPhase);
			if (set == null)
			{
				set = new HashSet<PendingEntry>();
				pendingEntriesByConjugalPhase.put(femaleConjugalPhase, set);
			}
			set.add(pendingEntry);
		}

		private synchronized void removePendingEntryByConjugalPhase(FemaleConjugalPhase femaleConjugalPhase, PendingEntry pendingEntry)
		{
			Set<PendingEntry> set = pendingEntriesByConjugalPhase.get(femaleConjugalPhase);
			if (set != null)
			{
				set.remove(pendingEntry);
				if (set.isEmpty())
					pendingEntriesByConjugalPhase.remove(femaleConjugalPhase);
			}
		}

		private synchronized void addPending(PendingEntry pendingEntry)
		{
			logger.trace("SpliceManager.addPending: " + pendingEntry);
			PendingEntry old = pending.put(pendingEntry.getConnectionId(), pendingEntry);
			if (old != null)
				throw new Error();
			pendingEntryExpirationQueue.offer(pendingEntry);
			expireThread.asynchronousMyNotifyAll();
			for (FemaleConjugalPhase femaleConjugalPhase : pendingEntry.femaleConjugalPhases())
				addPendingEntryByConjugalPhase(femaleConjugalPhase, pendingEntry);
		}

		private synchronized <E extends PendingEntry> E addPending(Class<E> pendingEntryClass, FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData,
				Object... args)
		{
			int connectionId = generateConnectionId();
			try
			{

				E entry = MiscUtilities.construct(pendingEntryClass,
						MiscUtilities.arrayAppend(MiscUtilities.arrayAppend(SpliceManager.this, connectionId, femaleConjugalPhase, pendingData), args));
				addPending(entry);
				return entry;
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoConstructorException e)
			{
				throw new RuntimeException(e);
			}
		}

		private synchronized PendingEntry removePending(int connectionId)
		{
			logger.trace("SpliceManager.removePending: " + connectionId);
			PendingEntry removed = pending.remove(connectionId);
			if (removed != null)
				for (FemaleConjugalPhase femaleConjugalPhase : removed.femaleConjugalPhases())
					removePendingEntryByConjugalPhase(femaleConjugalPhase, removed);
			return removed;
		}

		private synchronized void cancelPending(int connectionId) throws IOException
		{
			PendingEntry e = removePending(connectionId);
			if (e != null)
				e.close();
		}

		private synchronized <E extends PendingEntry> E addPending(Class<E> pendingEntryClass, FemaleConjugalPhase femaleConjugalPhase, Object... args)
		{
			return addPending(pendingEntryClass, femaleConjugalPhase, null, args);
		}

		private synchronized SocketChannelPendingEntry addSocketChannelPending(FemaleConjugalPhase femaleConjugalPhase, byte[] pendingData,
				SocketChannel socketChannel)
		{
			return addPending(SocketChannelPendingEntry.class, femaleConjugalPhase, pendingData, socketChannel);
		}

		private synchronized SocketChannelPendingEntry addSocketChannelPending(FemaleConjugalPhase femaleConjugalPhase, SocketChannel socketChannel)
		{
			return addPending(SocketChannelPendingEntry.class, femaleConjugalPhase, socketChannel);
		}

		private synchronized FemaleLocalPendingEntry addFemaleLocalPending(FemaleConjugalPhase femaleConjugalPhase)
		{
			return addPending(FemaleLocalPendingEntry.class, femaleConjugalPhase);
		}

		private synchronized DuplexPendingEntry addDuplexPending(FemaleConjugalPhase femaleConjugalPhase, FemaleConjugalPhase targetFemaleConjugalPhase)
		{
			return addPending(DuplexPendingEntry.class, femaleConjugalPhase, targetFemaleConjugalPhase);
		}

		private synchronized MaleLocalPendingEntry addMaleLocalPending(FemaleConjugalPhase femaleConjugalPhase)
		{
			return addPending(MaleLocalPendingEntry.class, femaleConjugalPhase);
		}

		private synchronized void splicePending(SocketChannel socketChannel, int connectionId) throws SplicePendingException
		{
			PendingEntry entry = removePending(connectionId);
			if (entry == null)
				throw new SplicePendingException();
			try
			{
				if (entry.pendingData != null)
				{
					ByteBuffer buffer = ByteBuffer.wrap(entry.pendingData);
					socketChannel.write(buffer);
				}
				entry.splice(socketChannel);
			}
			catch (IOException e)
			{
				throw new SplicePendingException(e);
			}
		}

		private synchronized long nextExpireWaitTime()
		{
			if (pendingEntryExpirationQueue.isEmpty())
				return 0;
			PendingEntry entry = pendingEntryExpirationQueue.peek();
			long expireWaitTime = entry.expireWaitTime();
			if (expireWaitTime <= 0)
				return -1;
			return expireWaitTime;
		}

		private synchronized void expireSocketChannelEntries() throws IOException
		{
			while (!pendingEntryExpirationQueue.isEmpty())
			{
				PendingEntry entry = pendingEntryExpirationQueue.peek();
				if (!entry.expired())
					break;
				cancelPending(entry.connectionId);
				pendingEntryExpirationQueue.poll();
			}
		}

		private void asynchronousFemaleOpenConnection(final FemaleConjugalPhase femaleConjugalPhase, final SplicedConnectionId splicedConnectionId)
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{
				@Override
				public void invoke()
				{
					try
					{
						femaleConjugalPhase.femaleOpenConnection(splicedConnectionId);
					}
					catch (InterruptedException | CancelledCommandException | OpenConnectionException e)
					{
						logger.error("Exception caught", e);
						try
						{
							cancelPending(splicedConnectionId.getConnectionId());
						}
						catch (IOException e1)
						{
							logger.error("Exception caught", e1);
						}
					}
				}
			});
		}

		private synchronized SplicedConnectionId newPendingSplicedSockedChannelMale(FemaleConjugalPhase femaleConjugalPhase, UUID expectedPeerNodeUuid,
				InetSocketAddress socketAddress) throws ConnectException, IOException
		{
			if (expectedPeerNodeUuid != null && expectedPeerNodeUuid.equals(getNodeUuid()) || (socketAddress == null && expectedPeerNodeUuid == null))
				return new SplicedConnectionId(addFemaleLocalPending(femaleConjugalPhase).getConnectionId());
			else
			{
				FemaleConjugalPhase targetFemaleConjugalPhase = expectedPeerNodeUuid != null ? getFemaleConjugalPhaseByMaleNodeUuid(expectedPeerNodeUuid)
						: null;
				if (targetFemaleConjugalPhase != null)
				{
					int connectionId = addDuplexPending(femaleConjugalPhase, targetFemaleConjugalPhase).getConnectionId();
					SplicedConnectionId splicedConnectionId = new SplicedConnectionId(connectionId);
					asynchronousFemaleOpenConnection(targetFemaleConjugalPhase, splicedConnectionId);
					return splicedConnectionId;
				}
				else
				{
					ConnectSocketChannel connectSocketChannel = connectSocketChannel(socketAddress, expectedPeerNodeUuid);
					return new SplicedConnectionId(addSocketChannelPending(femaleConjugalPhase, connectSocketChannel.getSocketChannel()).getConnectionId(),
							connectSocketChannel.getRemoteAddress());
				}
			}
		}

		private synchronized void newPendingSplicedSockedChannelFemale(SocketChannel socketChannel, UUID expectedPeerNodeUuid, InetAddress remoteAddress,
				byte[] pendingData) throws NewPendingSplicedSocketChannelException
		{
			FemaleConjugalPhase femaleConjugalPhase = getFemaleConjugalPhaseByMaleNodeUuid(expectedPeerNodeUuid);
			if (femaleConjugalPhase == null)
				throw new NewPendingSplicedSocketChannelException();
			PendingEntry pendingEntry = addSocketChannelPending(femaleConjugalPhase, pendingData, socketChannel);
			asynchronousFemaleOpenConnection(femaleConjugalPhase, new SplicedConnectionId(pendingEntry.getConnectionId(), remoteAddress));
		}

		private synchronized void terminatedFemaleConjugalPhase(FemaleConjugalPhase femaleConjugalPhase)
		{
			Set<PendingEntry> pendingEntries = pendingEntriesByConjugalPhase.remove(femaleConjugalPhase);
			if (pendingEntries != null)
			{
				for (PendingEntry pendingEntry : pendingEntries)
				{
					pending.remove(pendingEntry.getConnectionId());
					try
					{
						pendingEntry.close();
					}
					catch (IOException e)
					{
						logger.error("Exception caught", e);
					}
				}
			}
		}

		private synchronized void removeAllPending()
		{
			for (PendingEntry entry : pending.values())
				try
				{
					entry.close();
				}
				catch (IOException e)
				{
					logger.warn("Exception caught", e);
				}
			pending.clear();
			pendingEntriesByConjugalPhase.clear();
			pendingEntryExpirationQueue.clear();
		}

		private synchronized void closeAllSocketChannels()
		{
			removeAllPending();
			socketChannelSplicer.closeAllSocketChannels();
		}

	}

	private class FemaleConjugalPhasesByMaleNodeUuidManager
	{
		private final Map<UUID, FemaleConjugalPhase> femaleConjugalPhasesByMaleNodeUuid;
		private final Map<FemaleConjugalPhase, Set<UUID>> maleNodeUuidsByFemaleConjugalPhase;

		private FemaleConjugalPhasesByMaleNodeUuidManager()
		{
			this.femaleConjugalPhasesByMaleNodeUuid = new HashMap<UUID, FemaleConjugalPhase>();
			this.maleNodeUuidsByFemaleConjugalPhase = new HashMap<FemaleConjugalPhase, Set<UUID>>();
		}

		private synchronized Collection<UUID> maleNodeUuids()
		{
			return new BufferedList<>(new CombinedCollection<>(Collections.singleton(getNodeUuid()), femaleConjugalPhasesByMaleNodeUuid.keySet()));
		}

		private synchronized void updateMaleNodeUuids(FemaleConjugalPhase femaleConjugalPhase, Collection<UUID> addUuids, Collection<UUID> removeUuids)
		{
			if (addUuids != null)
				for (UUID uuid : addUuids)
					femaleConjugalPhasesByMaleNodeUuid.put(uuid, femaleConjugalPhase);
			if (removeUuids != null)
				for (UUID uuid : removeUuids)
					femaleConjugalPhasesByMaleNodeUuid.remove(uuid);
			Set<UUID> uuids = maleNodeUuidsByFemaleConjugalPhase.get(femaleConjugalPhase);
			if (uuids == null && addUuids != null && !addUuids.isEmpty())
			{
				uuids = new HashSet<UUID>();
				maleNodeUuidsByFemaleConjugalPhase.put(femaleConjugalPhase, uuids);
			}
			if (uuids != null && addUuids != null)
				uuids.addAll(addUuids);
			if (uuids != null && removeUuids != null)
				uuids.removeAll(removeUuids);
		}

		private synchronized FemaleConjugalPhase getFemaleConjugalPhaseByMaleNodeUuid(UUID maleNodeUuid)
		{
			return femaleConjugalPhasesByMaleNodeUuid.get(maleNodeUuid);
		}

		private synchronized void removeFemaleConjugalPhase(FemaleConjugalPhase femaleConjugalPhase)
		{
			Set<UUID> uuids = maleNodeUuidsByFemaleConjugalPhase.remove(femaleConjugalPhase);
			for (UUID uuid : uuids)
				femaleConjugalPhasesByMaleNodeUuid.remove(uuid);
		}

	}

	private final FemaleConjugalPhasesByMaleNodeUuidManager femaleConjugalPhasesByMaleNodeUuidManager;

	private class ExternalBindSocketPortManager
	{
		private int externalBindSocketPort;

		private ExternalBindSocketPortManager()
		{
			this.externalBindSocketPort = -1;
		}

		private synchronized int getExternalBindSocketPort()
		{
			return externalBindSocketPort;
		}

		private synchronized void setExternalBindSocketPort(int externalBindSocketPort)
		{
			if (this.externalBindSocketPort != externalBindSocketPort)
			{
				this.externalBindSocketPort = externalBindSocketPort;
				for (NetworkPhase networkPhase : subRootPhases(NetworkPhase.class))
					networkPhase.updateBindPort();
				for (ConjugalPhase conjugalPhase : subRootPhases(ConjugalPhase.class))
					conjugalPhase.updateBindPortIfFemale();
			}
		}

	}

	private final ExternalBindSocketPortManager externalBindSocketPortManager;

	private boolean disconnectingAll;
	private InternalServerSocketManager internalServerSocketManager;
	private SpliceManager spliceManager;
	private int sequence;

	public PeerToPeerNode(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.listeners = Collections.synchronizedCollection(new LinkedHashSet<Listener>());
		this.connectionManager = new ConnectionManager();
		this.subRootPhaseManager = new SubRootPhaseManager();
		this.persistentConnectionLock = new PeerToPeerConnectionLock();
		this.rootContextSignatureManager = new RootContextSignatureManager();
		this.followerListener = new FollowerListener();
		this.peerToPeerStatementFollower = new PeerToPeerStatementFollower(persistenceManager, followerListener);
		this.peerToPeerPersonFollower = new PeerToPeerPersonFollower(persistenceManager, followerListener);
		this.statementMalePeerToPeerConnectionManager = new StatementMalePeerToPeerConnectionManager();
		this.initializeResourcesThread = new InitializeResourcesThread();
		this.nodeUuid = UuidGenerator.instance.generate();
		logger.debug("Node identifier: " + nodeUuid);
		this.routeableSubMessageProcessor = new RouteableSubMessageProcessor(this);
		this.pendingResponses = new HashMap<Integer, ResponseRouteableSubMessage>();
		this.beltMaintainer = new BeltMaintainer();
		this.beltMaintainer.start();
		this.resourceTreeNodeSetListener = new ResourceTreeNodeSetListener();
		this.routeableSubMessageProcessor.getResourceTreeNodeSet().addListener(resourceTreeNodeSetListener);
		this.joinedToNetworkFlag = new SynchronizedFlag<JoinStatus>(JoinStatus.Joined);
		this.femaleConjugalPhasesByMaleNodeUuidManager = new FemaleConjugalPhasesByMaleNodeUuidManager();
		this.externalBindSocketPortManager = new ExternalBindSocketPortManager();

		this.disconnectingAll = false;
		this.internalServerSocketManager = null;
		this.spliceManager = null;
		this.sequence = 0;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	public PeerToPeerConnectionLock getPersistentConnectionLock()
	{
		return persistentConnectionLock;
	}

	public synchronized void internalBind(InetSocketAddress bindSocketAddress) throws InternalServerSocketManagerException
	{
		if (((bindSocketAddress == null) != (getInternalBindSocketAddress() == null))
				|| (bindSocketAddress != null && !bindSocketAddress.equals(getInternalBindSocketAddress())))
			setInternalBindSocketAddress(bindSocketAddress);
	}

	public synchronized InetSocketAddress internalBindSocketAddress()
	{
		return getInternalBindSocketAddress();
	}

	public boolean isDisconnectingAll()
	{
		return disconnectingAll;
	}

	protected void init()
	{
		initializeResourcesThread.start();
	}

	public void waitForInitializedResources() throws InterruptedException
	{
		initializeResourcesThread.join();
	}

	public abstract class ConnectException extends PeerToPeerNodeException
	{
		private static final long serialVersionUID = -6539599231499551767L;

		protected ConnectException()
		{
			super();
		}

		protected ConnectException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected ConnectException(String message)
		{
			super(message);
		}

		protected ConnectException(Throwable cause)
		{
			super(cause);
		}

	}

	public class DisconnectingException extends ConnectException
	{
		private static final long serialVersionUID = -3013455516668498352L;

	}

	public class SocketOpenException extends ConnectException
	{

		private static final long serialVersionUID = -8749249342073125846L;

		protected SocketOpenException(Exception cause)
		{
			super(cause);
		}

		@Override
		public synchronized Exception getCause()
		{
			return (Exception) super.getCause();
		}
	}

	public class ConnectTimeoutException extends ConnectException
	{
		private static final long serialVersionUID = 8620543205026279649L;

		protected ConnectTimeoutException()
		{
		}
	}

	public class InvalidSocketAddressConnectException extends ConnectException
	{
		private static final long serialVersionUID = 7430304347810411238L;

		protected InvalidSocketAddressConnectException()
		{
			super("Invalid socket address");
		}

		protected InvalidSocketAddressConnectException(String message)
		{
			super(message);
		}
	}

	public class NullSocketAddressConnectException extends InvalidSocketAddressConnectException
	{
		private static final long serialVersionUID = -8334023117124955847L;

		protected NullSocketAddressConnectException()
		{
			super("Null socket address");
		}

	}

	protected ConnectSocketChannel directConnectSocketChannel(InetSocketAddress socketAddress) throws ConnectTimeoutException, SocketOpenException,
			NullSocketAddressConnectException
	{
		if (socketAddress == null)
			throw new NullSocketAddressConnectException();
		try
		{
			Selector selector = Selector.open();
			try
			{
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);
				SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
				socketChannel.connect(socketAddress);
				selector.select((long) (connectTimeout * 1000));
				if (!selector.selectedKeys().contains(selectionKey))
					throw new ConnectTimeoutException();
				socketChannel.finishConnect();
				return new ConnectSocketChannel(socketChannel, socketAddress.getAddress()); //TODO
			}
			finally
			{
				selector.close();
			}
		}
		catch (IOException | RuntimeException e)
		{
			throw new SocketOpenException(e);
		}
	}

	protected class ConnectSocketChannel
	{
		private final SocketChannel socketChannel;
		private final InetAddress remoteAddress;

		protected ConnectSocketChannel(SocketChannel socketChannel, InetAddress remoteAddress)
		{
			super();
			this.socketChannel = socketChannel;
			this.remoteAddress = remoteAddress;
		}

		public SocketChannel getSocketChannel()
		{
			return socketChannel;
		}

		public InetAddress getRemoteAddress()
		{
			return remoteAddress;
		}

	}

	protected abstract ConnectSocketChannel connectSocketChannel(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid) throws ConnectException,
			IOException;

	private ConnectSocketChannel connectSocketChannel(InetSocketAddress socketAddress, Class<? extends MalePeerToPeerConnection> connectionClass,
			UUID expectedPeerNodeUuid) throws ConnectException, IOException
	{
		if (DirectMalePeerToPeerConnection.class.isAssignableFrom(connectionClass))
			return directConnectSocketChannel(socketAddress);
		else
		{
			FemaleConjugalPhase femaleConjugalPhase = femaleConjugalPhasesByMaleNodeUuidManager.getFemaleConjugalPhaseByMaleNodeUuid(expectedPeerNodeUuid);
			if (femaleConjugalPhase == null)
				return connectSocketChannel(socketAddress, expectedPeerNodeUuid);
			else
				return conjugalConnectSocketChannel(femaleConjugalPhase);
		}
	}

	public class ConjugalConnectException extends ConnectException
	{

		private static final long serialVersionUID = 6288839253119786085L;

		protected ConjugalConnectException(Exception cause)
		{
			super(cause);
		}

		protected ConjugalConnectException()
		{
			super();
		}

	}

	private ConnectSocketChannel conjugalConnectSocketChannel(FemaleConjugalPhase femaleConjugalPhase) throws ConnectException
	{
		SpliceManager.MaleLocalPendingEntry pendingEntry = spliceManager.addMaleLocalPending(femaleConjugalPhase);
		try
		{
			femaleConjugalPhase.femaleOpenConnection(new SplicedConnectionId(pendingEntry.getConnectionId()));
			SocketChannel socketChannel = pendingEntry.waitForSocketChannel((long) (connectTimeout * 1000));
			if (pendingEntry.isClosed())
				throw new ConjugalConnectException();
			if (socketChannel == null)
				throw new ConnectTimeoutException();
			return new ConnectSocketChannel(socketChannel, null);
		}
		catch (InterruptedException | CancelledCommandException | OpenConnectionException e)
		{
			try
			{
				spliceManager.cancelPending(pendingEntry.getConnectionId());
				pendingEntry.close();
			}
			catch (IOException e1)
			{
				logger.error("Exception caught", e1);
			}
			throw new ConjugalConnectException(e);
		}
	}

	protected <C extends MalePeerToPeerConnection> C connect(InetSocketAddress socketAddress, Class<? extends C> connectionClass, UUID expectedPeerNodeUuid,
			Object... extraArgs) throws IOException, ConnectException
	{
		logger.debug("Connecting to: " + socketAddress + "  (connectionClass: " + connectionClass + ", expectedPeerNodeUuid: " + expectedPeerNodeUuid + ")");
		if (disconnectingAll)
			throw new DisconnectingException();
		ConnectSocketChannel connectSocketChannel = connectSocketChannel(socketAddress, connectionClass, expectedPeerNodeUuid);
		SocketChannel socketChannel = connectSocketChannel.getSocketChannel();
		boolean added = false;
		try
		{
			Object[] args = MiscUtilities.arrayAppend(this, socketChannel, connectSocketChannel.getRemoteAddress());
			if (expectedPeerNodeUuid != null)
				args = MiscUtilities.arrayAppend(args, expectedPeerNodeUuid);
			args = MiscUtilities.arrayAppend(args, extraArgs);
			C connection = MiscUtilities.construct(connectionClass, args);
			addConnection(connection);
			added = true;
			return connection;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoConstructorException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (!added)
				socketChannel.close();
		}
	}

	protected <C extends MalePeerToPeerConnection> C connect(InetSocketAddress socketAddress, Class<? extends C> connectionClass, Object... extraArgs)
			throws IOException, ConnectException
	{
		return connect(socketAddress, connectionClass, null, extraArgs);
	}

	protected <C extends MalePeerToPeerConnection> C connect(NodeAddress nodeAddress, Class<? extends C> connectionClass, Object... extraArgs)
			throws IOException, ConnectException
	{
		return connect(nodeAddress.getAddress(), connectionClass, nodeAddress.getUuid(), extraArgs);
	}

	protected void addConnection(PeerToPeerConnection connection) throws DisconnectingException
	{
		connectionManager.addConnection(connection);
	}

	private boolean removeConnection(PeerToPeerConnection connection)
	{
		return connectionManager.removeConnection(connection);
	}

	public Collection<PeerToPeerConnection> connections()
	{
		return connectionManager.connections();
	}

	protected void connectionEnded(PeerToPeerConnection connection) throws IOException
	{
		try
		{
			if (removeConnection(connection))
				persistentConnectionLock.unlock(connection);
			if (connection instanceof StatementMalePeerToPeerConnection)
			{
				try
				{
					statementMalePeerToPeerConnectionManager.connectionEnded((StatementMalePeerToPeerConnection) connection);
				}
				catch (ConnectException | IOException | InterruptedException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			else if (connection instanceof FemalePeerToPeerConnection)
			{
				try
				{
					SubRootPhase subRootPhase = connection.waitForSubRootPhase();
					if (subRootPhase instanceof SplicedPhase)
					{
						boolean spliced = false;
						try
						{
							GenderedSplicedPhase genderedSplicedPhase = ((SplicedPhase) subRootPhase).getGenderedConjugalPhase();
							int connectionId = genderedSplicedPhase.getConnectionId();
							if (connectionId >= 0)
							{
								spliceManager.splicePending(connection.getSocketChannel(), connectionId);
								spliced = true;
							}
						}
						catch (SpliceManager.SplicePendingException e)
						{
							logger.warn("Exception caught (spliceManager.splicePending)", e);
						}
						finally
						{
							if (!spliced)
								connection.setShutdownSocketWhenFinish(true);
						}
					}
				}
				catch (InterruptedException e)
				{
					logger.warn("Exception caught", e);
				}
			}
			if (connection.getCaughtException() != null)
			{
				String message = "Connection to " + connection.getRemoteAddress() + " abnormally ended with exception.";
				synchronized (listeners)
				{
					for (Listener listener : listeners)
						listener.exception(message, connection.getCaughtException());
				}
			}
		}
		finally
		{
			if (connection.isShutdownSocketWhenFinish())
				connection.getSocketChannel().close();
		}
	}

	private StatementMalePeerToPeerConnection statementConnect(NodeAddress nodeAddress) throws IOException, ConnectException
	{
		return connect(nodeAddress, StatementMalePeerToPeerConnection.class);
	}

	protected JoiningNetworkMalePeerToPeerConnection joiningNetworkConnect(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid) throws IOException,
			ConnectException
	{
		return connect(socketAddress, JoiningNetworkMalePeerToPeerConnection.class, expectedPeerNodeUuid);
	}

	public JoiningNetworkMalePeerToPeerConnection joiningNetworkConnect(NodeAddress nodeAddress) throws IOException, ConnectException
	{
		return connect(nodeAddress, JoiningNetworkMalePeerToPeerConnection.class);
	}

	@Deprecated
	public JoiningNetworkMalePeerToPeerConnection joiningNetworkConnect(InetSocketAddress socketAddress) throws IOException, ConnectException
	{
		return connect(socketAddress, JoiningNetworkMalePeerToPeerConnection.class);
	}

	public ComplementingNetworkMalePeerToPeerConnection complementingNetworkConnect(NodeAddress nodeAddress) throws IOException, ConnectException
	{
		return connect(nodeAddress, ComplementingNetworkMalePeerToPeerConnection.class);
	}

	public BeltNetworkMalePeerToPeerConnection beltNetworkConnect(NodeAddress nodeAddress, Collection<Side> sides) throws IOException, ConnectException
	{
		return connect(nodeAddress, BeltNetworkMalePeerToPeerConnection.class, sides);
	}

	public BeltNetworkMalePeerToPeerConnection beltNetworkConnect(NodeAddress nodeAddress, Side side) throws IOException, ConnectException
	{
		return beltNetworkConnect(nodeAddress, EnumSet.of(side));
	}

	private EphemeralMalePeerToPeerConnection ephemeralConnect(NodeAddress nodeAddress) throws IOException, ConnectException
	{
		return connect(nodeAddress, EphemeralMalePeerToPeerConnection.class);
	}

	protected ConjugalMalePeerToPeerConnection conjugalConnect(InetSocketAddress socketAddress) throws ConnectException, IOException
	{
		return connect(socketAddress, ConjugalMalePeerToPeerConnection.class);
	}

	protected SplicedMalePeerToPeerConnection splicedConnect(NodeAddress nodeAddress, int connectionId) throws ConnectException, IOException
	{
		return connect(nodeAddress, SplicedMalePeerToPeerConnection.class, connectionId);
	}

	private Collection<PeerToPeerConnection> disconnect(boolean fast) throws IOException, InterruptedException
	{
		Collection<PeerToPeerConnection> connections = connections();
		for (PeerToPeerConnection connection : connections)
			connection.shutdown(fast, true);
		return connections;
	}

	private synchronized void internalServerSocketManagerShutdown() throws IOException, InterruptedException
	{
		if (internalServerSocketManager != null)
			internalServerSocketManager.shutdown();
	}

	private synchronized void spliceManagerShutdown() throws InterruptedException
	{
		if (spliceManager != null)
			spliceManager.shutdown();
	}

	public void shutdown(boolean fast) throws IOException, InterruptedException
	{
		disconnectingAll = true;
		initializeResourcesThread.shutdown();
		internalServerSocketManagerShutdown();
		spliceManagerShutdown();
		beltMaintainer.shutdown();
		for (NetworkPhase n : subRootPhases(NetworkPhase.class))
			n.getPeerToPeerConnection().shutdown(fast, true);
		routeableSubMessageProcessor.close();
		peerToPeerStatementFollower.unfollow();
		peerToPeerPersonFollower.unfollow();
		clearLocalResources();
		disconnect(fast);
		disconnectingAll = false;
	}

	public <P extends SubRootPhase> boolean addSubRootPhase(P subRootPhase)
	{
		return subRootPhaseManager.addSubRootPhase(subRootPhase);
	}

	public <P extends SubRootPhase> boolean removeSubRootPhase(P subRootPhase)
	{
		return subRootPhaseManager.removeSubRootPhase(subRootPhase);
	}

	public <P extends SubRootPhase> Collection<P> subRootPhases(SubRootPhaseType type)
	{
		return subRootPhaseManager.subRootPhases(type);
	}

	public <P extends SubRootPhase> Collection<P> subRootPhases(Class<P> subRootPhaseClass)
	{
		return subRootPhaseManager.subRootPhases(subRootPhaseClass);
	}

	public <P extends SubRootPhase> boolean isSubRootPhasesEmpty(SubRootPhaseType type)
	{
		return subRootPhaseManager.isSubRootPhasesEmpty(type);
	}

	public <P extends SubRootPhase> boolean isSubRootPhasesEmpty(Class<P> subRootPhaseClass)
	{
		return subRootPhaseManager.isSubRootPhasesEmpty(subRootPhaseClass);
	}

	public boolean isNetworkIsolated()
	{
		return isSubRootPhasesEmpty(NetworkPhase.class);
	}

	public Collection<NetworkPhase> networkPhases()
	{
		return subRootPhases(NetworkPhase.class);
	}

	private void receiveSignatureRequestDeferredMessageContent(UUID recipientUuid, Date date, SignatureRequestDeferredMessageContent content)
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			PrivatePerson privatePerson = persistenceManager.getPrivatePerson(transaction, recipientUuid);
			if (privatePerson == null)
				return;
			try
			{
				content.signatureRequest(persistenceManager, transaction, privatePerson.getSignatory(transaction).getPrivateKey());
				transaction.commit();
			}
			catch (DecipherException e)
			{
				logger.error("Couldn't decrypt signature request", e);
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	private void receivePersonsDeferredMessageContent(UUID recipientUuid, Date date, PersonsDeferredMessageContent content)
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			PrivatePerson privatePerson = persistenceManager.getPrivatePerson(transaction, recipientUuid);
			if (privatePerson == null)
				return;
			try
			{
				content.persons(persistenceManager, transaction, privatePerson.getSignatory(transaction).getPrivateKey());
				transaction.commit();
			}
			catch (DecipherException e)
			{
				logger.error("Couldn't decrypt signature request", e);
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	public void receiveDeferredMessageContent(UUID recipientUuid, Date date, DeferredMessageContent content)
	{
		if (content instanceof SignatureRequestDeferredMessageContent)
			receiveSignatureRequestDeferredMessageContent(recipientUuid, date, (SignatureRequestDeferredMessageContent) content);
		else if (content instanceof PersonsDeferredMessageContent)
			receivePersonsDeferredMessageContent(recipientUuid, date, (PersonsDeferredMessageContent) content);
	}

	public RootContext obtainRootContext(UUID signatureUuid, ListenableAborter aborter) throws InterruptedException, IOException, ConnectException,
			CancelledCommandException, AbortException
	{
		PersistenceManager persistenceManager = getPersistenceManager();
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				RootContextAuthority rootCtxAuth = persistenceManager.rootContextAuthorityBySignatureUuid(transaction).get(signatureUuid);
				if (rootCtxAuth != null)
					return rootCtxAuth.getRootContext(transaction);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}
		ResourceInfo resourceInfo = locateResource(new RootContextSignatureResource(signatureUuid));
		if (resourceInfo == null)
			return null;
		if (resourceInfo.getResourceMetadata() instanceof RootContextSignatureResource.Metadata)
		{
			RootContextSignatureResource.Metadata metadata = (aletheia.peertopeer.resource.RootContextSignatureResource.Metadata) resourceInfo
					.getResourceMetadata();
			if (!metadata.isProvided())
				return null;
		}
		else
			return null;
		return obtainRootContext(resourceInfo.getNodeAddress(), signatureUuid, aborter);
	}

	private RootContext obtainRootContext(NodeAddress nodeAddress, UUID signatureUuid, ListenableAborter aborter) throws ConnectException, IOException,
			InterruptedException, AbortException, CancelledCommandException
	{
		EphemeralMalePeerToPeerConnection connection = ephemeralConnect(nodeAddress);
		try
		{
			EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase(aborter);
			if (ephemeralPhase == null)
				return null;
			RootContext rootContext = ephemeralPhase.obtainRootContext(signatureUuid, aborter);
			return rootContext;
		}
		finally
		{
			connection.shutdown(false, false);
		}
	}

	private RootContext obtainRootContext(NodeAddress nodeAddress, UUID signatureUuid) throws ConnectException, IOException, InterruptedException,
			CancelledCommandException
	{
		try
		{
			return obtainRootContext(nodeAddress, signatureUuid, ListenableAborter.nullListenableAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void transmitRootContext(NodeAddress nodeAddress, UUID signatureUuid, ListenableAborter aborter) throws ConnectException, IOException,
			InterruptedException, AbortException, CancelledCommandException
	{
		EphemeralMalePeerToPeerConnection connection = ephemeralConnect(nodeAddress);
		try
		{
			EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase(aborter);
			if (ephemeralPhase == null)
				return;
			ephemeralPhase.transmitRootContext(signatureUuid);
		}
		finally
		{
			connection.shutdown(false, false);
		}
	}

	private void transmitRootContext(NodeAddress nodeAddress, UUID signatureUuid) throws ConnectException, IOException, InterruptedException,
			CancelledCommandException
	{
		try
		{
			transmitRootContext(nodeAddress, signatureUuid, ListenableAborter.nullListenableAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	public boolean sendSignatureRequest(Person person, SignatureRequest signatureRequest, ListenableAborter aborter) throws InterruptedException, IOException,
			ConnectException, AbortException, CancelledCommandException
	{
		ResourceInfo resourceInfo = locateResource(new PrivatePersonResource(person.getUuid()));
		if (resourceInfo != null)
		{
			NodeAddress nodeAddress = resourceInfo.getNodeAddress();
			EphemeralMalePeerToPeerConnection connection = ephemeralConnect(nodeAddress);
			try
			{
				EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase(aborter);
				if (ephemeralPhase == null)
					return false;
				boolean sent = ephemeralPhase.sendSignatureRequest(signatureRequest, aborter);
				return sent;
			}
			finally
			{
				connection.shutdown(false, false);
			}

		}
		else
			return sendDeferredSignatureRequest(person, signatureRequest, aborter);
	}

	private boolean sendDeferredSignatureRequest(Person person, SignatureRequest signatureRequest, ListenableAborter aborter) throws IOException,
			ConnectException, InterruptedException, AbortException, CancelledCommandException
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			SignatureRequestDeferredMessageContent signatureRequestDeferredMessageContent = new SignatureRequestDeferredMessageContent(getPersistenceManager(),
					transaction, person.getSignatory(transaction).getPublicKey(), signatureRequest);
			boolean sent = sendDeferredMessage(person.getUuid(), signatureRequestDeferredMessageContent, aborter);
			transaction.commit();
			return sent;
		}
		finally
		{
			transaction.abort();
		}
	}

	private boolean sendDeferredMessage(UUID recipientUuid, DeferredMessageContent content, ListenableAborter aborter) throws IOException, ConnectException,
			InterruptedException, AbortException, CancelledCommandException
	{
		NodeAddress closest = closestNodeAddress(recipientUuid);
		if (closest.getUuid().equals(getNodeUuid()))
		{
			seedDeferredMessage(recipientUuid, content);
			return true;
		}
		else
		{
			while (closest != null)
			{
				EphemeralMalePeerToPeerConnection connection = ephemeralConnect(closest);
				try
				{
					EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase(aborter);
					if (ephemeralPhase == null)
						return false;
					closest = ephemeralPhase.sendDeferredMessage(recipientUuid, content, aborter);
				}
				finally
				{
					connection.shutdown(false, false);
				}
			}
			return true;
		}
	}

	@Deprecated
	public boolean sendDeferredMessage(UUID recipientUuid, aletheia.model.peertopeer.deferredmessagecontent.DummyDeferredMessageContent content,
			ListenableAborter aborter) throws IOException, ConnectException, InterruptedException, AbortException, CancelledCommandException
	{
		return sendDeferredMessage(recipientUuid, (DeferredMessageContent) content, aborter);
	}

	public void localResourceUpdatedNextLocation(ResourceTreeNodeSet.Location nextLocation) throws ConnectException, IOException, InterruptedException
	{
		Resource.Metadata metadata = nextLocation.getResourceMetadata();
		NodeAddress nodeAddress = nextLocation.getNodeAddress();
		if (metadata instanceof SubscribedStatementsContextResource.Metadata)
			statementMalePeerToPeerConnectionManager.obtainConnection(metadata.getResource(), nodeAddress);
		else if (metadata instanceof StatementProofResource.Metadata)
		{
			StatementProofResource.Metadata nextMetadata = (StatementProofResource.Metadata) metadata;
			StatementProofResource.Metadata localMetadata = (StatementProofResource.Metadata) getLocalResourceMetadata(metadata.getResource());
			if (localMetadata.isSignedProof() && !nextMetadata.isSignedProof() && nextMetadata.isSubscribed())
				statementMalePeerToPeerConnectionManager.obtainConnection(metadata.getResource(), nodeAddress);
			else if (!localMetadata.isSignedProof() && localMetadata.isSubscribed() && nextMetadata.isSignedProof())
				statementMalePeerToPeerConnectionManager.obtainConnection(metadata.getResource(), nodeAddress);
			else
				statementMalePeerToPeerConnectionManager.unregisterResource(metadata.getResource());
		}
		else if (metadata instanceof RootContextSignatureResource.Metadata)
		{
			RootContextSignatureResource.Metadata nextMetadata = (RootContextSignatureResource.Metadata) metadata;
			RootContextSignatureResource.Metadata localMetadata = (RootContextSignatureResource.Metadata) getLocalResourceMetadata(metadata.getResource());
			if (!localMetadata.isProvided() && localMetadata.isSubscribed() && nextMetadata.isProvided())
			{
				try
				{
					obtainRootContext(nodeAddress, metadata.getResource().getUuid());
				}
				catch (CancelledCommandException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			else if (!nextMetadata.isProvided() && nextMetadata.isSubscribed() && localMetadata.isProvided())
			{
				try
				{
					transmitRootContext(nodeAddress, metadata.getResource().getUuid());
				}
				catch (CancelledCommandException e)
				{
					logger.error(e.getMessage(), e);
				}
			}

		}
		else if (metadata instanceof PersonResource.Metadata)
		{
			PersonResource.Metadata personResourceMetadata = (PersonResource.Metadata) metadata;
			updatePersonResourceMetadata(personResourceMetadata);
			PersonResource.Metadata localPersonResourceMetadata = (PersonResource.Metadata) getLocalResourceMetadata(personResourceMetadata.getResource());
			if (localPersonResourceMetadata != null)
			{
				PersonResource.Metadata.PersonInfo localPersonInfo = localPersonResourceMetadata.getPersonInfo();
				PersonResource.Metadata.PersonInfo nextPersonInfo = personResourceMetadata.getPersonInfo();
				if (localPersonInfo.compareTo(nextPersonInfo) > 0)
					sendResourceMetadata(nextLocation.getNodeAddress().getUuid(), localPersonResourceMetadata);
			}
		}
	}

	public void localResourceRemovedNextLocation(Resource resource) throws IOException, InterruptedException
	{
		statementMalePeerToPeerConnectionManager.unregisterResource(resource);
	}

	public void rootContextSignatureUpdated(UUID rootContextUuid)
	{
		rootContextSignatureManager.rootContextSignatureUpdated(rootContextUuid);
	}

	public void rootContextSignatureSubscribe(UUID signatureUuid, boolean subscribe)
	{
		rootContextSignatureManager.rootContextSignatureSubscribe(signatureUuid, subscribe);
	}

	public void receiveResourceMetadata(UUID origin, Resource.Metadata resourceMetadata)
	{
		if (resourceMetadata instanceof PersonResource.Metadata)
			updatePersonResourceMetadata((PersonResource.Metadata) resourceMetadata);
	}

	private void updatePersonResourceMetadata(PersonResource.Metadata personResourceMetadata)
	{
		PersonResource.Metadata.PersonInfo personInfo = personResourceMetadata.getPersonInfo();
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			Person person = persistenceManager.getPerson(transaction, personResourceMetadata.getResource().getUuid());
			if (person != null)
				try
				{
					personInfo.update(transaction, person);
				}
				catch (SignatureVerifyException e)
				{
					logger.error("Can't update person", e);
				}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}

	}

	public boolean sendPersons(Person recipient, Collection<Person> persons, ListenableAborter aborter) throws InterruptedException, AbortException,
			ConnectException, IOException, CancelledCommandException
	{
		ResourceInfo resourceInfo = locateResource(new PrivatePersonResource(recipient.getUuid()));
		if (resourceInfo != null)
		{
			NodeAddress nodeAddress = resourceInfo.getNodeAddress();
			EphemeralMalePeerToPeerConnection connection = ephemeralConnect(nodeAddress);
			try
			{
				EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase(aborter);
				if (ephemeralPhase == null)
					return false;
				ephemeralPhase.persons(persons, aborter);
				return true;
			}
			finally
			{
				connection.shutdown(false, false);
			}
		}
		else
		{
			return sendDeferredPersons(recipient, persons, aborter);
		}

	}

	private boolean sendDeferredPersons(Person recipient, Collection<Person> persons, ListenableAborter aborter) throws IOException, ConnectException,
			InterruptedException, AbortException, CancelledCommandException
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			PersonsDeferredMessageContent personsDeferredMessageContent = new PersonsDeferredMessageContent(getPersistenceManager(), transaction, recipient
					.getSignatory(transaction).getPublicKey(), persons);
			boolean sent = sendDeferredMessage(recipient.getUuid(), personsDeferredMessageContent, aborter);
			transaction.commit();
			return sent;
		}
		finally
		{
			transaction.abort();
		}
	}

	public boolean checkConnectSocketAddress(NodeAddress nodeAddress) throws InterruptedException, ConnectException, IOException
	{
		EphemeralMalePeerToPeerConnection connection = ephemeralConnect(nodeAddress);
		try
		{
			EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase();
			if (ephemeralPhase == null)
				return false;
			return true;
		}
		finally
		{
			connection.shutdown(false, false);
		}
	}

	public UUID getNodeUuid()
	{
		return nodeUuid;
	}

	public RouteableSubMessageProcessor getRouteableSubMessageProcessor()
	{
		return routeableSubMessageProcessor;
	}

	public synchronized InetSocketAddress getInternalBindSocketAddress()
	{
		if (internalServerSocketManager == null)
			return null;
		else
			return internalServerSocketManager.getBindSocketAddress();
	}

	private InternalServerSocketManager internalServerSocketManagerStart(InetSocketAddress bindSocketAddress) throws InternalServerSocketManagerException
	{
		try
		{
			InternalServerSocketManager serverSocketManager = new InternalServerSocketManager(bindSocketAddress);
			serverSocketManager.start();
			return serverSocketManager;
		}
		catch (IOException e)
		{
			throw new InternalServerSocketManagerException(e);
		}
	}

	public synchronized void setInternalBindSocketAddress(InetSocketAddress bindSocketAddress) throws InternalServerSocketManagerException
	{
		try
		{
			if (internalServerSocketManager != null)
				internalServerSocketManager.shutdown();
			if (bindSocketAddress == null)
			{
				internalServerSocketManager = null;
				if (spliceManager != null)
				{
					spliceManager.shutdown();
					spliceManager = null;
				}
			}
			else
			{
				internalServerSocketManager = internalServerSocketManagerStart(bindSocketAddress);
				if (spliceManager == null)
					spliceManager = new SpliceManager();
			}
		}
		catch (IOException | InterruptedException e)
		{
			throw new InternalServerSocketManagerException(e);
		}
	}

	public LocalRouterSet getLocalRouterSet()
	{
		return getRouteableSubMessageProcessor().getLocalRouterSet();
	}

	public Belt getBelt()
	{
		return getRouteableSubMessageProcessor().getBelt();
	}

	public ResourceTreeNodeSet getResourceTreeNodeSet()
	{
		return getRouteableSubMessageProcessor().getResourceTreeNodeSet();
	}

	public DeferredMessageSet getDeferredMessageSet()
	{
		return getRouteableSubMessageProcessor().getDeferredMessageSet();
	}

	public CumulationSet getCumulationSet()
	{
		return getRouteableSubMessageProcessor().getCumulationSet();
	}

	public int sequence()
	{
		return sequence++;
	}

	private synchronized ResponseRouteableSubMessage waitForResponse(int sequence, long timeout) throws InterruptedException
	{
		if (!pendingResponses.containsKey(sequence))
			throw new IllegalStateException();
		long limit = timeout > 0 ? System.currentTimeMillis() + timeout : 0;
		while (true)
		{
			ResponseRouteableSubMessage response = pendingResponses.get(sequence);
			if (response != null)
				return response;
			long timeout_ = limit > 0 ? limit - System.currentTimeMillis() : 0;
			if (limit > 0 && timeout_ <= 0)
				return null;
			wait(timeout_);
		}
	}

	@SuppressWarnings("unused")
	private synchronized ResponseRouteableSubMessage waitForResponse(int sequence) throws InterruptedException
	{
		return waitForResponse(sequence, 0);
	}

	private synchronized <M extends ResponseRouteableSubMessage> M waitForResponse(Class<? extends M> clazz, int sequence, long timeout)
			throws InterruptedException
	{
		ResponseRouteableSubMessage response = waitForResponse(sequence, timeout);
		if (!clazz.isInstance(response))
			return null;
		return clazz.cast(response);
	}

	@SuppressWarnings("unused")
	private synchronized <M extends ResponseRouteableSubMessage> M waitForResponse(Class<? extends M> clazz, int sequence) throws InterruptedException
	{
		return waitForResponse(clazz, sequence, 0);
	}

	public synchronized void pendingResponse(ResponseRouteableSubMessage response)
	{
		if (pendingResponses.containsKey(response.getSequenceResponse()))
		{
			pendingResponses.put(response.getSequenceResponse(), response);
			notifyAll();
		}
	}

	private synchronized void registerResponse(int sequence)
	{
		if (pendingResponses.containsKey(sequence))
			throw new IllegalStateException();
		pendingResponses.put(sequence, null);
	}

	private synchronized void unregisterResponse(int sequence)
	{
		if (!pendingResponses.containsKey(sequence))
			throw new IllegalStateException();
		pendingResponses.remove(sequence);
	}

	private <R extends ResponseRouteableSubMessage> R processRequestWaitForResponse(RouteableSubMessage request, Class<R> responseClass)
			throws InterruptedException
	{
		registerResponse(request.getSequence());
		try
		{
			while (true)
			{
				if (!routeableSubMessageProcessor.process(request))
					return null;
				R response = waitForResponse(responseClass, request.getSequence(), requestResponseTimeout);
				if (response != null)
					return response;
			}
		}
		finally
		{
			unregisterResponse(request.getSequence());
		}
	}

	public NodeAddress closestNodeAddress(UUID uuid) throws InterruptedException
	{
		ClosestNodeResponseRouteableSubMessage response = processRequestWaitForResponse(new ClosestNodeRouteableSubMessage(getNodeUuid(), sequence(), uuid),
				ClosestNodeResponseRouteableSubMessage.class);
		return response.nodeAddress();
	}

	public ResourceInfo locateResource(Resource resourceMetadata) throws InterruptedException
	{
		LocateResourceResponseRouteableSubMessage response = processRequestWaitForResponse(new LocateResourceRouteableSubMessage(getNodeUuid(), sequence(),
				resourceMetadata), LocateResourceResponseRouteableSubMessage.class);
		if (response instanceof FoundLocateResourceResponseRouteableSubMessage)
			return ((FoundLocateResourceResponseRouteableSubMessage) response).resourceInfo();
		else
			return null;
	}

	private boolean complementingInvitation(int slot)
	{
		ComplementingInvitationRouteableSubMessage complementingInvitationRouteableSubMessage = new ComplementingInvitationRouteableSubMessage(getNodeUuid(),
				sequence(), slot, !MiscUtilities.bitAt(getNodeUuid(), slot));
		return routeableSubMessageProcessor.process(complementingInvitationRouteableSubMessage);
	}

	public void sendComplementingInvitations()
	{
		LocalRouterSet localRouterSet = getLocalRouterSet();
		synchronized (localRouterSet)
		{
			for (int slot : localRouterSet.freeNeighbourSlots())
			{
				complementingInvitation(slot);
			}
		}
	}

	public void sendComplementingInvitation(int slot)
	{
		LocalRouterSet localRouterSet = getLocalRouterSet();
		synchronized (localRouterSet)
		{
			complementingInvitation(slot);
		}
	}

	public boolean sendBeltConnect(NetworkPhase fromNetworkPhase)
	{
		boolean waitForCompletion = true;
		Belt belt = getBelt();
		synchronized (belt)
		{
			if (belt.getLeft() == null)
			{
				boolean processed = routeableSubMessageProcessor.process(new BeltConnectRouteableSubMessage(getNodeUuid(), sequence(), getNodeUuid(),
						Side.Right), fromNetworkPhase);
				if (!processed)
					waitForCompletion = false;
			}
			if (belt.getRight() == null)
			{
				boolean processed = routeableSubMessageProcessor.process(
						new BeltConnectRouteableSubMessage(getNodeUuid(), sequence(), getNodeUuid(), Side.Left), fromNetworkPhase);
				if (!processed)
					waitForCompletion = false;
			}
		}
		return waitForCompletion;
	}

	public boolean sendBeltConnect()
	{
		return sendBeltConnect(null);
	}

	public void putLocalResource(Resource.Metadata resourceMetadata)
	{
		routeableSubMessageProcessor.putLocalResource(resourceMetadata);
	}

	public void removeLocalResource(Resource resource)
	{
		routeableSubMessageProcessor.removeLocalResource(resource);
	}

	public boolean isLocalResource(Resource resource)
	{
		return routeableSubMessageProcessor.isLocalResource(resource);
	}

	public Resource.Metadata getLocalResourceMetadata(Resource resource)
	{
		return routeableSubMessageProcessor.getLocalResourceMetadata(resource);
	}

	public Location getNextLocation(Resource resource)
	{
		return routeableSubMessageProcessor.getNextLocation(resource);
	}

	private synchronized void clearLocalResources()
	{
		routeableSubMessageProcessor.clearLocalResources();
	}

	public void seedDeferredMessage(UUID recipientUuid, DeferredMessageContent content)
	{
		routeableSubMessageProcessor.seedDeferredMessage(recipientUuid, content);
	}

	public boolean transmitDeferredMessages(UUID recipientUuid, Collection<DeferredMessage> deferredMessages) throws InterruptedException, IOException,
			ConnectException, CancelledCommandException
	{
		PrivatePersonResource privatePersonResourceMetadata = new PrivatePersonResource(recipientUuid);
		if (isLocalResource(privatePersonResourceMetadata))
		{
			for (DeferredMessage deferredMessage : deferredMessages)
				receiveDeferredMessageContent(deferredMessage.getRecipientUuid(), deferredMessage.getDate(), deferredMessage.getContent());
			return true;
		}
		else
		{
			ResourceInfo resourceInfo = locateResource(privatePersonResourceMetadata);
			if (resourceInfo == null)
				return false;
			NodeAddress nodeAddress = resourceInfo.getNodeAddress();
			EphemeralMalePeerToPeerConnection connection = ephemeralConnect(nodeAddress);
			try
			{
				EphemeralPhase ephemeralPhase = connection.waitForSubRootPhase();
				if (ephemeralPhase == null)
					return false;
				ephemeralPhase.transmitDeferredMessages(deferredMessages);
				return true;
			}
			finally
			{
				connection.shutdown(false, false);
			}
		}
	}

	public double networkSizeEstimation()
	{
		return routeableSubMessageProcessor.networkSizeEstimation();
	}

	public boolean sendResourceMetadata(UUID target, Resource.Metadata resourceMetadata)
	{
		ResourceMetadataRouteableSubMessage resourceMetadataRouteableSubMessage = new ResourceMetadataRouteableSubMessage(getNodeUuid(), sequence(), target,
				resourceMetadata);
		return routeableSubMessageProcessor.process(resourceMetadataRouteableSubMessage);
	}

	protected boolean isJoinedToNetwork()
	{
		return JoinStatus.Joined.equals(joinedToNetworkFlag.getValue());
	}

	@SuppressWarnings("unused")
	private boolean isNotJoiningToNetwork()
	{
		return JoinStatus.NotJoining.equals(joinedToNetworkFlag.getValue());
	}

	public void setJoinedToNetwork()
	{
		setJoinedToNetworkValue(JoinStatus.Joined);
	}

	public void setNotJoiningToNetwork()
	{
		setJoinedToNetworkValue(JoinStatus.NotJoining);
	}

	public void setNotJoiningToNetworkIfPending()
	{
		setJoinedToNetworkValueIfPending(JoinStatus.NotJoining);
	}

	private void setJoinedToNetworkValue(JoinStatus joinStatus)
	{
		synchronized (joinedToNetworkFlag)
		{
			if (joinedToNetworkFlag.getValue() != JoinStatus.Pending)
				throw new IllegalStateException();
			joinedToNetworkFlag.setValue(joinStatus);
		}
	}

	private void setJoinedToNetworkValueIfPending(JoinStatus joinStatus)
	{
		synchronized (joinedToNetworkFlag)
		{
			if (joinedToNetworkFlag.getValue() == JoinStatus.Pending)
				joinedToNetworkFlag.setValue(joinStatus);
		}
	}

	protected void clearJoinedToNetwork()
	{
		joinedToNetworkFlag.setValue(JoinStatus.Pending);
	}

	public void waitForJoinedToNetwork() throws InterruptedException
	{
		waitForJoinedToNetwork(0);
	}

	public class JoinedToNetworkTimeoutException extends Exception
	{
		private static final long serialVersionUID = -5985000471093952221L;

		private JoinedToNetworkTimeoutException()
		{

		}
	}

	public void waitForJoinedToNetworkTimeout(long timeout) throws InterruptedException, JoinedToNetworkTimeoutException
	{
		synchronized (joinedToNetworkFlag)
		{
			EnumSet<JoinStatus> set = EnumSet.of(JoinStatus.Joined, JoinStatus.NotJoining);
			joinedToNetworkFlag.waitForValue(set, timeout);
			if (!set.contains(joinedToNetworkFlag.getValue()))
				throw new JoinedToNetworkTimeoutException();
		}
	}

	protected void waitForJoinedToNetwork(long timeout) throws InterruptedException
	{
		joinedToNetworkFlag.waitForValue(EnumSet.of(JoinStatus.Joined, JoinStatus.NotJoining), timeout);
	}

	public int getExternalBindSocketPort()
	{
		return externalBindSocketPortManager.getExternalBindSocketPort();
	}

	public void setExternalBindSocketPort(int externalBindSocketPort)
	{
		externalBindSocketPortManager.setExternalBindSocketPort(externalBindSocketPort);
	}

	public class NewPendingSplicedSocketChannelException extends ConnectException
	{
		private static final long serialVersionUID = 5711872162383354996L;

		private NewPendingSplicedSocketChannelException()
		{
			super();
		}

		private NewPendingSplicedSocketChannelException(Throwable cause)
		{
			super(cause);
		}

	}

	public SplicedConnectionId newPendingSplicedSockedChannelMale(FemaleConjugalPhase femaleConjugalPhase, UUID expectedPeerNodeUuid,
			InetSocketAddress socketAddress) throws ConnectException, IOException
	{
		try
		{
			return spliceManager.newPendingSplicedSockedChannelMale(femaleConjugalPhase, expectedPeerNodeUuid, socketAddress);
		}
		catch (NullPointerException e)
		{
			throw new NewPendingSplicedSocketChannelException(e);
		}
	}

	public void newPendingSplicedSockedChannelFemale(SocketChannel socketChannel, UUID expectedPeerNodeUuid, InetAddress remoteAddress, byte[] pendingData)
			throws NewPendingSplicedSocketChannelException
	{
		try
		{
			spliceManager.newPendingSplicedSockedChannelFemale(socketChannel, expectedPeerNodeUuid, remoteAddress, pendingData);
		}
		catch (NullPointerException e)
		{
			throw new NewPendingSplicedSocketChannelException(e);
		}
	}

	public Collection<UUID> maleNodeUuids()
	{
		return femaleConjugalPhasesByMaleNodeUuidManager.maleNodeUuids();
	}

	public void updateMaleNodeUuids(FemaleConjugalPhase femaleConjugalPhase, Collection<UUID> addUuids, Collection<UUID> removeUuids)
	{
		femaleConjugalPhasesByMaleNodeUuidManager.updateMaleNodeUuids(femaleConjugalPhase, addUuids, removeUuids);
	}

	public FemaleConjugalPhase getFemaleConjugalPhaseByMaleNodeUuid(UUID maleNodeUuid)
	{
		return femaleConjugalPhasesByMaleNodeUuidManager.getFemaleConjugalPhaseByMaleNodeUuid(maleNodeUuid);
	}

	public boolean networkJoin(InetSocketAddress hookSocketAddress) throws IOException, InterruptedException, ConnectException
	{
		return networkJoin(hookSocketAddress, null);
	}

	private boolean networkJoin(InetSocketAddress hookSocketAddress, UUID expectedPeerNodeUuid) throws IOException, InterruptedException, ConnectException
	{
		asynchronousNetworkJoin(hookSocketAddress, expectedPeerNodeUuid);
		waitForJoinedToNetwork((long) (joinedToNetworkTimeout * 1000));
		return isJoinedToNetwork();
	}

	protected void asynchronousNetworkJoin(InetSocketAddress hookSocketAddress, UUID expectedPeerNodeUuid) throws InterruptedException, ConnectException,
			IOException
	{
		waitForJoinedToNetwork();
		clearJoinedToNetwork();
		try
		{
			joiningNetworkConnect(hookSocketAddress, expectedPeerNodeUuid);
		}
		catch (Exception e)
		{
			setNotJoiningToNetworkIfPending();
			throw e;
		}
	}

	public void terminatedFemaleConjugalPhase(FemaleConjugalPhase femaleConjugalPhase)
	{
		femaleConjugalPhasesByMaleNodeUuidManager.removeFemaleConjugalPhase(femaleConjugalPhase);
		spliceManager.terminatedFemaleConjugalPhase(femaleConjugalPhase);
	}

	private synchronized void spliceManagerCloseAllSocketChannels()
	{
		if (spliceManager != null)
			spliceManager.closeAllSocketChannels();
	}

	protected void closeAllSocketChannels()
	{
		for (PeerToPeerConnection connection : connections())
		{
			try
			{
				connection.getSocketChannel().close();
			}
			catch (IOException e)
			{
				logger.warn("Exception caught", e);
			}
		}
		spliceManagerCloseAllSocketChannels();
	}

}
