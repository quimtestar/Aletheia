/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.namespace.NamespaceProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.utilities.collections.BufferedList;

@ProtocolInfo(availableVersions = 0)
public class DelegateTreeRootNodeWithAuthorizersProtocol extends DelegateTreeRootNodeProtocol
{
	private final IntegerProtocol integerProtocol;
	private final NamespaceProtocol namespaceProtocol;

	public DelegateTreeRootNodeWithAuthorizersProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction,
			StatementAuthority statementAuthority)
	{
		super(0, persistenceManager, transaction, statementAuthority);
		checkVersionAvailability(DelegateTreeRootNodeWithAuthorizersProtocol.class, requiredVersion);
		this.integerProtocol = new IntegerProtocol(0);
		this.namespaceProtocol = new NamespaceProtocol(0);
	}

	@Override
	public void send(DataOutput out, DelegateTreeRootNode delegateTreeRootNode) throws IOException
	{
		super.send(out, delegateTreeRootNode);
		Collection<DelegateTreeNode> delegateTreeNodes = new BufferedList<>(delegateTreeRootNode.delegateTreeNodesRecursive(getTransaction()));
		integerProtocol.send(out, delegateTreeNodes.size());
		for (DelegateTreeNode delegateTreeNode : delegateTreeNodes)
		{
			namespaceProtocol.send(out, delegateTreeNode.getPrefix());
			DelegateAuthorizerProtocol delegateAuthorizerProtocol = new DelegateAuthorizerProtocol(0, getPersistenceManager(), getTransaction(),
					delegateTreeNode);
			CollectionProtocol<DelegateAuthorizer> delegateAuthorizerCollectionProtocol = new CollectionProtocol<>(0, delegateAuthorizerProtocol);
			delegateAuthorizerCollectionProtocol.send(out, delegateTreeNode.localDelegateAuthorizerMap(getTransaction()).values());
		}
	}

	@Override
	public DelegateTreeRootNode recv(DataInput in) throws IOException, ProtocolException
	{
		DelegateTreeRootNode delegateTreeRootNode = super.recv(in);
		int n = integerProtocol.recv(in);
		for (int i = 0; i < n; i++)
		{
			Namespace prefix = namespaceProtocol.recv(in);
			DelegateTreeNode delegateTreeNode = delegateTreeRootNode.getSubNode(getTransaction(), prefix);
			if (delegateTreeNode == null)
				throw new ProtocolException();
			DelegateAuthorizerProtocol delegateAuthorizerProtocol = new DelegateAuthorizerProtocol(0, getPersistenceManager(), getTransaction(),
					delegateTreeNode);
			CollectionProtocol<DelegateAuthorizer> delegateAuthorizerCollectionProtocol = new CollectionProtocol<>(0, delegateAuthorizerProtocol);
			delegateAuthorizerCollectionProtocol.recv(in);
		}
		return delegateTreeRootNode;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		super.skip(in);
		int n = integerProtocol.recv(in);
		for (int i = 0; i < n; i++)
		{
			namespaceProtocol.skip(in);
			DelegateAuthorizerProtocol delegateAuthorizerProtocol = new DelegateAuthorizerProtocol(0, getPersistenceManager(), getTransaction(), null);
			CollectionProtocol<DelegateAuthorizer> delegateAuthorizerCollectionProtocol = new CollectionProtocol<>(0, delegateAuthorizerProtocol);
			delegateAuthorizerCollectionProtocol.skip(in);
		}
	}

}
