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
package aletheia.persistence.berkeleydb.collections.authority;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.Person;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeRootNodeEntity;
import aletheia.persistence.collections.authority.DelegateTreeRootNodeSetBySuccessor;

public class BerkeleyDBDelegateTreeRootNodeSetBySuccessor extends BerkeleyDBAbstractDelegateTreeRootNodeSet implements DelegateTreeRootNodeSetBySuccessor
{
	private final Person successor;
	private final UUIDKey successorUuidKey;
	private final SecondaryIndex<UUIDKey, PrimaryKeyData, BerkeleyDBDelegateTreeRootNodeEntity> secondaryIndex;
	private final EntityIndex<PrimaryKeyData, BerkeleyDBDelegateTreeRootNodeEntity> subIndex;

	public BerkeleyDBDelegateTreeRootNodeSetBySuccessor(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Person successor)
	{
		super(persistenceManager, transaction);
		this.successor = successor;
		this.successorUuidKey = new UUIDKey(successor.getUuid());
		this.secondaryIndex = getPersistenceManager().getEntityStore().delegateTreeRootNodeEntitySuccessorUuidKeysSecondaryIndex();
		this.subIndex = secondaryIndex.subIndex(new UUIDKey(successor.getUuid()));
	}

	@Override
	public Person getSuccessor()
	{
		return successor;
	}

	@Override
	protected EntityIndex<PrimaryKeyData, BerkeleyDBDelegateTreeRootNodeEntity> index()
	{
		return subIndex;
	}

	@Override
	public boolean isEmpty()
	{
		return !getTransaction().contains(secondaryIndex, successorUuidKey);
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBDelegateTreeRootNodeEntity> cursor = getTransaction().entities(secondaryIndex, successorUuidKey, true, successorUuidKey, true);
		try
		{
			BerkeleyDBDelegateTreeRootNodeEntity entity = getTransaction().first(cursor);
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
