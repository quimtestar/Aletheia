package aletheia.test.unsorted;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.authority.Auth;
import aletheia.gui.cli.command.authority.AuthRec;
import aletheia.gui.cli.command.authority.Sign;
import aletheia.gui.cli.command.authority.SignRec;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class Test0012 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public Test0012()
	{
		super(false);
	}

	private void runTransactionalCommand(TransactionalCommand command) throws Exception
	{
		try
		{
			Method runMethod = TransactionalCommand.class.getDeclaredMethod("runTransactional");
			runMethod.setAccessible(true);
			runMethod.invoke(command);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			if (e.getCause() instanceof Exception)
				throw (Exception) e.getCause();
			else if (e.getCause() instanceof Error)
				throw (Error) e.getCause();
			else
				throw new RuntimeException(e.getCause());
		}
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		enterPassphrase(persistenceManager);
		PrivatePerson quimtestar = persistenceManager.privatePersonsByNick(transaction).get("quimtestar");
		Context choiceCtx = persistenceManager.getContext(transaction, UUID.fromString("42cc8199-8159-5567-b65c-db023f95eaa3"));
		for (Statement statement : choiceCtx.statements(transaction).values())
		{
			if (!statement.isValidSignature(transaction))
			{
				if (statement instanceof Context)
				{
					Context context = (Context) statement;
					runTransactionalCommand(new AuthRec(AletheiaCliConsole.cliConsole(persistenceManager), transaction, quimtestar, context));
					runTransactionalCommand(new SignRec(AletheiaCliConsole.cliConsole(persistenceManager), transaction, context,
							context.getAuthority(transaction), quimtestar));
				}
				else
					runTransactionalCommand(new Auth(AletheiaCliConsole.cliConsole(persistenceManager), transaction, quimtestar, statement));
				runTransactionalCommand(
						new Sign(AletheiaCliConsole.cliConsole(persistenceManager), transaction, statement, statement.getAuthority(transaction), quimtestar));
			}
		}
		System.out.println("done!");

	}

}
