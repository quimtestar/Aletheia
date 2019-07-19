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
package aletheia.utilities.collections;

/**
 * A {@link CacheMap} with a "key cleaned" listen mechanism. When an entry of
 * the map is removed to save memory space, the method
 * {@link CacheWithCleanerMap.Listener#keyCleaned(Object)} of the listeners will
 * be called.
 *
 * @author Quim Testar
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
