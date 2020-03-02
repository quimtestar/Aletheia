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
package aletheia.test.useless;

import java.util.UUID;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.gui.cli.command.statement.Useless;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;
import aletheia.utilities.MiscUtilities;

public class UselessTest0001 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public UselessTest0001()
	{
		super();
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		UUID uuid = UUID.fromString("4af25548-f878-59c9-9e62-01bb7a2355df");
		Context context = persistenceManager.getContext(transaction, uuid);
		int count = MiscUtilities.countIterable(context.descendentStatements(transaction));
		System.out.println("Count: " + count);
		Useless useless = new Useless(AletheiaCliConsole.cliConsole(persistenceManager), transaction, context, false);
		useless.run();
	}

}
