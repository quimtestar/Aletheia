package aletheia.persistence;

import aletheia.persistence.PersistenceManager.StartupProgressListener;

public abstract class Configuration
{
	private final static StartupProgressListener defaultStartupProgressListener = StartupProgressListener.silent;
	private final static boolean defaultDebug = false;

	private StartupProgressListener startupProgressListener;
	private boolean debug;

	public Configuration()
	{
		super();
		this.startupProgressListener = defaultStartupProgressListener;
		this.debug = defaultDebug;
	}

	public StartupProgressListener getStartupProgressListener()
	{
		return startupProgressListener;
	}

	public void setStartupProgressListener(StartupProgressListener startupProgressListener)
	{
		this.startupProgressListener = startupProgressListener;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	@Override
	public String toString()
	{
		return "Configuration [startupProgressListener=" + startupProgressListener + ", debug=" + debug + "]";
	}

}