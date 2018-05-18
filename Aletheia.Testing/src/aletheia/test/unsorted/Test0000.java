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

import java.io.File;
import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaParserException;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.BerkeleyDBPersistenceManagerTest;

public class Test0000 extends BerkeleyDBPersistenceManagerTest
{

	public Test0000(File dbFile)
	{
		super(dbFile);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager) throws AletheiaParserException
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			Context context = persistenceManager.getContext(transaction, UUID.fromString("42cc8199-8159-5567-b65c-db023f95eaa3"));
			Term term = context.parseTerm(transaction, "<x:Set, y:Set, Set.Equal x y -> Set.Equal y x>");
			System.out.println(context.unparseTerm(transaction, term));
		}
		finally
		{
			transaction.abort();
		}
	}

}
