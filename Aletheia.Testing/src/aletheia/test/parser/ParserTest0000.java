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
package aletheia.test.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class ParserTest0000 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ParserTest0000()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws ParserBaseException
	{
		Context context = persistenceManager.getContext(transaction, UUID.fromString("43563284-7976-5d48-b212-b27b556c5a30"));
		Map<ParameterVariableTerm, Identifier> parameterIdentifiers = new HashMap<>();
		Term term = context.parseTerm(transaction, "&(c33c255a-b0cd-5504-89ba-5ed95ec870ba/P)", parameterIdentifiers);
		System.out.println(parameterIdentifiers);
		System.out.println(context.unparseTerm(transaction, term));
	}

}
