/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.util.Set;

/**
 * An alternative combination of multimaps (maps whose values are sets). Instead
 * of being shadowed, the colliding entries' values are combined into a
 * {@link CombinedSet}. This is a much more consistent behavior of a combined
 * multimap.
 *
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 *
 * @author Quim Testar
 */
public class CombinedMultimap<K, V> extends CombinedMap<K, Set<V>>
{
	private static final long serialVersionUID = -5650995995002745252L;

	public CombinedMultimap(Map<K, Set<V>> front, Map<K, Set<V>> back)
	{
		super(front, back);
	}

	@Override
	public Set<V> get(Object key)
	{
		Set<V> vFront = getFront().get(key);
		Set<V> vBack = getBack().get(key);
		if (vFront == null)
			return vBack;
		if (vBack == null)
			return vFront;
		return new CombinedSet<>(vFront, vBack);
	}

	@Override
	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

}
