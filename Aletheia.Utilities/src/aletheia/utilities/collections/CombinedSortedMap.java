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

import java.util.SortedMap;

/**
 * A combined sorted map of any two other sorted maps. The entries in the front
 * map will shadow the entries in the back map with the same key.
 *
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 *
 * @author Quim Testar
 */
public class CombinedSortedMap<K, V> extends AbstractCombinedSortedMap<K, V>implements SortedMap<K, V>
{
	private static final long serialVersionUID = 7809913304487413172L;

	private final SortedMap<K, V> back;

	public CombinedSortedMap(SortedMap<K, V> front, SortedMap<K, V> back)
	{
		super(front);
		this.back = back;
		if (((front.comparator() == null) != (back.comparator() == null)) || (front.comparator() != null && !front.comparator().equals(back.comparator())))
			throw new RuntimeException("Comparators differ");
	}

	@Override
	protected SortedMap<K, V> getFront()
	{
		return super.getFront();
	}

	@Override
	protected SortedMap<K, V> getBack()
	{
		return back;
	}

}
