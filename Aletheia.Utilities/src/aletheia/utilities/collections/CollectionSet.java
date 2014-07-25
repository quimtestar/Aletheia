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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Mimic the {@link Set} behaviour with a {@link Collection}. That is, basically
 * avoid duplicate elements when adding. This class just filters the new
 * additions, the inner {@link Collection} should not have duplicates by itself.
 *
 * @author Quim Testar
 */
public class CollectionSet<E> extends AbstractSet<E>
{
	private final Collection<E> inner;

	public CollectionSet(Collection<E> inner)
	{
		this.inner = inner;
	}

	public Collection<E> getInner()
	{
		return inner;
	}

	@Override
	public Iterator<E> iterator()
	{
		return inner.iterator();
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean add(E e)
	{
		if (inner.contains(e))
			return false;
		return inner.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		return inner.remove(o);
	}

}
