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
package aletheia.persistence.berkeleydb;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.exceptions.BerkeleyDBPersistenceException;
import aletheia.utilities.MiscUtilities;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.PrimaryIndex;

public class BerkeleyDBTransaction extends Transaction
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final com.sleepycat.je.Transaction dbTransaction;
	private final CursorConfig defaultCursorConfig;
	private final long timeOut;
	private final Map<EntityCursor<?>, List<StackTraceElement>> openedCursors;

	protected BerkeleyDBTransaction(BerkeleyDBPersistenceManager persistenceManager, TransactionConfig config, long timeOut)
	{
		super(persistenceManager);
		try
		{
			this.dbTransaction = persistenceManager.getEnvironment().beginTransaction(null, config);
			persistenceManager.registerTransaction(this);
			this.dbTransaction.setLockTimeout(timeOut, TimeUnit.MILLISECONDS);
			this.defaultCursorConfig = new CursorConfig();
			this.defaultCursorConfig.setReadCommitted(config.getReadCommitted());
			this.defaultCursorConfig.setReadUncommitted(config.getReadUncommitted());
			this.timeOut = timeOut;
			this.openedCursors = Collections.synchronizedMap(new HashMap<EntityCursor<?>, List<StackTraceElement>>());
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	protected BerkeleyDBTransaction(BerkeleyDBPersistenceManager persistenceManager, TransactionConfig config)
	{
		this(persistenceManager, config, 0);
	}

	protected BerkeleyDBTransaction(BerkeleyDBPersistenceManager persistenceManager)
	{
		this(persistenceManager, null);
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return (BerkeleyDBPersistenceManager) super.getPersistenceManager();
	}

	public com.sleepycat.je.Transaction getDbTransaction()
	{
		return dbTransaction;
	}

	public long getTimeOut()
	{
		return timeOut;
	}

	public class BerkeleyDBPersistenceOpenedCursorsException extends BerkeleyDBPersistenceException
	{
		private static final long serialVersionUID = 1994197443707568963L;

	}

	@Override
	public synchronized void commit()
	{
		try
		{
			if (!openedCursors.isEmpty())
			{
				closeOpenedCursors();
				throw new BerkeleyDBPersistenceOpenedCursorsException();
			}
			dbTransaction.commit();
			getPersistenceManager().unregisterTransaction(this);
			super.commit();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized void closeOpenedCursors()
	{
		try
		{
			for (Map.Entry<EntityCursor<?>, List<StackTraceElement>> e : openedCursors.entrySet())
			{
				EntityCursor<?> cur = e.getKey();
				logger.warn("Opened cursor:" + cur);
				if (e.getValue() != null)
				{
					for (StackTraceElement ste : e.getValue())
						logger.debug("\tat " + ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")");
				}
				cur.close();
			}
			openedCursors.clear();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	@Override
	public synchronized void abort()
	{
		try
		{
			closeOpenedCursors();
			dbTransaction.abort();
			getPersistenceManager().unregisterTransaction(this);
			super.abort();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	@Override
	public String toString()
	{
		return dbTransaction.toString();
	}

	protected synchronized void addOpenedCursor(EntityCursor<?> cursor)
	{
		List<StackTraceElement> stackTraceList = getPersistenceManager().isDebug() ? MiscUtilities.stackTraceList(1) : null;
		openedCursors.put(cursor, stackTraceList);
	}

	public synchronized void close(EntityCursor<?> cursor)
	{
		try
		{
			if (openedCursors.containsKey(cursor))
			{
				openedCursors.remove(cursor);
				cursor.close();
			}
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public CursorConfig getDefaultCursorConfig()
	{
		return defaultCursorConfig;
	}

	public long getDbTransactionId()
	{
		return dbTransaction.getId();
	}

	public synchronized <K, V> EntityCursor<K> keys(EntityIndex<K, V> index)
	{
		try
		{
			EntityCursor<K> cursor = index.keys(getDbTransaction(), getDefaultCursorConfig());
			addOpenedCursor(cursor);
			return cursor;
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> EntityCursor<V> entities(EntityIndex<K, V> index)
	{
		try
		{
			EntityCursor<V> cursor = index.entities(getDbTransaction(), getDefaultCursorConfig());
			addOpenedCursor(cursor);
			return cursor;
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> EntityCursor<K> keys(EntityIndex<K, V> index, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
	{
		try
		{
			EntityCursor<K> cursor = index.keys(getDbTransaction(), fromKey, fromInclusive, toKey, toInclusive, getDefaultCursorConfig());
			addOpenedCursor(cursor);
			return cursor;
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> EntityCursor<V> entities(EntityIndex<K, V> index, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
	{
		try
		{
			EntityCursor<V> cursor = index.entities(getDbTransaction(), fromKey, fromInclusive, toKey, toInclusive, getDefaultCursorConfig());
			addOpenedCursor(cursor);
			return cursor;
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> V get(EntityIndex<K, V> index, K key)
	{
		try
		{
			return index.get(getDbTransaction(), key, LockMode.DEFAULT);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> boolean contains(EntityIndex<K, V> index, K key)
	{
		try
		{
			return index.contains(getDbTransaction(), key, LockMode.DEFAULT);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> boolean lock(EntityIndex<K, V> index, K key)
	{
		try
		{
			return index.contains(getDbTransaction(), key, LockMode.RMW);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, V> boolean delete(EntityIndex<K, V> index, K key)
	{
		try
		{
			return index.delete(getDbTransaction(), key);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, E> E put(PrimaryIndex<K, E> index, E entity)
	{
		try
		{
			return index.put(getDbTransaction(), entity);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, E> boolean putNoOverwrite(PrimaryIndex<K, E> index, E entity)
	{
		try
		{
			return index.putNoOverwrite(getDbTransaction(), entity);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <K, E> void putNoReturn(PrimaryIndex<K, E> index, E entity)
	{
		try
		{
			index.putNoReturn(getDbTransaction(), entity);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> int count(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.count();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> long countEstimate(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.countEstimate();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V current(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.current();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> boolean delete(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.delete();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> EntityCursor<V> dup(EntityCursor<V> cursor)
	{
		try
		{
			EntityCursor<V> other = cursor.dup();
			addOpenedCursor(other);
			return other;
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V first(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.first();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V last(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.last();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V next(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.next();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V nextDup(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.nextDup();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V nextNoDup(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.nextNoDup();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V prev(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.prev();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V prevDup(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.prevDup();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> V prevNoDup(EntityCursor<V> cursor)
	{
		try
		{
			return cursor.prevNoDup();
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized <V> boolean update(EntityCursor<V> cursor, V value)
	{
		try
		{
			return cursor.update(value);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

	public synchronized void truncateClass(Class<?> entityClass)
	{
		try
		{
			getPersistenceManager().getEntityStore().truncateClass(getDbTransaction(), entityClass);
		}
		catch (DatabaseException e)
		{
			throw getPersistenceManager().convertDatabaseException(e);
		}
	}

}
