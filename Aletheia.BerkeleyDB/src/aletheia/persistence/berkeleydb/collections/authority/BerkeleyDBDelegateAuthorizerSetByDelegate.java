/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb.collections.authority;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.Person;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.collections.authority.DelegateAuthorizerSetByDelegate;

public class BerkeleyDBDelegateAuthorizerSetByDelegate extends BerkeleyDBAbstractDelegateAuthorizerSet implements DelegateAuthorizerSetByDelegate
{
	private final Person delegate;
	private final UUIDKey delegateUuidKey;
	private final SecondaryIndex<UUIDKey, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> secondaryIndex;
	private final EntityIndex<BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> subIndex;

	public BerkeleyDBDelegateAuthorizerSetByDelegate(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Person delegate)
	{
		super(persistenceManager, transaction);
		this.delegate = delegate;
		this.delegateUuidKey = new UUIDKey(delegate.getUuid());
		this.secondaryIndex = getPersistenceManager().getEntityStore().delegateAuthorizerEntityDelegateSecondaryIndex();
		this.subIndex = secondaryIndex.subIndex(new UUIDKey(delegate.getUuid()));
	}

	@Override
	public Person getDelegate()
	{
		return delegate;
	}

	@Override
	protected EntityIndex<BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> index()
	{
		return subIndex;
	}

	@Override
	public boolean isEmpty()
	{
		return !getTransaction().contains(secondaryIndex, delegateUuidKey);
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBDelegateAuthorizerEntity> cursor = getTransaction().entities(secondaryIndex, delegateUuidKey, true, delegateUuidKey, true);
		try
		{
			BerkeleyDBDelegateAuthorizerEntity entity = getTransaction().first(cursor);
			if (entity == null)
				return 0;
			return getTransaction().count(cursor);
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

}
