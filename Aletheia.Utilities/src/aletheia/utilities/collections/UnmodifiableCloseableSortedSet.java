/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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

import java.util.Comparator;

/**
 * A read-only view of a {@link CloseableSortedSet}.
 *
 * @author Quim Testar
 */
public class UnmodifiableCloseableSortedSet<E> extends UnmodifiableCloseableSet<E> implements CloseableSortedSet<E>
{

	public UnmodifiableCloseableSortedSet(CloseableSortedSet<E> inner)
	{
		super(inner);
	}

	@Override
	protected CloseableSortedSet<E> getInner()
	{
		return (CloseableSortedSet<E>) super.getInner();
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return getInner().comparator();
	}

	@Override
	public E first()
	{
		return getInner().first();
	}

	@Override
	public E last()
	{
		return getInner().last();
	}

	@Override
	public UnmodifiableCloseableSortedSet<E> subSet(E fromElement, E toElement)
	{
		return new UnmodifiableCloseableSortedSet<>(getInner().subSet(fromElement, toElement));
	}

	@Override
	public UnmodifiableCloseableSortedSet<E> headSet(E toElement)
	{
		return new UnmodifiableCloseableSortedSet<>(getInner().headSet(toElement));
	}

	@Override
	public UnmodifiableCloseableSortedSet<E> tailSet(E fromElement)
	{
		return new UnmodifiableCloseableSortedSet<>(getInner().tailSet(fromElement));
	}

}
