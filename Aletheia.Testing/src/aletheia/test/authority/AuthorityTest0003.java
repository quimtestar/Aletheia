/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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
package aletheia.test.authority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.authority.Auth;
import aletheia.gui.cli.command.authority.AuthRec;
import aletheia.gui.cli.command.authority.Sign;
import aletheia.gui.cli.command.authority.SignRec;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

/**
 * Batch authoring and signing
 */
public class AuthorityTest0003 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public AuthorityTest0003()
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
		Namespace prefix = Namespace.parse("");
		for (Context ctx : choiceCtx.statementPath(transaction))
		{
			for (Statement statement : ctx.localStatements(transaction).values())
			{
				if (prefix.isPrefixOf(statement.getIdentifier()) && statement.isProved() && !statement.isSignedProof(transaction))
				{
					System.out.println(statement.statementPathString(transaction));
					if (statement instanceof Context)
						if (StreamSupport.stream(((Context) statement).descendentStatements(transaction).spliterator(), false)
								.filter(st -> !st.isValidSignature(transaction)).findAny().isEmpty())
							continue;
					Set<Statement> useless = statement instanceof Context ? ((Context) statement).uselessDescendents(transaction) : Collections.emptySet();
					if (useless.stream().filter(st -> !((st instanceof Assumption) && (!ctx.equals(st.getContext(transaction))))).findAny().isPresent())
					{
						System.out.println("      -> useless statements!");
						continue;
					}
					Context hc = statement.highestContext(transaction);
					if (!ctx.equals(hc))
					{
						System.out.println("      -> higher context!");
						continue;
					}
					StatementAuthority stAuth = statement.getAuthority(transaction);
					if (statement instanceof Context)
						runTransactionalCommand(new AuthRec(AletheiaCliConsole.cliConsole(persistenceManager), transaction, quimtestar, (Context) statement));
					else if (stAuth == null)
						runTransactionalCommand(new Auth(AletheiaCliConsole.cliConsole(persistenceManager), transaction, quimtestar, statement));
					stAuth = statement.getAuthority(transaction);
					if (statement instanceof Context)
					{
						Context context = (Context) statement;
						runTransactionalCommand(
								new SignRec(AletheiaCliConsole.cliConsole(persistenceManager), transaction, context, (ContextAuthority) stAuth, quimtestar));
					}
					if (!stAuth.isValidSignature())
						runTransactionalCommand(new Sign(AletheiaCliConsole.cliConsole(persistenceManager), transaction, statement, stAuth, quimtestar));
				}
			}
		}
		System.out.println("done!");

	}

}
