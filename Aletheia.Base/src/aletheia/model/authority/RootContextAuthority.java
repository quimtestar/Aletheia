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

import java.util.Date;
import java.util.UUID;

import aletheia.model.identifier.Namespace;
import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.RootContextAuthorityEntity;
import aletheia.utilities.collections.CloseableMap;

public class RootContextAuthority extends ContextAuthority
{

	public RootContextAuthority(PersistenceManager persistenceManager, RootContextAuthorityEntity entity)
	{
		super(persistenceManager, entity);
	}

	protected RootContextAuthority(PersistenceManager persistenceManager, RootContext rootContext, UUID authorUuid, Date creationDate)
	{
		super(persistenceManager, RootContextAuthorityEntity.class, rootContext, authorUuid, creationDate);
	}

	protected RootContextAuthority(PersistenceManager persistenceManager, RootContext rootContext, Person author, Date creationDate)
	{
		this(persistenceManager, rootContext, author.getUuid(), creationDate);
	}

	@Override
	public RootContextAuthorityEntity getEntity()
	{
		return (RootContextAuthorityEntity) super.getEntity();
	}

	@Override
	public RootContext getStatement(Transaction transaction)
	{
		try
		{
			return (RootContext) super.getStatement(transaction);
		}
		catch (ClassCastException e)
		{
			throw e;
		}
	}

	public RootContext getRootContext(Transaction transaction)
	{
		return getStatement(transaction);
	}

	@Override
	public RootContextAuthority refresh(Transaction transaction)
	{
		return (RootContextAuthority) super.refresh(transaction);
	}

	public UUID getSignatureUuid()
	{
		return getEntity().getSignatureUuid();
	}

	private void setSignatureUuid(UUID uuid)
	{
		getEntity().setSignatureUuid(uuid);
	}

	private UUID calcSignatureUuid(Transaction transaction)
	{
		StatementAuthoritySignature signature = signatureMap(transaction).get(getAuthor(transaction).getSignatory(transaction));
		if (signature == null)
			return null;
		return signature.getSignatureData().uuid();
	}

	protected void updateSignatureUuid(Transaction transaction)
	{
		setSignatureUuid(calcSignatureUuid(transaction));
	}

	@Override
	protected void deleteSignature(Transaction transaction, UUID authorizerUuid)
	{
		super.deleteSignature(transaction, authorizerUuid);
		updateSignatureUuid(transaction);
		persistenceUpdate(transaction);
	}

	@Override
	protected void clearSignaturesNoCheckValidSignature(Transaction transaction)
	{
		super.clearSignaturesNoCheckValidSignature(transaction);
		updateSignatureUuid(transaction);
		persistenceUpdate(transaction);
	}

	@Override
	public ContextAuthority getContextAuthority(Transaction transaction)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableMap<Signatory, DelegateAuthorizer> delegateAuthorizerByAuthorizerMap(Transaction transaction, Namespace prefix)
	{
		return localDelegateAuthorizerByAuthorizerMapPrefixes(transaction, prefix);
	}

	@Override
	public CloseableMap<Person, DelegateAuthorizer> delegateAuthorizerMap(Transaction transaction, Namespace namespace)
	{
		return localDelegateAuthorizerMap(transaction, namespace);
	}

}
