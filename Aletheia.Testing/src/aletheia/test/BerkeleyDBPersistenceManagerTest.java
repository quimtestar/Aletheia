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
package aletheia.test;

import java.io.File;

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceConfiguration;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;

public abstract class BerkeleyDBPersistenceManagerTest extends PersistenceManagerTest<BerkeleyDBPersistenceManager>
{
	public BerkeleyDBPersistenceManagerTest()
	{
		super(new BerkeleyDBPersistenceConfiguration());
		File dbFile = TestingAletheiaPreferences.instance.getDbFile();
		if (dbFile == null)
			throw new RuntimeException("No db file configured");
		getConfiguration().setDbFile(dbFile);
		getConfiguration().setCachePercent(TestingAletheiaPreferences.instance.getCachePercent());
	}

	@Override
	public BerkeleyDBPersistenceConfiguration getConfiguration()
	{
		return (BerkeleyDBPersistenceConfiguration) super.getConfiguration();
	}

	public boolean isReadOnly()
	{
		return getConfiguration().isReadOnly();
	}

	public void setReadOnly(boolean readOnly)
	{
		getConfiguration().setReadOnly(readOnly);
	}

	@Override
	protected BerkeleyDBPersistenceManager createPersistenceManager()
	{
		return new BerkeleyDBPersistenceManager(getConfiguration());
	}

}
