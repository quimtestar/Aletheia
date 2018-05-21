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
package aletheia.test.unsorted.parse_on_the_fly;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.term.TermParser;
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
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws FileNotFoundException, AletheiaParserException
	{
		Context context = persistenceManager.getContext(transaction, UUID.fromString("42cc8199-8159-5567-b65c-db023f95eaa3"));
		Term term = TermParser.parseTerm(context, transaction, new FileReader("tmp/term.txt"));
		try (PrintWriter pw = new PrintWriter(System.out))
		{
			term.print(pw, transaction, context);
		}
	}

}
