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

import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class ParameterIdentificationTest0005 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ParameterIdentificationTest0005()
	{
		super();
		setReadOnly(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		for (Statement st : persistenceManager.statements(transaction).values())
		{
			if (st instanceof Assumption)
				((Assumption) st).updateTermParameterIdentification(transaction, null);
			else if (st instanceof Declaration)
				((Declaration) st).updateValueParameterIdentification(transaction, null);
			else if (st instanceof Specialization)
				((Specialization) st).updateInstanceParameterIdentification(transaction, null);
			else if (st instanceof Context)
				((Context) st).updateConsequentParameterIdentification(transaction, null);
		}
	}

}
