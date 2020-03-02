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
package aletheia.test.parameteridentification;

import java.util.UUID;

import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Declaration;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class ParameterIdentificationTest0002 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ParameterIdentificationTest0002()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		Declaration decAnd = persistenceManager.getDeclaration(transaction, UUID.fromString("9de69905-7e01-5827-bc6c-e7a6a576b58f"));
		ParameterIdentification parameterIdentification = decAnd.inferValueParameterIdentification(transaction);
		System.out.println(parameterIdentification);
	}

}
