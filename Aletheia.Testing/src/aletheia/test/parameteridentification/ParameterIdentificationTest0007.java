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
package aletheia.test.parameteridentification;

import aletheia.model.parameteridentification.CompositionParameterIdentification;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class ParameterIdentificationTest0007 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ParameterIdentificationTest0007()
	{
		super();
	}

	private boolean checkParameterIdentification(ParameterIdentification parameterIdentification)
	{
		if (parameterIdentification != null)
		{
			ParameterIdentification other;
			if (parameterIdentification instanceof FunctionParameterIdentification)
			{
				FunctionParameterIdentification functionParameterIdentification = (FunctionParameterIdentification) parameterIdentification;
				checkParameterIdentification(functionParameterIdentification.getDomain());
				checkParameterIdentification(functionParameterIdentification.getBody());
				other = FunctionParameterIdentification.make(functionParameterIdentification.getParameter(), functionParameterIdentification.getDomain(),
						functionParameterIdentification.getBody());
			}
			else if (parameterIdentification instanceof CompositionParameterIdentification)
			{
				CompositionParameterIdentification compositionParameterIdentification = (CompositionParameterIdentification) parameterIdentification;
				checkParameterIdentification(compositionParameterIdentification.getHead());
				checkParameterIdentification(compositionParameterIdentification.getTail());
				other = CompositionParameterIdentification.make(compositionParameterIdentification.getHead(), compositionParameterIdentification.getTail());
			}
			else
				throw new Error();
			if (!parameterIdentification.equals(other))
			{
				System.out.println(" ========> " + parameterIdentification + " -> " + other);
				return false;
			}
		}
		return true;
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		for (Statement statement : persistenceManager.statements(transaction).values())
		{
			System.out.println(statement.getUuid());
			boolean checked = true;
			checked = checked && checkParameterIdentification(statement.getTermParameterIdentification());
			if (statement instanceof Declaration)
				checked = checked && checkParameterIdentification(((Declaration) statement).getValueParameterIdentification());
			else if (statement instanceof Specialization)
				checked = checked && checkParameterIdentification(((Specialization) statement).getInstanceParameterIdentification());
			else if (statement instanceof Context)
				checked = checked && checkParameterIdentification(((Context) statement).getConsequentParameterIdentification());
			if (!checked)
				System.out.println("  -> " + statement);
		}
	}

}
