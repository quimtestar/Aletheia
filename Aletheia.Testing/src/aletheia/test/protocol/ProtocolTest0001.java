/*******************************************************************************
 * Copyright (c) 2018, 2023 Quim Testar
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
package aletheia.test.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceConfiguration;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.protocol.ProtocolException;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;
import aletheia.utilities.collections.AdaptedCollection;

public class ProtocolTest0001 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public ProtocolTest0001()
	{
		super();
		setReadOnly(true);
		setDebug(true);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream(pos);
		Thread importer = new Thread("Importer")
		{

			@Override
			public void run()
			{
				BerkeleyDBPersistenceConfiguration configuration = new BerkeleyDBPersistenceConfiguration();
				configuration.setDbFile(new File("/mnt/vlb0/quim/Aletheia/aletheiadb_identified_parameters2_bis"));
				configuration.setReadOnly(false);
				try (PersistenceManager persistenceManager = new BerkeleyDBPersistenceManager(configuration))
				{
					persistenceManager.import_(new DataInputStream(pis));
				}
				catch (IOException | ProtocolException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
		importer.start();
		persistenceManager.export(new DataOutputStream(pos), transaction, new AdaptedCollection<>(persistenceManager.sortedRootContexts(transaction)), true,
				false);
		pos.close();
		importer.join();
		pis.close();
	}

}
