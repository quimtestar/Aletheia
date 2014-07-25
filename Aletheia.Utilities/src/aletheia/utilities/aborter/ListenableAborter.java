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

/**
 * Implementation of the {@link Aborter} with an interrupt mechanism.
 *
 * The process to be aborted should provide an implementation of the
 * {@link ListenableAborter.Listener} interface and add it to the listener set.
 * When aborting, the {@link ListenableAborter.Listener#abort()} method will be
 * called.
 *
 * @author Quim Testar
 */
public abstract class ListenableAborter implements Aborter
{
	public final static ListenableAborter nullListenableAborter = new ListenableAborter()
	{
		@Override
		public void checkAbort()
		{
		}
	};

	/**
	 * Listener object to this aborter.
	 */
	public interface Listener
	{
		/**
		 * The process must be aborted.
		 */
		void abort();
	}

	/**
	 * Listener set.
	 */
	private final Set<Listener> listeners;

	public ListenableAborter()
	{
		this.listeners = new HashSet<Listener>();
	}

	/**
	 * Add a listener.
	 */
	public synchronized void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 */
	public synchronized void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Invoke the {@link Listener#abort()} method on each listener.
	 */
	protected synchronized void abort()
	{
		for (Listener listener : listeners)
			listener.abort();
	}

}
