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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;

import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class Test0001 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public Test0001()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws FileNotFoundException
	{
		UUID uuid = UUID.fromString("8bf0bf35-c253-5257-b3ef-49b59beabc77"); // Set.th.epsilon_induction
		//UUID uuid=UUID.fromString("e7a380b5-3b72-5341-b868-56226e163779");		// Prop.And.th.symm/s001
		//UUID uuid=UUID.fromString("360e41c6-2713-50ba-a22f-1f9d69dd1143");		// Set
		//UUID uuid = UUID.fromString("db8546ce-ab3b-5441-98ca-b2fed77e695d"); 		// Set.Equal.def
		//UUID uuid = UUID.fromString("9209090d-fc0c-5e24-b1e4-1d509857a328"); 		// Natural.two.th.sum.one
		//UUID uuid = UUID.fromString("ac179280-c294-5812-b653-8bdc1dd9f24e"); 		// Natural.th.fundamental.arithmetic
		//UUID uuid = UUID.fromString("13401716-ea4c-542e-9951-e69241578080"); 		// Real.e.th.summation.factorial

		Statement statement = persistenceManager.getStatement(transaction, uuid);
		Term term = statement.proofTerm(transaction);
		if (term != null)
		{
			System.out.println("     : " + term.getType().toString(transaction, statement.getContext(transaction)));
			System.out.println("     : " + statement.getInnerTerm(transaction).toString(transaction, statement.getContext(transaction)));
			assert term.getType().equals(statement.getInnerTerm(transaction));

			PrintWriter printWriter = new PrintWriter("tmp/out.txt");
			term.print(printWriter, transaction, statement.getContext(transaction));
			printWriter.close();
		}
	}

}
