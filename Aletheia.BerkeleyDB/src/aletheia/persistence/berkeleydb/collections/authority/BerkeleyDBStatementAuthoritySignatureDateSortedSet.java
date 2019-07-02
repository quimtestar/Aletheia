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

import java.util.Comparator;
import java.util.Date;
import java.util.NoSuchElementException;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureDateSortedSet;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSortedSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBStatementAuthoritySignatureDateSortedSet extends AbstractCloseableSet<StatementAuthoritySignature>
		implements StatementAuthoritySignatureDateSortedSet
{
	private final static Comparator<StatementAuthoritySignature> comparator = new Comparator<>()
	{
		@Override
		public int compare(StatementAuthoritySignature o1, StatementAuthoritySignature o2)
		{
			return ((BerkeleyDBStatementAuthoritySignatureEntity) o1.getEntity()).getStatementSignatureDateKeyData()
					.compareTo(((BerkeleyDBStatementAuthoritySignatureEntity) o2.getEntity()).getStatementSignatureDateKeyData());
		}
	};

	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final StatementAuthority statementAuthority;
	private final SecondaryIndex<StatementSignatureDateKeyData, PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> index;
	private final StatementAuthoritySignature from;
	private final StatementAuthoritySignature to;
	private final BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData fromStatementSignatureDateKeyData;
	private final BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData toStatementSignatureDateKeyData;

	private BerkeleyDBStatementAuthoritySignatureDateSortedSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			StatementAuthority statementAuthority, StatementAuthoritySignature from, StatementAuthoritySignature to)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.statementAuthority = statementAuthority;
			this.index = persistenceManager.getEntityStore().statementAuthoritySignatureEntityStatementSignatureDateSecondaryIndex();
			this.from = from;
			this.to = to;
			if (from == null)
				this.fromStatementSignatureDateKeyData = new BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData(
						statementAuthority.getStatementUuid(), new Date(Long.MIN_VALUE));
			else
				this.fromStatementSignatureDateKeyData = new BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData(
						statementAuthority.getStatementUuid(), from.getSignatureDate());
			if (to == null)
				this.toStatementSignatureDateKeyData = new BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData(
						statementAuthority.getStatementUuid(), new Date(Long.MAX_VALUE));
			else
				this.toStatementSignatureDateKeyData = new BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData(
						statementAuthority.getStatementUuid(), to.getSignatureDate());
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBStatementAuthoritySignatureDateSortedSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			StatementAuthority statementAuthority)
	{
		this(persistenceManager, transaction, statementAuthority, null, null);
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	public StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	@Override
	public Comparator<StatementAuthoritySignature> comparator()
	{
		return comparator;
	}

	@Override
	public BerkeleyDBStatementAuthoritySignatureDateSortedSet subSet(StatementAuthoritySignature fromElement, StatementAuthoritySignature toElement)
	{
		StatementAuthoritySignature fromElement_;
		if (fromElement == null)
			fromElement_ = from;
		else if ((from == null) || comparator.compare(from, fromElement) <= 0)
			fromElement_ = fromElement;
		else
			fromElement_ = from;
		StatementAuthoritySignature toElement_;
		if (toElement == null)
			toElement_ = to;
		else if ((to == null) || comparator.compare(toElement, to) >= 0)
			toElement_ = toElement;
		else
			toElement_ = to;
		return new BerkeleyDBStatementAuthoritySignatureDateSortedSet(persistenceManager, transaction, statementAuthority, fromElement_, toElement_);
	}

	@Override
	public CloseableSortedSet<StatementAuthoritySignature> headSet(StatementAuthoritySignature toElement)
	{
		return subSet(null, toElement);
	}

	@Override
	public CloseableSortedSet<StatementAuthoritySignature> tailSet(StatementAuthoritySignature fromElement)
	{
		return subSet(fromElement, null);
	}

	@Override
	public StatementAuthoritySignature first()
	{
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = transaction.entities(index, fromStatementSignatureDateKeyData, true,
				toStatementSignatureDateKeyData, false);
		try
		{
			BerkeleyDBStatementAuthoritySignatureEntity entity = transaction.first(cursor);
			if (entity == null)
				throw new NoSuchElementException();
			return persistenceManager.entityToStatementAuthoritySignature(entity);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public StatementAuthoritySignature last()
	{
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = transaction.entities(index, fromStatementSignatureDateKeyData, true,
				toStatementSignatureDateKeyData, false);
		try
		{
			BerkeleyDBStatementAuthoritySignatureEntity entity = transaction.last(cursor);
			if (entity == null)
				throw new NoSuchElementException();
			return persistenceManager.entityToStatementAuthoritySignature(entity);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	private abstract class InnerIterator implements CloseableIterator<StatementAuthoritySignature>
	{
		private final EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor;
		private BerkeleyDBStatementAuthoritySignatureEntity next;

		public InnerIterator()
		{
			super();
			this.cursor = transaction.entities(index, fromStatementSignatureDateKeyData, true, toStatementSignatureDateKeyData, false);
			advance();
		}

		public abstract BerkeleyDBStatementAuthoritySignatureEntity cursorAdvance(EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor);

		public void advance()
		{
			next = cursorAdvance(cursor);
			if (next == null)
				transaction.close(cursor);
		}

		@Override
		public boolean hasNext()
		{
			return next != null;
		}

		@Override
		public StatementAuthoritySignature next()
		{
			BerkeleyDBStatementAuthoritySignatureEntity entity = next;
			if (entity == null)
				throw new NoSuchElementException();
			advance();
			return persistenceManager.entityToStatementAuthoritySignature(entity);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void close()
		{
			transaction.close(cursor);
		}

		@Override
		protected void finalize() throws Throwable
		{
			close();
		}

	}

	@Override
	public CloseableIterator<StatementAuthoritySignature> iterator()
	{
		return new InnerIterator()
		{
			@Override
			public BerkeleyDBStatementAuthoritySignatureEntity cursorAdvance(EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor)
			{
				return transaction.next(cursor);
			}
		};
	}

	@Override
	public CloseableIterator<StatementAuthoritySignature> reverseIterator()
	{
		return new InnerIterator()
		{
			@Override
			public BerkeleyDBStatementAuthoritySignatureEntity cursorAdvance(EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor)
			{
				return transaction.prev(cursor);
			}
		};
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = transaction.entities(index, fromStatementSignatureDateKeyData, true,
				toStatementSignatureDateKeyData, false);
		try
		{
			int n = 0;
			while (transaction.next(cursor) != null)
				n++;
			return n;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = transaction.entities(index, fromStatementSignatureDateKeyData, true,
				toStatementSignatureDateKeyData, false);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof StatementAuthoritySignature))
			return false;
		StatementAuthoritySignature sas = (StatementAuthoritySignature) o;
		BerkeleyDBStatementAuthoritySignatureEntity entity = transaction.get(index,
				new BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData(sas.getStatementUuid(), sas.getSignatureDate()));
		if (entity == null)
			return false;
		StatementAuthoritySignature sas_ = persistenceManager.entityToStatementAuthoritySignature(entity);
		return sas_.equals(sas);
	}

}
