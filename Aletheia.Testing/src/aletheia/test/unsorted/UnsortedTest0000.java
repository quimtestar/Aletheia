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

import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CloseableSet;

public class UnsortedTest0000 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public UnsortedTest0000()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		Context context = persistenceManager.getContext(transaction, UUID.fromString("4504a57e-e713-4783-a2f7-66f64ef5028c"));
		CloseableMap<Term, CloseableSet<Statement>> localStatementsByTerm = context.localStatementsByTerm(transaction);
		Term term = context.parseTerm(transaction,
				"<@0:Set, Set.In @0 (Set.bCoproduct X Y), @1:Set, Set.In @1 (Set.bCoproduct X Y), Prop.And (Prop.And (Set.bCoproduct.element.Lft @1) (Set.bCoproduct.element.Rgt @0)) (Prop.And (Set.In (Set.op.rgt @0) A) (Set.Equal (f (Set.op.rgt @0)) (Set.op.rgt @1))), Set.bCoproduct.element.Lft @0 -> Prop.And (Set.bCoproduct.element.Lft @1) (Set.Equal (Set.op.rgt @0) (Set.op.rgt @1))>");
		CloseableSet<Statement> statements = localStatementsByTerm.get(term);
		for (Statement st : statements)
			System.out.println(st);
	}

}
