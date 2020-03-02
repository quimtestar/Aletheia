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
package aletheia.test.authority;

import aletheia.model.authority.Signatory;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class AuthorityTest0000 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public AuthorityTest0000()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		System.out.println(persistenceManager.privateSignatories(transaction).size());
		for (Signatory privateSignatory : persistenceManager.privateSignatories(transaction).values())
			System.out.println(privateSignatory);
	}

}
