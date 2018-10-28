/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.test.unsorted;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.authority.Sign;
import aletheia.gui.cli.command.authority.SignRec;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class Test0021 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public Test0021()
	{
		super();
		setReadOnly(false);
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
		for (Context ctx : choiceCtx.statementPath(transaction))
		{
			for (Statement statement : ctx.localStatements(transaction).values())
			{
				StatementAuthority stAuth = statement.getAuthority(transaction);
				if (stAuth != null && !stAuth.isValidSignature())
				{
					try (Transaction transaction2 = persistenceManager.beginTransaction())
					{
						if (statement instanceof Context)
						{
							Context context = (Context) statement;
							runTransactionalCommand(new SignRec(AletheiaCliConsole.cliConsole(persistenceManager), transaction2, context,
									(ContextAuthority) stAuth, quimtestar));
						}
						runTransactionalCommand(new Sign(AletheiaCliConsole.cliConsole(persistenceManager), transaction2, statement, stAuth, quimtestar));
						transaction2.commit();
					}
				}
			}
		}
		System.out.println("done!");

	}

}
