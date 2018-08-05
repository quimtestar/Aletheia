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
package aletheia.test;

import aletheia.gui.app.AletheiaCliConsole;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.PersistenceSecretKeyManager.PersistenceSecretKeyException;

public abstract class PersistenceManagerTest<P extends PersistenceManager> extends Test
{
	private final PersistenceManager.Configuration configuration;

	public PersistenceManagerTest(PersistenceManager.Configuration configuration)
	{
		this.configuration = configuration;
	}

	public PersistenceManager.Configuration getConfiguration()
	{
		return configuration;
	}

	@Override
	public final void run() throws Exception
	{
		try (P persistenceManager = createPersistenceManager())
		{
			run(persistenceManager);
		}
	}

	protected abstract P createPersistenceManager();

	protected abstract void run(P persistenceManager) throws Exception;

	protected void enterPassphrase(P persistenceManager) throws PersistenceSecretKeyException
	{
		char[] passphrase = AletheiaCliConsole.cliConsole(persistenceManager).passphrase(false);
		persistenceManager.getSecretKeyManager().enterPassphrase(passphrase);
	}

}
