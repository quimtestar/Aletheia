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
package aletheia.model.authority;

import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.NoPrivateDataForAuthorException;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.DelegateTreeSubNodeEntity;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CombinedCloseableMap;

public class DelegateTreeSubNode extends DelegateTreeNode
{
	public DelegateTreeSubNode(PersistenceManager persistenceManager, DelegateTreeSubNodeEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public DelegateTreeSubNodeEntity getEntity()
	{
		return (DelegateTreeSubNodeEntity) super.getEntity();
	}

	private DelegateTreeSubNode(PersistenceManager persistenceManager, DelegateTreeNode parent, String name) throws InvalidNameException
	{
		super(persistenceManager, DelegateTreeSubNodeEntity.class, parent.getStatementUuid());
		getEntity().setPrefix(new NodeNamespace(parent.getPrefix(), name));
	}

	public static DelegateTreeSubNode create(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode parent, String name)
			throws InvalidNameException, NoPrivateDataForAuthorException, DateConsistenceException
	{
		DelegateTreeSubNode delegateTreeSubNode = new DelegateTreeSubNode(persistenceManager, parent, name);
		delegateTreeSubNode.persistenceUpdateSign(transaction);
		return delegateTreeSubNode;
	}

	public static DelegateTreeSubNode createNoSign(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode parent, String name)
			throws InvalidNameException
	{
		DelegateTreeSubNode delegateTreeSubNode = new DelegateTreeSubNode(persistenceManager, parent, name);
		delegateTreeSubNode.persistenceUpdate(transaction);
		return delegateTreeSubNode;
	}

	@Override
	public NodeNamespace getPrefix()
	{
		return (NodeNamespace) super.getPrefix();
	}

	public DelegateTreeNode getParent(Transaction transaction)
	{
		return getPersistenceManager().getDelegateTreeNode(transaction, getStatementUuid(), getPrefix().getParent());
	}

	@Override
	public CloseableMap<Person, DelegateAuthorizer> delegateAuthorizerMap(Transaction transaction)
	{
		return new CombinedCloseableMap<Person, DelegateAuthorizer>(localDelegateAuthorizerMap(transaction),
				getParent(transaction).delegateAuthorizerMap(transaction));
	}

	@Override
	public void persistenceUpdateSign(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		changeSignatureVersion(transaction);
		try
		{
			updateMessageDigestData(transaction);
		}
		catch (SignatureVersionException e)
		{
			throw new Error("signingSignatureVersion must be supported", e);
		}
		persistenceUpdate(transaction);
		getParent(transaction).persistenceUpdateSign(transaction);
	}

	@Override
	public void delete(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		getParent(transaction).deleteSubNode(transaction, getPrefix());
	}

}
