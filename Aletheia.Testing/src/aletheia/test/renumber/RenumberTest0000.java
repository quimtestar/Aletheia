/*******************************************************************************
 * Copyright (c) 2019 Quim Testar.
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
 *******************************************************************************/
package aletheia.test.renumber;

import java.lang.reflect.Method;
import java.util.UUID;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.gui.cli.command.statement.Renumber;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

public class RenumberTest0000 extends TransactionalBerkeleyDBPersistenceManagerTest
{
	public RenumberTest0000()
	{
		super();
		setReadOnly(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		Context context = persistenceManager.getContext(transaction, UUID.fromString("8116ec9b-9075-4b0d-af44-71865829ec2e"));
		Renumber renumber = new Renumber(AletheiaCliConsole.cliConsole(persistenceManager), transaction, "s###", context);
		Method method = Renumber.class.getDeclaredMethod("runTransactional");
		method.setAccessible(true);
		method.invoke(renumber);
	}

}
