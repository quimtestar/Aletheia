/*******************************************************************************
 * Copyright (c) 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb;

import java.io.File;

import aletheia.persistence.PersistenceConfiguration;

public class BerkeleyDBPersistenceConfiguration extends PersistenceConfiguration
{
	private final static boolean defaultAllowCreate = false;
	private final static boolean defaultReadOnly = true;
	private final static boolean defaultAllowUpgrade = false;
	private final static int defaultCachePercent = 0;
	private final static boolean defaultSharedCache = false;

	private File dbFile;
	private boolean allowCreate;
	private boolean readOnly;
	private boolean allowUpgrade;
	private int cachePercent;
	private boolean sharedCache;

	public BerkeleyDBPersistenceConfiguration()
	{
		super();
		this.allowCreate = defaultAllowCreate;
		this.readOnly = defaultReadOnly;
		this.allowUpgrade = defaultAllowUpgrade;
		this.cachePercent = defaultCachePercent;
		this.sharedCache = defaultSharedCache;
	}

	public File getDbFile()
	{
		return dbFile;
	}

	public void setDbFile(File dbFile)
	{
		this.dbFile = dbFile;
	}

	public boolean isAllowCreate()
	{
		return allowCreate;
	}

	public void setAllowCreate(boolean allowCreate)
	{
		this.allowCreate = allowCreate;
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	public boolean isAllowUpgrade()
	{
		return allowUpgrade;
	}

	public void setAllowUpgrade(boolean allowUpgrade)
	{
		this.allowUpgrade = allowUpgrade;
	}

	public int getCachePercent()
	{
		return cachePercent;
	}

	public void setCachePercent(int cachePercent)
	{
		this.cachePercent = cachePercent;
	}

	public boolean isSharedCache()
	{
		return sharedCache;
	}

	public void setSharedCache(boolean sharedCache)
	{
		this.sharedCache = sharedCache;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[dbFile=" + dbFile + ", allowCreate=" + allowCreate + ", readOnly=" + readOnly + ", allowUpgrade=" + allowUpgrade
				+ ", cachePercent=" + cachePercent + ", sharedCache=" + sharedCache + "]";
	}

}