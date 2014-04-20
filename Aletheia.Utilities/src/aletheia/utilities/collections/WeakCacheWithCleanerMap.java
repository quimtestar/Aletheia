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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A cache map that uses {@linkplain WeakReference weak references}.
 * 
 * @param <K>
 *            The map keys' type.
 * @param <V>
 *            The map value's type.
 * 
 * @see WeakReference
 */
public class WeakCacheWithCleanerMap<K, V> extends AbstractCacheWithCleanerMap<K, V, WeakReference<V>>
{

	@Override
	protected WeakReference<V> makeRef(V value, ReferenceQueue<V> queue)
	{
		return new WeakReference<V>(value, queue);
	}

}
