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

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.SignedDependenciesLocalStatementAuthoritySet;
import aletheia.persistence.collections.authority.SignedProofLocalStatementAuthoritySet;
import aletheia.persistence.entities.authority.ContextAuthorityEntity;

public class ContextAuthority extends StatementAuthority
{

	public ContextAuthority(PersistenceManager persistenceManager, ContextAuthorityEntity entity)
	{
		super(persistenceManager, entity);
	}

	protected ContextAuthority(PersistenceManager persistenceManager, Class<? extends ContextAuthorityEntity> entityClass, Statement statement,
			UUID authorUuid, Date creationDate)
	{
		super(persistenceManager, entityClass, statement, authorUuid, creationDate);
	}

	protected ContextAuthority(PersistenceManager persistenceManager, Context context, UUID authorUuid, Date creationDate)
	{
		this(persistenceManager, ContextAuthorityEntity.class, context, authorUuid, creationDate);
	}

	protected ContextAuthority(PersistenceManager persistenceManager, Context context, Person author, Date creationDate)
	{
		this(persistenceManager, context, author.getUuid(), creationDate);
	}

	@Override
	public ContextAuthorityEntity getEntity()
	{
		return (ContextAuthorityEntity) super.getEntity();
	}

	public SignedDependenciesLocalStatementAuthoritySet signedDependenciesLocalAuthoritiesSet(Transaction transaction)
	{
		return getPersistenceManager().signedDependenciesLocalStatementAuthoritySet(transaction, this);
	}

	public SignedProofLocalStatementAuthoritySet signedProofLocalAuthoritiesSet(Transaction transaction)
	{
		return getPersistenceManager().signedProofLocalStatementAuthoritySet(transaction, this);
	}

}
