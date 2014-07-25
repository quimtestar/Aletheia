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
package aletheia.utilities;

import java.util.Collection;
import java.util.Collections;

/**
 * A generic synchronization capsule embedding a single variable.
 */
public class SynchronizedFlag<V>
{
	private V value;

	/**
	 * @param value
	 *            The initial value.
	 */
	public SynchronizedFlag(V value)
	{
		super();
		this.value = value;
	}

	/**
	 * @return The value this flag is set to.
	 */
	public synchronized V getValue()
	{
		return value;
	}

	/**
	 * @param value
	 *            The value to set this flag to.
	 */
	public synchronized void setValue(V value)
	{
		this.value = value;
		notifyAll();
	}

	/**
	 * Waits until this flag is set to any value of a given collection.
	 */
	public synchronized void waitForValue(Collection<V> values) throws InterruptedException
	{
		while (!values.contains(value))
			wait();
	}

	/**
	 * Waits until this flag is set to any value of a given collection or a
	 * timeout is expired.
	 */
	public synchronized void waitForValue(Collection<V> values, long timeout) throws InterruptedException
	{
		if (timeout <= 0)
			waitForValue(values);
		else
		{
			long t0 = System.currentTimeMillis();
			long t1 = t0;
			while (!values.contains(value) && (t1 - t0 < timeout))
			{
				wait(timeout - (t1 - t0));
				t1 = System.currentTimeMillis();
			}
		}

	}

	/**
	 * Waits until this flag is set to a given value.
	 */
	public synchronized void waitForValue(V value) throws InterruptedException
	{
		waitForValue(Collections.singleton(value));
	}

	/**
	 * Waits until this flag is set to a given value or a timeout is expired.
	 */
	public synchronized void waitForValue(V value, long timeout) throws InterruptedException
	{
		waitForValue(Collections.singleton(value), timeout);
	}

	@Override
	public String toString()
	{
		return "SynchronizedFlag [value=" + value + "]";
	}

}
