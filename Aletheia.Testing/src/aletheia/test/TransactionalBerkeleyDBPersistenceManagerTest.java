package aletheia.test;

import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;

public abstract class TransactionalBerkeleyDBPersistenceManagerTest extends BerkeleyDBPersistenceManagerTest
{
	public TransactionalBerkeleyDBPersistenceManagerTest(boolean readOnly)
	{
		super(readOnly);
	}

	public TransactionalBerkeleyDBPersistenceManagerTest()
	{
		super();
	}

	@Override
	protected final void run(BerkeleyDBPersistenceManager persistenceManager) throws Exception
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			run(persistenceManager, transaction);
			if (!isReadOnly())
				transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	protected abstract void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception;

}
