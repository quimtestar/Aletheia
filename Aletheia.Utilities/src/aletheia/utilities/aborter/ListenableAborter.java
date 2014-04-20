/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.utilities.aborter;

import java.util.HashSet;
import java.util.Set;

public abstract class ListenableAborter implements Aborter
{
	public final static ListenableAborter nullListenableAborter = new ListenableAborter()
	{
		@Override
		public void checkAbort()
		{
		}
	};

	public interface Listener
	{
		void abort();
	}

	private final Set<Listener> listeners;

	public ListenableAborter()
	{
		this.listeners = new HashSet<Listener>();
	}

	public synchronized void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	protected synchronized void abort()
	{
		for (Listener listener : listeners)
			listener.abort();
	}

}
