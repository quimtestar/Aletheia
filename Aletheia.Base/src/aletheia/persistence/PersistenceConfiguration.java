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
package aletheia.persistence;

import aletheia.persistence.PersistenceManager.StartupProgressListener;

public abstract class PersistenceConfiguration
{
	private final static StartupProgressListener defaultStartupProgressListener = StartupProgressListener.silent;
	private final static boolean defaultDebug = false;

	private StartupProgressListener startupProgressListener;
	private boolean debug;

	public PersistenceConfiguration()
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