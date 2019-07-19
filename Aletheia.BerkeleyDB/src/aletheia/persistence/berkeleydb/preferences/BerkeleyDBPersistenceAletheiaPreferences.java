/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.persistence.berkeleydb.preferences;

import java.io.File;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.preferences.PersistenceAletheiaPreferences;
import aletheia.preferences.AletheiaPreferences;

public class BerkeleyDBPersistenceAletheiaPreferences extends PersistenceAletheiaPreferences
{
	private static final String NODE_PATH = "BerkeleyDB";

	private final static String DB_FILE_NAME = "db_file_name";
	private final static String READONLY = "readonly";
	private final static String CACHE_PERCENT = "cache_percent";

	private final static boolean defaultReadOnly = false;
	private final static int defaultCachePercent = 25;

	public BerkeleyDBPersistenceAletheiaPreferences(AletheiaPreferences parent)
	{
		super(parent, NODE_PATH);
	}

	@Override
	public boolean configurationMatches(PersistenceManager persistenceManager)
	{
		if (persistenceManager == null)
		{
			if (getDbFile() != null)
				return false;
			return true;
		}
		else
		{
			if (!(persistenceManager instanceof BerkeleyDBPersistenceManager))
				return false;
			BerkeleyDBPersistenceManager berkeleyDBPersistenceManager = (BerkeleyDBPersistenceManager) persistenceManager;
			if (getDbFile() == null)
				return false;
			if (!getDbFile().equals(berkeleyDBPersistenceManager.getDbFile()))
				return false;
			if (isReadOnly() != berkeleyDBPersistenceManager.isReadOnly())
				return false;
			if (getCachePercent() != berkeleyDBPersistenceManager.getCachePercent())
				return false;
			return true;
		}
	}

	public File getDbFile()
	{
		String dbFileName = getPreferences().get(DB_FILE_NAME, null);
		if (dbFileName == null)
			return null;
		return new File(dbFileName);
	}

	public void setDbFile(File dbFile)
	{
		if (dbFile != null)
			getPreferences().put(DB_FILE_NAME, dbFile.getAbsolutePath());
		else
			getPreferences().remove(DB_FILE_NAME);
	}

	public boolean isReadOnly()
	{
		return getPreferences().getBoolean(READONLY, defaultReadOnly);
	}

	public void setReadOnly(boolean readOnly)
	{
		getPreferences().putBoolean(READONLY, readOnly);
	}

	public int getCachePercent()
	{
		return getPreferences().getInt(CACHE_PERCENT, defaultCachePercent);
	}

	public void setCachePercent(int cachePercent)
	{
		getPreferences().putInt(CACHE_PERCENT, cachePercent);
	}
}
