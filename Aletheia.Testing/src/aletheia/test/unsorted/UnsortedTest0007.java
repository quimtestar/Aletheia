/*******************************************************************************
 * Copyright (c) 2023 Quim Testar.
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
package aletheia.test.unsorted;

import java.util.Stack;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.local.ContextLocal;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class UnsortedTest0007 extends TransactionalBerkeleyDBPersistenceManagerTest
{
	private static final Logger logger = LoggerManager.instance.logger();

	public UnsortedTest0007()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		int nStatements = persistenceManager.statements(transaction).fastSize();
		logger.info("Statements: {}", nStatements);
		Stack<ContextLocal> stack = new Stack<>();
		stack.addAll(persistenceManager.subscribeStatementsRootContextLocalSet(transaction));
		int nDeclarations = 0;
		int nDefinitions = 0;
		int nAxioms = 0;
		int nTheorems = 0;
		int nOther = 0;
		while (!stack.isEmpty())
		{
			ContextLocal contextLocal = stack.pop();
			Context context = contextLocal.getStatement(transaction);
			for (Statement statement : context.localStatements(transaction).values())
			{
				if (statement.isSignedProof(transaction))
					if (statement instanceof Declaration)
						nDeclarations++;
					else if (statement instanceof Context)
					{
						if (statement instanceof UnfoldingContext)
							nDefinitions++;
						else
							nTheorems++;
					}
					else if (statement instanceof Assumption)
						nAxioms++;
					else
						nOther++;
			}
			stack.addAll(contextLocal.subscribeStatementsContextLocalSet(transaction));
		}
		logger.info("Declarations: {}", nDeclarations);
		logger.info("Definitions: {}", nDefinitions);
		logger.info("Axioms: {}", nAxioms);
		logger.info("Theorems: {}", nTheorems);
		logger.info("Other: {}", nOther);
	}

}
