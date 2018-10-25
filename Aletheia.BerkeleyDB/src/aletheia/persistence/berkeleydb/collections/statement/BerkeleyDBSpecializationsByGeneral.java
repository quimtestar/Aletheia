package aletheia.persistence.berkeleydb.collections.statement;

import java.util.NoSuchElementException;

import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBSpecializationEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.SpecializationsByGeneral;
import aletheia.persistence.entities.statement.SpecializationEntity;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBSpecializationsByGeneral extends AbstractCloseableSet<Specialization> implements SpecializationsByGeneral
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBSpecializationEntity> specializationEntityGeneralSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final Statement general;
	private final UUIDKey uuidKey;

	public BerkeleyDBSpecializationsByGeneral(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Statement general)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.specializationEntityGeneralSecondaryIndex = persistenceManager.getEntityStore().specializationEntityGeneralSecondaryIndex();
			this.transaction = transaction;
			this.general = general;
			this.uuidKey = ((BerkeleyDBStatementEntity) general.getEntity()).getUuidKey();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}

	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	@Override
	public Statement getGeneral()
	{
		return general;
	}

	@Override
	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableIterator<Specialization> iterator()
	{
		final EntityCursor<BerkeleyDBSpecializationEntity> cursor = transaction.entities(specializationEntityGeneralSecondaryIndex, uuidKey, true, uuidKey,
				true);
		return new CloseableIterator<Specialization>()
		{
			SpecializationEntity next;

			{
				next = transaction.next(cursor);
			}

			@Override
			public boolean hasNext()
			{
				if (next == null)
				{
					transaction.close(cursor);
					return false;
				}
				return true;
			}

			@Override
			public Specialization next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				SpecializationEntity entity = next;
				next = transaction.next(cursor);
				return persistenceManager.specializationEntityToStatement(entity);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected void finalize() throws Throwable
			{
				close();
				super.finalize();
			}

			@Override
			public void close()
			{
				transaction.close(cursor);
			}

		};

	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBSpecializationEntity> cursor = transaction.entities(specializationEntityGeneralSecondaryIndex, uuidKey, true, uuidKey, true);
		try
		{
			if (transaction.first(cursor) == null)
				return 0;
			return transaction.count(cursor);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(specializationEntityGeneralSecondaryIndex, uuidKey, true, uuidKey, true);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

}
