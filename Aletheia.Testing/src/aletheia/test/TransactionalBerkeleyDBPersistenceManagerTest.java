/*******************************************************************************
 * Copyright (c) 2018, 2019 Quim Testar
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
package aletheia.test;

import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;

public abstract class TransactionalBerkeleyDBPersistenceManagerTest extends BerkeleyDBPersistenceManagerTest
{
	public TransactionalBerkeleyDBPersistenceManagerTest()
	{
		super();
	}

	@Override
	protected final void run(BerkeleyDBPersistenceManager persistenceManager) throws Exception
	{
		try (Transaction transaction = persistenceManager.beginTransaction())
		{
			run(persistenceManager, transaction);
			if (!isReadOnly())
				transaction.commit();
		}
	}

	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
	}

}
