/*******************************************************************************
 * Copyright (c) 2019, 2020 Quim Testar.
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
 *******************************************************************************/
package aletheia.test.local;

import java.io.File;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBConfiguration;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.Test;

public class LocalTest0000 extends Test
{

	@Override
	public void run() throws Exception
	{
		BerkeleyDBConfiguration configuration = new BerkeleyDBConfiguration();
		configuration.setDbFile(new File("/home/quimtestar/.Aletheia/aletheiadb_training"));
		configuration.setReadOnly(false);
		try (BerkeleyDBPersistenceManager persistenceManager = new BerkeleyDBPersistenceManager(configuration))
		{
			try (Transaction transaction = persistenceManager.beginTransaction())
			{
				Stack<Context> stack = new Stack<>();
				stack.push(persistenceManager.getRootContext(transaction, UUID.fromString("3e6fc222-aefa-5551-b182-7a736264f03b")));
				while (!stack.isEmpty())
				{
					Context context = stack.pop();
					for (Statement statement : context.localStatements(transaction).values())
					{
						if (statement instanceof UnfoldingContext)
							((UnfoldingContext) statement).getOrCreateLocal(transaction).setSubscribeProof(transaction, true);
						if (statement instanceof Context)
							if (((Context) statement).isSubscribeStatements(transaction))
								stack.push((Context) statement);
					}
				}
				transaction.commit();
			}
		}

	}

}
