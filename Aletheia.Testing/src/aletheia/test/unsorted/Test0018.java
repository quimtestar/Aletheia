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

import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.BerkeleyDBPersistenceManagerTest;

public class Test0018 extends BerkeleyDBPersistenceManagerTest
{

	public Test0018()
	{
		super();
		setReadOnly(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager) throws Exception
	{
		try (Transaction transaction = persistenceManager.beginDirtyTransaction())
		{
			for (Statement st : persistenceManager.statements(transaction).values())
			{
				try (Transaction transaction2 = persistenceManager.beginTransaction())
				{
					System.out.print(st.getUuid() + ": ");
					if (st instanceof Declaration)
					{
						Declaration dec = (Declaration) st;
						ParameterIdentification pi = dec.inferValueParameterIdentification(transaction2);
						System.out.println(dec.label() + ": " + pi + ": " + dec.getValue().toString(transaction2, dec.getContext(transaction2), pi));
						dec.updateValueParameterIdentification(transaction2, pi);
					}
					else
						System.out.println();
					st.checkTermParameterIdentification(transaction2);
					transaction2.commit();
				}
			}
		}
	}

}
