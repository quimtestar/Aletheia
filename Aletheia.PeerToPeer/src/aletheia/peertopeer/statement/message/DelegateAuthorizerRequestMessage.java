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
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.Person;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.collection.MapProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = DelegateAuthorizerRequestMessage.SubProtocol.class)
public class DelegateAuthorizerRequestMessage extends AbstractUUIDInfoMessage<DelegateAuthorizerRequestMessage.DelegateTreeRootNodeInfo>
{
	static abstract class DelegateTreeNodeInfo implements Exportable
	{
		private final Collection<UUID> delegateUuids;
		private final Map<String, DelegateTreeSubNodeInfo> subNodes;

		private DelegateTreeNodeInfo(Collection<UUID> delegateUuids, Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super();
			this.delegateUuids = delegateUuids;
			this.subNodes = subNodes;
		}

		private DelegateTreeNodeInfo(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode,
				DelegateTreeInfoMessage.DelegateTreeNodeInfo delegateTreeInfoMessageNode)
		{
			super();
			this.delegateUuids = new HashSet<>();
			this.subNodes = new HashMap<>();
			for (Map.Entry<UUID, DelegateTreeInfoMessage.DelegateAuthorizerInfo> e : delegateTreeInfoMessageNode.getDelegateAuthorizers().entrySet())
			{
				if (e.getValue() instanceof DelegateTreeInfoMessage.SignedDelegateAuthorizerInfo)
				{
					DelegateTreeInfoMessage.SignedDelegateAuthorizerInfo signedDelegateAuthorizerInfo = (DelegateTreeInfoMessage.SignedDelegateAuthorizerInfo) e
							.getValue();
					Person delegate = persistenceManager.getPerson(transaction, e.getKey());
					if (delegate != null)
					{
						DelegateAuthorizer delegateAuthorizer = delegateTreeNode.getDelegateAuthorizer(transaction, delegate);
						if (delegateAuthorizer != null)
						{
							if (signedDelegateAuthorizerInfo.updatable(delegateAuthorizer))
								delegateUuids.add(e.getKey());
						}
					}

				}
			}
			for (Map.Entry<String, DelegateTreeInfoMessage.DelegateTreeSubNodeInfo> e : delegateTreeInfoMessageNode.getSubNodes().entrySet())
			{
				try
				{
					DelegateTreeSubNode delegateTreeSubNode = delegateTreeNode.getSubNode(transaction, e.getKey());
					if (delegateTreeSubNode != null)
						subNodes.put(e.getKey(), new DelegateTreeSubNodeInfo(persistenceManager, transaction, delegateTreeSubNode, e.getValue()));
				}
				catch (InvalidNameException e1)
				{
					throw new RuntimeException(e1);
				}
			}
		}

		protected Collection<UUID> getDelegateUuids()
		{
			return delegateUuids;
		}

		protected Map<String, DelegateTreeSubNodeInfo> getSubNodes()
		{
			return subNodes;
		}

		@ProtocolInfo(availableVersions = 0)
		private abstract static class MyProtocol<I extends DelegateTreeNodeInfo> extends ExportableProtocol<I>
		{
			private final static UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			private final static CollectionProtocol<UUID> uuidCollectionProtocol = new CollectionProtocol<>(0, uuidProtocol);
			private final static StringProtocol stringProtocol = new StringProtocol(0);
			private final static DelegateTreeSubNodeInfo.MyProtocol delegateTreeSubNodeInfoProtocol = new DelegateTreeSubNodeInfo.MyProtocol(0);
			private final static MapProtocol<String, DelegateTreeSubNodeInfo> subNodesMapProtocol = new MapProtocol<>(0, stringProtocol,
					delegateTreeSubNodeInfoProtocol);

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, I delegateTreeNodeInfo) throws IOException
			{
				uuidCollectionProtocol.send(out, delegateTreeNodeInfo.getDelegateUuids());
				subNodesMapProtocol.send(out, delegateTreeNodeInfo.getSubNodes());
			}

			protected abstract I recv(Collection<UUID> delegateUuids, Map<String, DelegateTreeSubNodeInfo> subNodes, DataInput in)
					throws IOException, ProtocolException;

			@Override
			public I recv(DataInput in) throws IOException, ProtocolException
			{
				Collection<UUID> delegateUuids = uuidCollectionProtocol.recv(in);
				Map<String, DelegateTreeSubNodeInfo> subNodes = subNodesMapProtocol.recv(in);
				return recv(delegateUuids, subNodes, in);
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				uuidCollectionProtocol.skip(in);
				subNodesMapProtocol.skip(in);
			}

		}

	}

	public static class DelegateTreeRootNodeInfo extends DelegateTreeNodeInfo
	{

		private DelegateTreeRootNodeInfo(Collection<UUID> delegateUuids, Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super(delegateUuids, subNodes);
		}

		public DelegateTreeRootNodeInfo(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeRootNode delegateTreeRootNode,
				DelegateTreeInfoMessage.DelegateTreeRootNodeInfo delegateTreeInfoMessageRootNode)
		{
			super(persistenceManager, transaction, delegateTreeRootNode, delegateTreeInfoMessageRootNode);
		}

		@ProtocolInfo(availableVersions = 0)
		private static class MyProtocol extends DelegateTreeNodeInfo.MyProtocol<DelegateTreeRootNodeInfo>
		{

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			protected DelegateTreeRootNodeInfo recv(Collection<UUID> delegateUuids, Map<String, DelegateTreeSubNodeInfo> subNodes, DataInput in)
					throws IOException, ProtocolException
			{
				return new DelegateTreeRootNodeInfo(delegateUuids, subNodes);
			}

		}

	}

	static class DelegateTreeSubNodeInfo extends DelegateTreeNodeInfo
	{
		private DelegateTreeSubNodeInfo(Collection<UUID> delegateUuids, Map<String, DelegateTreeSubNodeInfo> subNodes)
		{
			super(delegateUuids, subNodes);
		}

		private DelegateTreeSubNodeInfo(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeSubNode delegateTreeSubNode,
				DelegateTreeInfoMessage.DelegateTreeSubNodeInfo delegateTreeInfoMessageSubNode)
		{
			super(persistenceManager, transaction, delegateTreeSubNode, delegateTreeInfoMessageSubNode);
		}

		@ProtocolInfo(availableVersions = 0)
		private static class MyProtocol extends DelegateTreeNodeInfo.MyProtocol<DelegateTreeSubNodeInfo>
		{

			protected MyProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(MyProtocol.class, requiredVersion);
			}

			@Override
			protected DelegateTreeSubNodeInfo recv(Collection<UUID> delegateUuids, Map<String, DelegateTreeSubNodeInfo> subNodes, DataInput in)
					throws IOException, ProtocolException
			{
				return new DelegateTreeSubNodeInfo(delegateUuids, subNodes);
			}

		}

	}

	public static class Entry extends AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo>
	{
		public Entry(UUID uuid, DelegateTreeRootNodeInfo value)
		{
			super(uuid, value);
		}
	}

	public DelegateAuthorizerRequestMessage(Collection<? extends AbstractUUIDInfoMessage.Entry<DelegateTreeRootNodeInfo>> entries)
	{
		super(entries);
	}

	private static Collection<Entry> makeEntries(PersistenceManager persistenceManager, Transaction transaction,
			DelegateTreeInfoMessage delegateTreeInfoMessage)
	{
		Collection<Entry> entries = new ArrayList<>();
		for (AbstractUUIDInfoMessage.Entry<DelegateTreeInfoMessage.DelegateTreeRootNodeInfo> e : delegateTreeInfoMessage.getEntries())
		{
			DelegateTreeRootNode delegateTreeRootNode = persistenceManager.getDelegateTreeRootNode(transaction, e.getKey());
			if (delegateTreeRootNode != null)
				entries.add(new Entry(e.getKey(), new DelegateTreeRootNodeInfo(persistenceManager, transaction, delegateTreeRootNode, e.getValue())));
		}
		return entries;
	}

	public DelegateAuthorizerRequestMessage(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeInfoMessage delegateTreeInfoMessage)
	{
		this(makeEntries(persistenceManager, transaction, delegateTreeInfoMessage));
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDInfoMessage.SubProtocol<DelegateTreeRootNodeInfo, DelegateAuthorizerRequestMessage>
	{
		private final DelegateTreeRootNodeInfo.MyProtocol delegateTreeRootNodeInfoProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.delegateTreeRootNodeInfoProtocol = new DelegateTreeRootNodeInfo.MyProtocol(0);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, DelegateTreeRootNodeInfo v) throws IOException
		{
			delegateTreeRootNodeInfoProtocol.send(out, v);
		}

		@Override
		protected DelegateTreeRootNodeInfo recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			return delegateTreeRootNodeInfoProtocol.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			delegateTreeRootNodeInfoProtocol.skip(in);
		}

		@Override
		public DelegateAuthorizerRequestMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new DelegateAuthorizerRequestMessage(recvEntries(in));
		}

	}

}
