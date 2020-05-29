package aletheia.persistence.berkeleydb;

import java.io.File;

public class BerkeleyDBConfiguration extends aletheia.persistence.Configuration
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

	public BerkeleyDBConfiguration()
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