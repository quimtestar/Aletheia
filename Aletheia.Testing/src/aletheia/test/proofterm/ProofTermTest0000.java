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
package aletheia.test.proofterm;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;

import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class ProofTermTest0000 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ProofTermTest0000()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws FileNotFoundException
	{
		//UUID uuid = UUID.fromString("8bf0bf35-c253-5257-b3ef-49b59beabc77"); 		// Set.th.epsilon_induction
		//UUID uuid=UUID.fromString("e7a380b5-3b72-5341-b868-56226e163779");		// Prop.And.th.symm/s001
		//UUID uuid=UUID.fromString("360e41c6-2713-50ba-a22f-1f9d69dd1143");		// Set
		//UUID uuid = UUID.fromString("db8546ce-ab3b-5441-98ca-b2fed77e695d"); 		// Set.Equal.def
		//UUID uuid = UUID.fromString("9209090d-fc0c-5e24-b1e4-1d509857a328"); 		// Natural.two.th.sum.one
		//UUID uuid = UUID.fromString("ac179280-c294-5812-b653-8bdc1dd9f24e"); 		// Natural.th.fundamental.arithmetic
		//UUID uuid = UUID.fromString("13401716-ea4c-542e-9951-e69241578080"); 		// Real.e.th.summation.factorial
		//UUID uuid = UUID.fromString("6f7ec943-8027-5ed0-b727-ebea5ac7cb0d"); 		// Integer.gcd.th.product
		//UUID uuid = UUID.fromString("148f0fea-c933-5fad-8742-cb80d75cfaac"); 		// Prop.Iff.th.symm
		//UUID uuid = UUID.fromString("e32c201b-a0b8-490e-b232-a477f97ae139");		// Category.Arrow.Universal.Initial.th.Isomorphism
		UUID uuid = UUID.fromString("4af25548-f878-59c9-9e62-01bb7a2355df"); // Natural.set.th.Countable.finite_parts

		Statement statement = persistenceManager.getStatement(transaction, uuid);
		Term term = statement.proofTerm(transaction);
		if (term != null)
		{
			System.out.println("     : " + term.getType().toString(transaction, statement.getContext(transaction)));
			System.out.println("     : " + statement.getInnerTerm(transaction).toString(transaction, statement.getContext(transaction)));
			assert term.getType().equals(statement.getInnerTerm(transaction));
			assert statement.getContext(transaction).statements(transaction).keySet().containsAll(term.freeVariables());

			PrintWriter printWriter = new PrintWriter("tmp/term.txt");
			term.print(printWriter, transaction, statement.getContext(transaction));
			printWriter.close();
		}
	}

}
