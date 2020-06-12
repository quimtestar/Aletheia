/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.UUID;

import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.NoPrivateDataForAuthorException;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.DelegateAuthorizerEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.collection.NonReturningCollectionProtocol;
import aletheia.protocol.namespace.NamespaceProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.model.SignatureData;
import aletheia.security.signerverifier.Signer;
import aletheia.security.signerverifier.Verifier;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CombinedCloseableSet;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCloseableSet;

public class DelegateAuthorizer implements Exportable
{
	private final static int signingSignatureVersion = 0;

	private final PersistenceManager persistenceManager;
	private final DelegateAuthorizerEntity entity;

	private UUID oldAuthorizerUuid;

	private boolean checkAuthoritySignatures;
	private UUID authoritySignaturesOldAuthorizerUuid;

	public DelegateAuthorizer(PersistenceManager persistenceManager, DelegateAuthorizerEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
		this.oldAuthorizerUuid = entity.getAuthorizerUuid();
		clearCheckAuthoritySignatures();
	}

	protected DelegateAuthorizer(PersistenceManager persistenceManager, DelegateTreeNode delegateTreeNode, Person delegate)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateDelegateAuthorizerEntity(DelegateAuthorizerEntity.class);
		this.entity.setStatementUuid(delegateTreeNode.getStatementUuid());
		this.entity.setPrefix(delegateTreeNode.getPrefix());
		this.entity.setDelegateUuid(delegate.getUuid());
		this.entity.setSignatureVersion(-1);
		this.oldAuthorizerUuid = null;
		clearCheckAuthoritySignatures();
	}

	protected static DelegateAuthorizer create(PersistenceManager persistenceManager, Transaction transaction, DelegateTreeNode delegateTreeNode,
			Person delegate)
	{
		DelegateAuthorizer delegateAuthorizer = new DelegateAuthorizer(persistenceManager, delegateTreeNode, delegate);
		delegateAuthorizer.persistenceUpdate(transaction);
		delegate.checkOrphanity(transaction);
		return delegateAuthorizer;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public DelegateAuthorizerEntity getEntity()
	{
		return entity;
	}

	protected Iterable<StatementAuthority.StateListener> stateListeners()
	{
		return getPersistenceManager().getListenerManager().getStatementAuthorityStateListeners().iterable(getStatementUuid());
	}

	public void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putDelegateAuthorizer(transaction, this);
		if (isCheckAuthoritySignatures())
		{
			Iterable<StatementAuthority.StateListener> listeners = stateListeners();
			synchronized (listeners)
			{
				StatementAuthority statementAuthority = null;
				DelegateTreeNode node = null;
				Person delegate = null;
				Signatory authorizer = null;
				boolean first = true;
				for (StatementAuthority.StateListener l : listeners)
				{
					if (first)
					{
						statementAuthority = getStatementAuthority(transaction);
						node = getDelegateTreeNode(transaction);
						delegate = getDelegate(transaction);
						authorizer = getAuthorizer(transaction);
						first = false;
					}
					l.delegateAuthorizerChanged(transaction, statementAuthority, node.getPrefix(), delegate, authorizer);
				}
			}
			checkAuthoritySignaturesByAuthorizer(transaction, getAuthoritySignaturesOldAuthorizer(transaction), getAuthorizer(transaction));
			clearCheckAuthoritySignatures();
		}
		if (oldAuthorizerUuid != null && !oldAuthorizerUuid.equals(getAuthorizerUuid()))
		{
			Signatory oldAuthorizer = getPersistenceManager().getSignatory(transaction, oldAuthorizerUuid);
			if (oldAuthorizer != null)
				oldAuthorizer.deleteIfOrphan(transaction);
		}
	}

	public void checkAuthoritySignatures(Transaction transaction)
	{
		checkAuthoritySignaturesByAuthorizer(transaction, getAuthorizer(transaction));
	}

	private void checkAuthoritySignaturesByAuthorizer(Transaction transaction, Signatory... authorizers)
	{
		CloseableSet<StatementAuthoritySignature> set = null;
		for (Signatory authorizer : authorizers)
		{
			if (authorizer != null)
			{
				CloseableSet<StatementAuthoritySignature> set_ = statementAuthoritySignaturesByAuthorizer(transaction, authorizer);
				if (set == null)
					set = set_;
				else
					set = new CombinedCloseableSet<>(set_, set);
			}
		}
		if (set != null)
		{
			for (StatementAuthoritySignature sas : set)
				sas.checkValidSignature(transaction);
		}
	}

	private CloseableSet<StatementAuthoritySignature> statementAuthoritySignaturesByAuthorizer(Transaction transaction, Signatory authorizer)
	{
		return filterStatementAuthoritySignatures(transaction, authorizer.statementAuthoritySignatures(transaction));
	}

	private CloseableSet<StatementAuthoritySignature> filterStatementAuthoritySignatures(final Transaction transaction,
			CloseableSet<StatementAuthoritySignature> statementAuthoritySignatures)
	{
		Filter<StatementAuthoritySignature> filter = new Filter<>()
		{
			@Override
			public boolean filter(StatementAuthoritySignature sas)
			{
				if (getStatementUuid().equals(sas.getStatementUuid()))
					return false;
				Statement ctx_ = getStatement(transaction);
				if (!(ctx_ instanceof Context))
					return false;
				Context ctx = (Context) ctx_;
				Statement st = sas.getStatement(transaction);
				if (!getPrefix().isPrefixOf(st.identifier(transaction)))
					return false;
				if (!ctx.isDescendent(transaction, st))
					return false;
				return true;
			}

		};
		return new FilteredCloseableSet<>(filter, statementAuthoritySignatures);
	}

	public UUID getStatementUuid()
	{
		return entity.getStatementUuid();
	}

	public Namespace getPrefix()
	{
		return entity.getPrefix();
	}

	public UUID getDelegateUuid()
	{
		return entity.getDelegateUuid();
	}

	public UUID getAuthorizerUuid()
	{
		return entity.getAuthorizerUuid();
	}

	public Person getDelegate(Transaction transaction)
	{
		return persistenceManager.getPerson(transaction, entity.getDelegateUuid());
	}

	private void setAuthorizerUuid(UUID authorizerUuid)
	{
		UUID old = getAuthorizerUuid();
		if ((old == null) != (authorizerUuid == null) || (old != null) && !old.equals(authorizerUuid))
		{
			clearSignature();
			clearRevokedSignatureUuid();
			entity.setAuthorizerUuid(authorizerUuid);
		}
	}

	private void setAuthorizer(Signatory authorizer)
	{
		if (authorizer == null)
			setAuthorizerUuid(null);
		else
			setAuthorizerUuid(authorizer.getUuid());
	}

	public void clearAuthorizer()
	{
		setAuthorizer(null);
	}

	public PrivateSignatory createAuthorizer(Transaction transaction)
	{
		PrivateSignatory authorizer = PrivateSignatory.create(getPersistenceManager(), transaction);
		setAuthorizer(authorizer);
		return authorizer;
	}

	public Signatory getAuthorizer(Transaction transaction)
	{
		UUID uuid = entity.getAuthorizerUuid();
		if (uuid == null)
			return null;
		return persistenceManager.getSignatory(transaction, uuid);
	}

	private Signatory getAuthoritySignaturesOldAuthorizer(Transaction transaction)
	{
		if (authoritySignaturesOldAuthorizerUuid == null)
			return null;
		return persistenceManager.getSignatory(transaction, authoritySignaturesOldAuthorizerUuid);
	}

	private void setCheckAuthoritySignatures()
	{
		this.checkAuthoritySignatures = true;
	}

	private void clearCheckAuthoritySignatures()
	{
		this.checkAuthoritySignatures = false;
		this.authoritySignaturesOldAuthorizerUuid = getAuthorizerUuid();
	}

	private boolean isCheckAuthoritySignatures()
	{
		return this.checkAuthoritySignatures;
	}

	private SortedSet<UUID> getRevokedSignatureUuids()
	{
		return getEntity().getRevokedSignatureUuids();
	}

	public SortedSet<UUID> revokedSignatureUuids()
	{
		return Collections.unmodifiableSortedSet(getRevokedSignatureUuids());
	}

	public synchronized boolean addRevokedSignatureUuid(UUID revokedSignatureUuid)
	{
		if (getRevokedSignatureUuids().add(revokedSignatureUuid))
		{
			clearSignature();
			return true;
		}
		else
			return false;
	}

	public synchronized boolean removeRevokedSignatureUuid(UUID revokedSignatureUuid)
	{
		if (getRevokedSignatureUuids().remove(revokedSignatureUuid))
		{
			clearSignature();
			return true;
		}
		else
			return false;
	}

	public synchronized void clearRevokedSignatureUuid()
	{
		boolean wasEmpty = getRevokedSignatureUuids().isEmpty();
		getRevokedSignatureUuids().clear();
		if (!wasEmpty)
			clearSignature();
	}

	public synchronized boolean setRevokedSignatureUuids(Collection<UUID> revokedSignatureUuids)
	{
		boolean change = false;
		change = getRevokedSignatureUuids().retainAll(revokedSignatureUuids) || change;
		change = getRevokedSignatureUuids().addAll(revokedSignatureUuids) || change;
		if (change)
		{
			clearSignature();
			return true;
		}
		else
			return false;
	}

	public Date getSignatureDate()
	{
		return getEntity().getSignatureDate();
	}

	public void setSignatureDate(Date signatureDate)
	{
		getEntity().setSignatureDate(signatureDate);
	}

	protected void setSignatureDate()
	{
		setSignatureDate(new Date());
	}

	private void setSignatureVersion(int signatureVersion)
	{
		getEntity().setSignatureVersion(signatureVersion);
	}

	public int getSignatureVersion()
	{
		return getEntity().getSignatureVersion();
	}

	public SignatureData getSignatureData()
	{
		return getEntity().getSignatureData();
	}

	public void setSignatureData(SignatureData signatureData)
	{
		getEntity().setSignatureData(signatureData);
	}

	protected void signatureDataOut(DataOutput out, Transaction transaction) throws SignatureVersionException
	{
		if (getSignatureVersion() != 0)
			throw new SignatureVersionException();
		try
		{
			UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			NullableProtocol<UUID> nullableUuidProtocol = new NullableProtocol<>(0, uuidProtocol);
			NamespaceProtocol namespaceProtocol = new NamespaceProtocol(0);
			NullableProtocol<Date> nullableDateProtocol = new NullableProtocol<>(0, new DateProtocol(0));
			NonReturningCollectionProtocol<UUID> uuidCollectionProtocol = new NonReturningCollectionProtocol<>(0, uuidProtocol);

			uuidProtocol.send(out, getStatementUuid());
			namespaceProtocol.send(out, getPrefix());
			uuidProtocol.send(out, getDelegateUuid());
			nullableUuidProtocol.send(out, getAuthorizerUuid());
			uuidCollectionProtocol.send(out, getRevokedSignatureUuids());
			nullableDateProtocol.send(out, getSignatureDate());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void sign(Transaction transaction) throws BadSignatoryException
	{
		try
		{
			Signatory signatory = getDelegate(transaction).getSignatory(transaction);
			if (!(signatory instanceof PrivateSignatory))
				throw new BadSignatoryException("Don't have the private data for this author. Maybe try entering a passphrase?");
			PrivateSignatory privateSignatory = (PrivateSignatory) signatory;
			Signer signer = privateSignatory.signer();
			setSignatureDate();
			setSignatureVersion(signingSignatureVersion);
			try
			{
				signatureDataOut(signer.dataOutput(), transaction);
			}
			catch (SignatureVersionException e)
			{
				throw new Error("signingSignatureVersion must be supported", e);
			}
			setSignatureData(signer.sign());
			setCheckAuthoritySignatures();
		}
		catch (InvalidKeyException e)
		{
			throw new RuntimeException(e);
		}

	}

	public void verify(Transaction transaction) throws SignatureVerifyException, SignatureVersionException
	{
		try
		{
			Signatory signatory = getDelegate(transaction).getSignatory(transaction);
			Verifier verifier = signatory.verifier(getSignatureData());
			signatureDataOut(verifier.dataOutput(), transaction);
			if (!verifier.verify())
				throw new SignatureVerifyException();
		}
		catch (InvalidKeyException | IncompleteDataSignatureException | NoSuchAlgorithmException e)
		{
			throw new SignatureVerifyException(e);
		}
		finally
		{

		}
	}

	public boolean isSigned()
	{
		return getSignatureData() != null;
	}

	private void clearSignature()
	{
		setSignatureDate(null);
		setSignatureData(null);
		setCheckAuthoritySignatures();
	}

	public Statement getStatement(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getStatementUuid());
	}

	public StatementAuthority getStatementAuthority(Transaction transaction)
	{
		return persistenceManager.getStatementAuthority(transaction, getStatementUuid());
	}

	public DelegateTreeNode getDelegateTreeNode(Transaction transaction)
	{
		return persistenceManager.getDelegateTreeNode(transaction, getStatementUuid(), getPrefix());
	}

	public DelegateTreeRootNode getDelegateTreeRootNode(Transaction transaction)
	{
		return (DelegateTreeRootNode) persistenceManager.getDelegateTreeNode(transaction, getStatementUuid(), RootNamespace.instance);
	}

	public void update(Transaction transaction, Signatory authorizer, Collection<UUID> revokedSignatureUuids, Date signatureDate, int signatureVersion,
			SignatureData signatureData) throws SignatureVerifyException, SignatureVersionException
	{
		setAuthorizer(authorizer);
		setRevokedSignatureUuids(revokedSignatureUuids);
		setSignatureDate(signatureDate);
		setSignatureVersion(signatureVersion);
		setSignatureData(signatureData);
		if (signatureData != null)
			verify(transaction);
		persistenceUpdate(transaction);
	}

	public void delete(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		DelegateTreeNode node = getDelegateTreeNode(transaction);
		node.deleteDelegateAuthorizer(transaction, this);
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
		Person delegate = getDelegate(transaction);
		if (delegate == null)
			return "*null*";
		Signatory authorizer = getAuthorizer(transaction);
		if (authorizer == null)
			return "*null*";
		return getDelegateTreeNode(transaction).toString(transaction) + ": " + "[" + delegate.toString(transaction) + "]" + ": " + authorizer.toString();
	}

}
