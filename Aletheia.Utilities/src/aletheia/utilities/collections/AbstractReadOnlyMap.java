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

import java.util.Map;

/**
 * Abstract implementation of a {@link Map} that keeps every read-only method
 * unimplemented and throws an {@link UnsupportedOperationException} on every
 * altering method.
 * 
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 */
public abstract class AbstractReadOnlyMap<K, V> implements Map<K, V>
{

	@Override
	public final void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final V put(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final V remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

}
