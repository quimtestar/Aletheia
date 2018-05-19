package aletheia.test;

import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;

public abstract class TransactionalBerkeleyDBPersistenceManagerTest extends BerkeleyDBPersistenceManagerTest
{
	private final boolean commit;

	protected TransactionalBerkeleyDBPersistenceManagerTest(boolean commit)
	{
		super();
		this.commit = commit;
	}

	@Override
	protected final void run(BerkeleyDBPersistenceManager persistenceManager) throws Exception
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			run(persistenceManager, transaction);
			if (commit)
				transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	protected abstract void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception;

}
