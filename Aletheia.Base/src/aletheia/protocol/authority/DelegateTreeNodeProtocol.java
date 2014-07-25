/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.protocol.authority;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureVersionException;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

@ProtocolInfo(availableVersions = 0)
public abstract class DelegateTreeNodeProtocol<D extends DelegateTreeNode> extends PersistentExportableProtocol<D>
{

	private final UUIDProtocol uuidProtocol;
	private final CollectionProtocol<UUID> uuidCollectionProtocol;
	private final StringProtocol stringProtocol;

	@ProtocolInfo(availableVersions = 0)
	private class DelegateTreeSubNodeEntryProtocol extends Protocol<Entry<Namespace, DelegateTreeSubNode>>
	{
		private final DelegateTreeNode parent;

		private DelegateTreeSubNodeEntryProtocol(int requiredVersion, DelegateTreeNode delegateTreeNode)
		{
			super(0);
			checkVersionAvailability(DelegateTreeSubNodeEntryProtocol.class, requiredVersion);
			this.parent = delegateTreeNode;
		}

		@Override
		public void send(DataOutput out, Entry<Namespace, DelegateTreeSubNode> e) throws IOException
		{
			String name = ((NodeNamespace) e.getKey()).getName();
			stringProtocol.send(out, name);
			DelegateTreeSubNodeProtocol delegateTreeSubNodeProtocol = new DelegateTreeSubNodeProtocol(0, getPersistenceManager(), getTransaction(), parent,
					name);
			delegateTreeSubNodeProtocol.send(out, e.getValue());
		}

		private class MyEntry implements Entry<Namespace, DelegateTreeSubNode>
		{
			private final Namespace namespace;
			private final DelegateTreeSubNode delegateTreeSubNode;

			private MyEntry(Namespace namespace, DelegateTreeSubNode delegateTreeSubNode)
			{
				this.namespace = namespace;
				this.delegateTreeSubNode = delegateTreeSubNode;
			}

			@Override
			public Namespace getKey()
			{
				return namespace;
			}

			@Override
			public DelegateTreeSubNode getValue()
			{
				return delegateTreeSubNode;
			}

			@Override
			public DelegateTreeSubNode setValue(DelegateTreeSubNode value)
			{
				throw new UnsupportedOperationException();
			}

		}

		@Override
		public Entry<Namespace, DelegateTreeSubNode> recv(DataInput in) throws IOException, ProtocolException
		{
			String name = stringProtocol.recv(in);
			DelegateTreeSubNodeProtocol delegateTreeSubNodeProtocol = new DelegateTreeSubNodeProtocol(0, getPersistenceManager(), getTransaction(), parent,
					name);
			DelegateTreeSubNode delegateTreeSubNode = delegateTreeSubNodeProtocol.recv(in);
			return new MyEntry(delegateTreeSubNode.getPrefix(), delegateTreeSubNode);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			stringProtocol.skip(in);
			DelegateTreeSubNodeProtocol delegateTreeSubNodeProtocol = new DelegateTreeSubNodeProtocol(0, getPersistenceManager(), getTransaction());
			delegateTreeSubNodeProtocol.skip(in);
		}

	}

	@ProtocolInfo(availableVersions = 0)
	private class DelegateTreeSubNodeEntryCollectionProtocol extends CollectionProtocol<Entry<Namespace, DelegateTreeSubNode>>
	{

		public DelegateTreeSubNodeEntryCollectionProtocol(int requiredVersion, DelegateTreeNode parent)
		{
			super(0, new DelegateTreeSubNodeEntryProtocol(0, parent));
			checkVersionAvailability(DelegateTreeSubNodeEntryCollectionProtocol.class, requiredVersion);
		}

	}

	public DelegateTreeNodeProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(DelegateTreeNodeProtocol.class, requiredVersion);
		this.uuidProtocol = new UUIDProtocol(0);
		this.uuidCollectionProtocol = new CollectionProtocol<UUID>(0, this.uuidProtocol);
		this.stringProtocol = new StringProtocol(0);

	}

	@Override
	public void send(DataOutput out, D delegateTreeNode) throws IOException
	{
		Collection<UUID> delegateUuids = new BijectionCollection<Person, UUID>(new Bijection<Person, UUID>()
				{

			@Override
			public UUID forward(Person person)
			{
				return person.getUuid();
			}

			@Override
			public Person backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
				}, delegateTreeNode.localDelegateAuthorizerMap(getTransaction()).keySet());
		uuidCollectionProtocol.send(out, delegateUuids);
		DelegateTreeSubNodeEntryCollectionProtocol delegateTreeSubNodeEntryCollectionProtocol = new DelegateTreeSubNodeEntryCollectionProtocol(0,
				delegateTreeNode);
		delegateTreeSubNodeEntryCollectionProtocol.send(out, delegateTreeNode.localDelegateTreeSubNodeMap(getTransaction()).entrySet());
	}

	protected abstract D obtainDelegateTreeNode() throws ProtocolException;

	@Override
	public D recv(DataInput in) throws IOException, ProtocolException
	{
		D delegateTreeNode = obtainDelegateTreeNode();
		Collection<UUID> delegateUuids = uuidCollectionProtocol.recv(in);
		for (UUID delegateUuid : delegateUuids)
		{
			Person delegate = getPersistenceManager().getPerson(getTransaction(), delegateUuid);
			if (delegate == null)
				throw new ProtocolException();
			delegateTreeNode.getOrCreateDelegateAuthorizerNoSign(getTransaction(), delegate);
		}
		Set<UUID> delegateUuidSet = new HashSet<UUID>(delegateUuids);
		for (DelegateAuthorizer da : delegateTreeNode.localDelegateAuthorizerMap(getTransaction()).values())
			if (!delegateUuidSet.contains(da.getDelegateUuid()))
				delegateTreeNode.deleteDelegateAuthorizerNoSign(getTransaction(), da);
		DelegateTreeSubNodeEntryCollectionProtocol delegateTreeSubNodeEntryCollectionProtocol = new DelegateTreeSubNodeEntryCollectionProtocol(0,
				delegateTreeNode);
		Collection<Entry<Namespace, DelegateTreeSubNode>> subNodeEntryCollection = delegateTreeSubNodeEntryCollectionProtocol.recv(in);
		Set<String> names = new HashSet<String>();
		for (Entry<Namespace, DelegateTreeSubNode> e : subNodeEntryCollection)
		{
			if (!(e.getKey() instanceof NodeNamespace))
				throw new ProtocolException();
			names.add(((NodeNamespace) e.getKey()).getName());
		}
		for (Namespace ns : delegateTreeNode.localDelegateTreeSubNodeMap(getTransaction()).keySet())
		{
			if (!(ns instanceof NodeNamespace))
				throw new ProtocolException();
			String name = ((NodeNamespace) ns).getName();
			if (!names.contains(name))
				try
			{
					delegateTreeNode.deleteSubNodeNoSign(getTransaction(), name);
			}
			catch (InvalidNameException e)
			{
				throw new ProtocolException(e);
			}
		}
		try
		{
			delegateTreeNode.update(getTransaction());
		}
		catch (SignatureVersionException e)
		{
			throw new ProtocolException(e);
		}
		return delegateTreeNode;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidCollectionProtocol.skip(in);
		DelegateTreeSubNodeEntryCollectionProtocol delegateTreeSubNodeEntryCollectionProtocol = new DelegateTreeSubNodeEntryCollectionProtocol(0, null);
		delegateTreeSubNodeEntryCollectionProtocol.skip(in);
	}

}
