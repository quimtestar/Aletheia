package aletheia.test.authority;

import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class AuthorityTest0004 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public AuthorityTest0004()
	{
		super();
		setReadOnly(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		Context choiceCtx = persistenceManager.getContext(transaction, UUID.fromString("42cc8199-8159-5567-b65c-db023f95eaa3"));
		Statement statement = choiceCtx.identifierToStatement(transaction).get(Identifier.parse("Real.lebesgue.Measurable.set"));
		statement.getAuthority(transaction).clearSignatures(transaction);
	}

}
