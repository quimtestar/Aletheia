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
package aletheia.utilities.collections;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

/**
 * A cache map is a map implemented where the values are kept in memory with
 * references (weak or soft). This interface defines the extra services a
 * generic cache map will have to offer to its callers, namely an observer
 * mechanism to inform the registered listeners when an entry's memory space has
 * been claimed (and so it has been cleaned) and a shutdown method to be called
 * when the map is not needed anymore (the thread managing the reference queues
 * must be shut down).
 * 
 * @param <K>
 *            The map keys' type.
 * @param <V>
 *            The map value's type.
 * 
 * @see Reference
 * @see ReferenceQueue
 */
public interface CacheWithCleanerMap<K, V> extends CacheMap<K, V>
{
	/**
	 * The listener interface
	 * 
	 * @param <K>
	 *            The map keys' type.
	 */
	public interface Listener<K>
	{
		/**
		 * A key has been cleaned in the observed map.
		 * 
		 * @param key
		 *            The cleaned key.
		 */
		void keyCleaned(K key);
	}

	/**
	 * Registers a listener to this map. While a listener is registered, it will
	 * receive notifications.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void addListener(Listener<K> listener);

	/**
	 * Unregisters a listener to this map. While a listener is registered, it
	 * will receive notifications.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void removeListener(Listener<K> listener);

}
