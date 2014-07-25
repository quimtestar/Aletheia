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
package aletheia.peertopeer.statement.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.Person;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.authority.DelegateAuthorizerProtocol;
import aletheia.protocol.collection.MapProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = DelegateAuthorizerResponseMessage.SubProtocol.class)
public class DelegateAuthorizerResponseMessage extends AbstractUUIDPersistentInfoMessage<DelegateAuthorizerResponseMessage.DelegateTreeRootNodeInfo>
{

	public static class ProtocolSendException extends RuntimeException
	{
		private static final long serialVersionUID = 6610164988922518923L;

		private ProtocolSendException()
		{
			super();
		}

		private ProtocolSendException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private ProtocolSendException(String message)
		{
			super(message);
		}

		private ProtocolSendException(Throwable cause)
		{
			super(cause);
		}

	}

	private static abstract class DelegateTreeNodeInfo implements Exportable
	{
		private final Map<UUID, DelegateAuthorizer> delegateAuthorizers;
		private final Map<String, DelegateTreeSubNodeInfo> subNodes;

		private DelegateTreeNodeInfo(DelegateTreeNode delegateTreeNode, Map<UUID, DelegateAuthorizer> delegateAuthorizers,
				Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super();
			this.delegateAuthorizers = delegateAuthorizers;
			this.subNodes = subNodes;
		}

		private DelegateTreeNodeInfo(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode,
				DelegateAuthorizerRequestMessage.DelegateTreeNodeInfo delegateAuthorizerRequestMessageNode)
		{
			super();
			this.delegateAuthorizers = new HashMap<UUID, DelegateAuthorizer>();
			this.subNodes = new HashMap<String, DelegateTreeSubNodeInfo>();

			for (UUID delegateUuid : delegateAuthorizerRequestMessageNode.getDelegateUuids())
			{
				Person delegate = persistenceManager.getPerson(transaction, delegateUuid);
				if (delegate != null)
				{
					DelegateAuthorizer delegateAuthorizer = delegateTreeNode.getDelegateAuthorizer(transaction, delegate);
					if (delegateAuthorizer != null && delegateAuthorizer.isSigned())
						delegateAuthorizers.put(delegateUuid, delegateAuthorizer);
				}
			}
			for (Map.Entry<String, DelegateAuthorizerRequestMessage.DelegateTreeSubNodeInfo> e : delegateAuthorizerRequestMessageNode.getSubNodes().entrySet())
			{
				try
				{
					DelegateTreeSubNode delegateTreeSubNode = delegateTreeNode.localDelegateTreeSubNodeMap(transaction).get(
							new NodeNamespace(delegateTreeNode.getPrefix(), e.getKey()));
					if (delegateTreeSubNode != null)
						subNodes.put(e.getKey(), new DelegateTreeSubNodeInfo(persistenceManager, transaction, delegateTreeSubNode, e.getValue()));
				}
				catch (InvalidNameException e1)
				{
					throw new RuntimeException(e1);
				}
			}
		}

		protected Map<UUID, DelegateAuthorizer> getDelegateAuthorizers()
		{
			return delegateAuthorizers;
		}

		protected Map<String, DelegateTreeSubNodeInfo> getSubNodes()
		{
			return subNodes;
		}

		@ProtocolInfo(availableVersions = 0)
		private abstract static class MyProtocol<I extends DelegateTreeNodeInfo> extends PersistentExportableProtocol<I>
		{
			private final static UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			private final static StringProtocol stringProtocol = new StringProtocol(0);
			private final static IntegerProtocol integerProtocol = new IntegerProtocol(0);

			private final DelegateTreeNode delegateTreeNode;
			private final DelegateAuthorizerProtocol delegateAuthorizerProtocol;
			private final MapProtocol<UUID, DelegateAuthorizer> delegateAuthorizerMapProtocol;

			private MyProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode)
			{
				super(0, persistenceManager, transaction);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
				this.delegateTreeNode = delegateTreeNode;
				this.delegateAuthorizerProtocol = new DelegateAuthorizerProtocol(0, persistenceManager, transaction, delegateTreeNode);
				this.delegateAuthorizerMapProtocol = new MapProtocol<UUID, DelegateAuthorizer>(0, uuidProtocol, delegateAuthorizerProtocol);
			}

			protected DelegateTreeNode getDelegateTreeNode()
			{
				return delegateTreeNode;
			}

			@Override
			public void send(DataOutput out, I delegateTreeNodeInfo) throws IOException
			{
				delegateAuthorizerMapProtocol.send(out, delegateTreeNodeInfo.getDelegateAuthorizers());
				integerProtocol.send(out, delegateTreeNodeInfo.getSubNodes().size());
				for (Map.Entry<String, DelegateTreeSubNodeInfo> e : delegateTreeNodeInfo.getSubNodes().entrySet())
				{
					stringProtocol.send(out, e.getKey());
					try
					{
						DelegateTreeSubNode delegateTreeSubNode = delegateTreeNode.getSubNode(getTransaction(), e.getKey());
						if (delegateTreeSubNode == null)
							throw new ProtocolSendException();
						DelegateTreeSubNodeInfo.MyProtocol delegateTreeSubNodeProtocol = new DelegateTreeSubNodeInfo.MyProtocol(0, getPersistenceManager(),
								getTransaction(), delegateTreeSubNode);
						delegateTreeSubNodeProtocol.send(out, e.getValue());
					}
					catch (InvalidNameException e1)
					{
						throw new ProtocolSendException(e1);
					}
				}
			}

			protected abstract I recv(Map<UUID, DelegateAuthorizer> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes, DataInput in)
					throws IOException, ProtocolException;

			@Override
			public I recv(DataInput in) throws IOException, ProtocolException
			{
				Map<UUID, DelegateAuthorizer> delegateAuthorizers = delegateAuthorizerMapProtocol.recv(in);
				int n = integerProtocol.recv(in);
				Map<String, DelegateTreeSubNodeInfo> subNodes = new HashMap<String, DelegateTreeSubNodeInfo>(n);
				for (int i = 0; i < n; i++)
				{
					String name = stringProtocol.recv(in);
					try
					{
						DelegateTreeSubNode delegateTreeSubNode = delegateTreeNode.getSubNode(getTransaction(), name);
						if (delegateTreeSubNode == null)
							throw new ProtocolException();
						DelegateTreeSubNodeInfo.MyProtocol delegateTreeSubNodeProtocol = new DelegateTreeSubNodeInfo.MyProtocol(0, getPersistenceManager(),
								getTransaction(), delegateTreeSubNode);
						DelegateTreeSubNodeInfo delegateTreeSubNodeInfo = delegateTreeSubNodeProtocol.recv(in);
						subNodes.put(name, delegateTreeSubNodeInfo);
					}
					catch (InvalidNameException e)
					{
						throw new ProtocolException(e);
					}
				}
				return recv(delegateAuthorizers, subNodes, in);
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				delegateAuthorizerMapProtocol.skip(in);
				DelegateTreeSubNodeInfo.MyProtocol delegateTreeSubNodeProtocol = new DelegateTreeSubNodeInfo.MyProtocol(0, getPersistenceManager(),
						getTransaction(), null);
				int n = integerProtocol.recv(in);
				for (int i = 0; i < n; i++)
				{
					stringProtocol.skip(in);
					delegateTreeSubNodeProtocol.skip(in);
				}
			}

		}

	}

	public static class DelegateTreeRootNodeInfo extends DelegateTreeNodeInfo
	{
		private DelegateTreeRootNodeInfo(DelegateTreeRootNode delegateTreeRootNode, Map<UUID, DelegateAuthorizer> delegateAuthorizers,
				Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super(delegateTreeRootNode, delegateAuthorizers, subNodes);
		}

		public DelegateTreeRootNodeInfo(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode,
				DelegateAuthorizerRequestMessage.DelegateTreeRootNodeInfo delegateAuthorizerRequestMessageRootNode)
		{
			super(persistenceManager, transaction, delegateTreeRootNode, delegateAuthorizerRequestMessageRootNode);
		}

		@ProtocolInfo(availableVersions = 0)
		private static class MyProtocol extends DelegateTreeNodeInfo.MyProtocol<DelegateTreeRootNodeInfo>
		{
			private MyProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode)
			{
				super(0, persistenceManager, transaction, delegateTreeRootNode);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			protected DelegateTreeRootNode getDelegateTreeNode()
			{
				return (DelegateTreeRootNode) super.getDelegateTreeNode();
			}

			@Override
			protected DelegateTreeRootNodeInfo recv(Map<UUID, DelegateAuthorizer> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes,
					DataInput in) throws IOException, ProtocolException
			{
				return new DelegateTreeRootNodeInfo(getDelegateTreeNode(), delegateAuthorizers, subNodes);
			}

		}

	}

	private static class DelegateTreeSubNodeInfo extends DelegateTreeNodeInfo
	{
		private DelegateTreeSubNodeInfo(DelegateTreeSubNode delegateTreeSubNode, Map<UUID, DelegateAuthorizer> delegateAuthorizers,
				Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super(delegateTreeSubNode, delegateAuthorizers, subNodes);
		}

		private DelegateTreeSubNodeInfo(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeSubNode delegateTreeSubNode,
				DelegateAuthorizerRequestMessage.DelegateTreeSubNodeInfo delegateAuthorizerRequestMessageSubNode)
		{
			super(persistenceManager, transaction, delegateTreeSubNode, delegateAuthorizerRequestMessageSubNode);
		}

		@ProtocolInfo(availableVersions = 0)
		static class MyProtocol extends DelegateTreeNodeInfo.MyProtocol<DelegateTreeSubNodeInfo>
		{
			MyProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, DelegateTreeSubNode delegateTreeSubNode)
			{
				super(0, persistenceManager, transaction, delegateTreeSubNode);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			protected DelegateTreeSubNode getDelegateTreeNode()
			{
				return (DelegateTreeSubNode) super.getDelegateTreeNode();
			}

			@Override
			protected DelegateTreeSubNodeInfo recv(Map<UUID, DelegateAuthorizer> delegateAuthorizers, Map<String, DelegateTreeSubNodeInfo> subNodes,
					DataInput in) throws IOException, ProtocolException
			{
				return new DelegateTreeSubNodeInfo(getDelegateTreeNode(), delegateAuthorizers, subNodes);
			}

		}

	}

	public static class Entry extends AbstractUUIDPersistentInfoMessage.Entry<DelegateTreeRootNodeInfo>
	{
		public Entry(UUID uuid, DelegateTreeRootNodeInfo value)
		{
			super(uuid, value);
		}
	}

	public DelegateAuthorizerResponseMessage(Collection<? extends AbstractUUIDPersistentInfoMessage.Entry<DelegateTreeRootNodeInfo>> entries)
	{
		super(entries);
	}

	private static Collection<Entry> makeEntries(PersistenceManager persistenceManager, Transaction transaction,
			DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage)
			{
		Collection<Entry> entries = new ArrayList<Entry>();
		for (AbstractUUIDInfoMessage.Entry<DelegateAuthorizerRequestMessage.DelegateTreeRootNodeInfo> e : delegateAuthorizerRequestMessage.getEntries())
		{
			DelegateTreeRootNode delegateTreeRootNode = persistenceManager.getDelegateTreeRootNode(transaction, e.getKey());
			if (delegateTreeRootNode != null)
				entries.add(new Entry(e.getKey(), new DelegateTreeRootNodeInfo(persistenceManager, transaction, delegateTreeRootNode, e.getValue())));
		}
		return entries;
			}

	public DelegateAuthorizerResponseMessage(PersistenceManager persistenceManager, Transaction transaction,
			DelegateAuthorizerRequestMessage delegateAuthorizerRequestMessage)
	{
		this(makeEntries(persistenceManager, transaction, delegateAuthorizerRequestMessage));
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDPersistentInfoMessage.SubProtocol<DelegateTreeRootNodeInfo, DelegateAuthorizerResponseMessage>
	{

		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, DelegateTreeRootNodeInfo v) throws IOException
		{
			DelegateTreeRootNode delegateTreeRootNode = getPersistenceManager().getDelegateTreeRootNode(getTransaction(), uuid);
			if (delegateTreeRootNode == null)
				throw new ProtocolSendException();
			DelegateTreeRootNodeInfo.MyProtocol delegateTreeRootNodeInfoProtocol = new DelegateTreeRootNodeInfo.MyProtocol(0, getPersistenceManager(),
					getTransaction(), delegateTreeRootNode);
			delegateTreeRootNodeInfoProtocol.send(out, v);
		}

		@Override
		protected DelegateTreeRootNodeInfo recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			DelegateTreeRootNode delegateTreeRootNode = getPersistenceManager().getDelegateTreeRootNode(getTransaction(), uuid);
			if (delegateTreeRootNode == null)
				throw new ProtocolException();
			DelegateTreeRootNodeInfo.MyProtocol delegateTreeRootNodeInfoProtocol = new DelegateTreeRootNodeInfo.MyProtocol(0, getPersistenceManager(),
					getTransaction(), delegateTreeRootNode);
			return delegateTreeRootNodeInfoProtocol.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			DelegateTreeRootNodeInfo.MyProtocol delegateTreeRootNodeInfoProtocol = new DelegateTreeRootNodeInfo.MyProtocol(0, getPersistenceManager(),
					getTransaction(), null);
			delegateTreeRootNodeInfoProtocol.skip(in);
		}

		@Override
		public DelegateAuthorizerResponseMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new DelegateAuthorizerResponseMessage(recvEntries(in));
		}

	}

}
