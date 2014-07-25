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

import java.io.DataOutput;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.NoPrivateDataForAuthorException;
import aletheia.model.authority.StatementAuthority.StateListener;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.security.MessageDigestData;
import aletheia.model.security.SignatureData;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.LocalDelegateAuthorizerMap;
import aletheia.persistence.collections.authority.LocalDelegateTreeSubNodeMap;
import aletheia.persistence.entities.authority.DelegateTreeNodeEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.collection.ListProtocol;
import aletheia.protocol.namespace.NamespaceProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.security.MessageDigestDataProtocol;
import aletheia.security.messagedigester.BufferedMessageDigester;
import aletheia.security.messagedigester.MessageDigester;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableMap;

public abstract class DelegateTreeNode implements Exportable
{
	private final static String digestAlgorithm = "SHA-1";

	private final PersistenceManager persistenceManager;
	private final DelegateTreeNodeEntity entity;

	public DelegateTreeNode(PersistenceManager persistenceManager, DelegateTreeNodeEntity entity)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public DelegateTreeNodeEntity getEntity()
	{
		return entity;
	}

	protected DelegateTreeNode(PersistenceManager persistenceManager, Class<? extends DelegateTreeNodeEntity> entityClass, UUID statementUuid)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateDelegateTreeNodeEntity(entityClass);
		this.entity.setStatementUuid(statementUuid);
	}

	public StatementAuthority getStatementAuthority(Transaction transaction)
	{
		return persistenceManager.getStatementAuthority(transaction, getStatementUuid());
	}

	public Statement getStatement(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getStatementUuid());
	}

	public Person getAuthor(Transaction transaction)
	{
		return getStatementAuthority(transaction).getAuthor(transaction);
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putDelegateTreeNode(transaction, this);
	}

	public abstract void persistenceUpdateSign(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException;

	public UUID getStatementUuid()
	{
		return getEntity().getStatementUuid();
	}

	public Namespace getPrefix()
	{
		return getEntity().getPrefix();
	}

	public MessageDigestData getMessageDigestData()
	{
		return getEntity().getMessageDigestData();
	}

	protected void setMessageDigestData(MessageDigestData messageDigestData)
	{
		getEntity().setMessageDigestData(messageDigestData);
	}

	protected void updateMessageDigestData(Transaction transaction) throws SignatureVersionException
	{
		setMessageDigestData(computeMessageDigestData(transaction));
	}

	protected void messageDigestDataOut(Transaction transaction, DataOutput out) throws SignatureVersionException
	{
		if (getSignatureVersion(transaction) != 0)
			throw new SignatureVersionException();
		try
		{
			List<UUID> uuidList = new ArrayList<UUID>();
			for (Person person : localDelegateAuthorizerMap(transaction).keySet())
				uuidList.add(person.getUuid());
			Collections.sort(uuidList);
			ListProtocol<UUID> uuidListProtocol = new ListProtocol<>(0, new UUIDProtocol(0));
			uuidListProtocol.send(out, uuidList);
			NamespaceProtocol namespaceProtocol = new NamespaceProtocol(0);
			MessageDigestDataProtocol messageDigestDataProtocol = new MessageDigestDataProtocol(0);
			for (Map.Entry<Namespace, DelegateTreeSubNode> e : localDelegateTreeSubNodeMap(transaction).entrySet())
			{
				namespaceProtocol.send(out, e.getKey());
				messageDigestDataProtocol.send(out, e.getValue().getMessageDigestData());
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}
	}

	protected MessageDigestData computeMessageDigestData(Transaction transaction) throws SignatureVersionException
	{
		if (getSignatureVersion(transaction) < 0)
			return null;
		try
		{
			MessageDigester digester = new BufferedMessageDigester(digestAlgorithm);
			DataOutput out = digester.dataOutput();
			messageDigestDataOut(transaction, out);
			return digester.digest();
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	public DelegateTreeSubNode getSubNode(Transaction transaction, String name) throws InvalidNameException
	{
		return (DelegateTreeSubNode) persistenceManager.getDelegateTreeNode(transaction, getStatementUuid(), new NodeNamespace(getPrefix(), name));
	}

	public DelegateTreeNode getSubNode(Transaction transaction, Namespace namespace)
	{
		return persistenceManager.getDelegateTreeNode(transaction, getStatementUuid(), getPrefix().concat(namespace));
	}

	protected DelegateTreeSubNode createSubNode(Transaction transaction, String name) throws InvalidNameException, NoPrivateDataForAuthorException,
			DateConsistenceException
	{
		DelegateTreeSubNode delegateTreeSubNode = DelegateTreeSubNode.create(persistenceManager, transaction, this, name);
		persistenceUpdateSign(transaction);
		return delegateTreeSubNode;
	}

	protected DelegateTreeSubNode createSubNodeNoSign(Transaction transaction, String name) throws InvalidNameException
	{
		DelegateTreeSubNode delegateTreeSubNode = DelegateTreeSubNode.createNoSign(persistenceManager, transaction, this, name);
		return delegateTreeSubNode;
	}

	public DelegateTreeSubNode getOrCreateSubNode(Transaction transaction, String name) throws InvalidNameException, NoPrivateDataForAuthorException,
			DateConsistenceException
	{
		DelegateTreeSubNode subNode = getSubNode(transaction, name);
		if (subNode == null)
			subNode = createSubNode(transaction, name);
		return subNode;
	}

	public DelegateTreeSubNode getOrCreateSubNodeNoSign(Transaction transaction, String name) throws InvalidNameException
	{
		DelegateTreeSubNode subNode = getSubNode(transaction, name);
		if (subNode == null)
			subNode = createSubNodeNoSign(transaction, name);
		return subNode;
	}

	public void deleteSubNode(Transaction transaction, Namespace prefix) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		deleteSubNodeNoSign(transaction, prefix);
		persistenceUpdateSign(transaction);
	}

	public void deleteSubNodeNoSign(Transaction transaction, Namespace prefix)
	{
		DelegateTreeSubNode subNode = localDelegateTreeSubNodeMap(transaction).get(prefix);
		if (subNode != null)
			subNode.deleteDelegateSubTree(transaction);
	}

	public void deleteSubNodeNoSign(Transaction transaction, String name) throws InvalidNameException
	{
		deleteSubNodeNoSign(transaction, new NodeNamespace(getPrefix(), name));
	}

	protected void deleteNoSign(Transaction transaction)
	{
		for (DelegateAuthorizer delegateAuthorizer : localDelegateAuthorizerMap(transaction).values())
			deleteDelegateAuthorizerNoSign(transaction, delegateAuthorizer);
		persistenceManager.deleteDelegateTreeNode(transaction, this);
	}

	protected void deleteDelegateSubTree(Transaction transaction)
	{
		Stack<DelegateTreeNode> stack = new Stack<DelegateTreeNode>();
		stack.push(this);
		Stack<DelegateTreeNode> stack2 = new Stack<DelegateTreeNode>();
		while (!stack.isEmpty())
		{
			DelegateTreeNode node = stack.pop();
			CloseableCollection<DelegateTreeSubNode> subnodes = node.localDelegateTreeSubNodeMap(transaction).values();
			stack.addAll(subnodes);
			if (subnodes.isEmpty())
			{
				node.deleteNoSign(transaction);
				while (!stack2.isEmpty())
				{
					DelegateTreeNode node_ = stack2.peek();
					if (!node_.localDelegateTreeSubNodeMap(transaction).isEmpty())
						break;
					node_.deleteNoSign(transaction);
					stack2.pop();
				}
			}
			else
				stack2.push(node);
		}
	}

	public LocalDelegateTreeSubNodeMap localDelegateTreeSubNodeMap(Transaction transaction)
	{
		return persistenceManager.localDelegateTreeSubNodeMap(transaction, this);
	}

	public DelegateAuthorizer getDelegateAuthorizer(Transaction transaction, Person delegate)
	{
		return getDelegateAuthorizer(transaction, delegate.getUuid());
	}

	protected DelegateAuthorizer getDelegateAuthorizer(Transaction transaction, UUID delegateUuid)
	{
		return persistenceManager.getDelegateAuthorizer(transaction, getStatementUuid(), getPrefix(), delegateUuid);
	}

	private DelegateAuthorizer createDelegateAuthorizer(Transaction transaction, Person delegate) throws NoPrivateDataForAuthorException,
			DateConsistenceException
	{
		DelegateAuthorizer delegateAuthorizer = DelegateAuthorizer.create(persistenceManager, transaction, this, delegate);
		persistenceUpdateSign(transaction);
		return delegateAuthorizer;
	}

	private DelegateAuthorizer createDelegateAuthorizerNoSign(Transaction transaction, Person delegate)
	{
		DelegateAuthorizer delegateAuthorizer = DelegateAuthorizer.create(persistenceManager, transaction, this, delegate);
		return delegateAuthorizer;
	}

	public DelegateAuthorizer getOrCreateDelegateAuthorizer(Transaction transaction, Person delegate) throws NoPrivateDataForAuthorException,
			DateConsistenceException
	{
		DelegateAuthorizer delegateAuthorizer = getDelegateAuthorizer(transaction, delegate);
		if (delegateAuthorizer == null)
			delegateAuthorizer = createDelegateAuthorizer(transaction, delegate);
		return delegateAuthorizer;
	}

	public DelegateAuthorizer updateDelegateAuthorizer(Transaction transaction, Person delegate, Signatory authorizer, Collection<UUID> revokedSignatureUuids,
			Date signatureDate, int signatureVersion, SignatureData signatureData) throws SignatureVerifyException, SignatureVersionException
	{
		DelegateAuthorizer delegateAuthorizer = getOrCreateDelegateAuthorizerNoSign(transaction, delegate);
		delegateAuthorizer.update(transaction, authorizer, revokedSignatureUuids, signatureDate, signatureVersion, signatureData);
		return delegateAuthorizer;
	}

	public DelegateAuthorizer getOrCreateDelegateAuthorizerNoSign(Transaction transaction, Person delegate)
	{
		DelegateAuthorizer delegateAuthorizer = getDelegateAuthorizer(transaction, delegate);
		if (delegateAuthorizer == null)
			delegateAuthorizer = createDelegateAuthorizerNoSign(transaction, delegate);
		return delegateAuthorizer;
	}

	public void deleteDelegateAuthorizer(Transaction transaction, Person delegate) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		deleteDelegateAuthorizer(transaction, getDelegateAuthorizer(transaction, delegate));
	}

	public void deleteDelegateAuthorizer(Transaction transaction, DelegateAuthorizer delegateAuthorizer) throws NoPrivateDataForAuthorException,
			DateConsistenceException
	{
		deleteDelegateAuthorizerNoSign(transaction, delegateAuthorizer);
		persistenceUpdateSign(transaction);
	}

	public void deleteDelegateAuthorizerNoSign(Transaction transaction, DelegateAuthorizer delegateAuthorizer)
	{
		delegateAuthorizer.clearAuthorizer();
		delegateAuthorizer.persistenceUpdate(transaction);
		persistenceManager.deleteDelegateAuthorizer(transaction, delegateAuthorizer);
		Person delegate = delegateAuthorizer.getDelegate(transaction);
		if (delegate != null)
			delegate.checkOrphanity(transaction);
	}

	public LocalDelegateAuthorizerMap localDelegateAuthorizerMap(Transaction transaction)
	{
		return persistenceManager.localDelegateAuthorizerMap(transaction, this);
	}

	public abstract CloseableMap<Person, DelegateAuthorizer> delegateAuthorizerMap(Transaction transaction);

	public void clearLocalDelegateTreeSubNodes(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		for (Namespace prefix : localDelegateTreeSubNodeMap(transaction).keySet())
			deleteSubNode(transaction, prefix);
	}

	public void clearLocalDelegateAuthorizers(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		for (DelegateAuthorizer delegateAuthorizer : localDelegateAuthorizerMap(transaction).values())
			deleteDelegateAuthorizer(transaction, delegateAuthorizer);
	}

	public DelegateTreeRootNode getDelegateTreeRootNode(Transaction transaction)
	{
		return (DelegateTreeRootNode) persistenceManager.getDelegateTreeNode(transaction, getStatementUuid(), RootNamespace.instance);
	}

	protected int getSignatureVersion(Transaction transaction)
	{
		return getDelegateTreeRootNode(transaction).getSignatureVersion();
	}

	protected void changeSignatureVersion(Transaction transaction)
	{
		getDelegateTreeRootNode(transaction).changeSignatureVersion(transaction);
	}

	protected Iterable<StateListener> stateListeners()
	{
		return getPersistenceManager().getListenerManager().getStatementAuthorityStateListeners().iterable(getStatementUuid());
	}

	public abstract void delete(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException;

	public void update(Transaction transaction) throws SignatureVersionException
	{
		updateMessageDigestData(transaction);
		persistenceUpdate(transaction);
	}

	@Override
	public String toString()
	{
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	public String toString(Transaction transaction)
	{
		Statement statement = getStatement(transaction);
		if (statement == null)
			return "*null*";
		return statement.getVariable().toString(statement.parentVariableToIdentifier(transaction)) + ": " + "[" + getPrefix().toString() + "]";
	}

}
