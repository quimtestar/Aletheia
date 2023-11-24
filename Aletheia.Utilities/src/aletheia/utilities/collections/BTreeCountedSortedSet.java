/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import aletheia.utilities.MiscUtilities;

/**
 * Implementation of a {@link CountedSortedSet} using a
 * {@link BTreeCountedSortedMap}.
 *
 * @author Quim Testar
 */
public class BTreeCountedSortedSet<E> extends DummyMapCountedSortedSet<E>
{

	public BTreeCountedSortedSet(int order, Comparator<? super E> comparator, Collection<? extends E> init)
	{
		super(new BTreeCountedSortedMap<>(order, comparator));
		addAll(init);
	}

	public BTreeCountedSortedSet(Comparator<? super E> comparator, Collection<? extends E> init)
	{
		super(new BTreeCountedSortedMap<>(comparator));
		addAll(init);
	}

	public BTreeCountedSortedSet(int order, Collection<? extends E> init)
	{
		super(new BTreeCountedSortedMap<>(order));
		addAll(init);
	}

	public BTreeCountedSortedSet(Collection<? extends E> init)
	{
		super(new BTreeCountedSortedMap<>());
		addAll(init);
	}

	public BTreeCountedSortedSet(int order, SortedSet<E> initSet)
	{
		super(new BTreeCountedSortedMap<>(order, new SetToDummyMap<>(initSet)));
	}

	public BTreeCountedSortedSet(SortedSet<E> initSet)
	{
		super(new BTreeCountedSortedMap<>(new SetToDummyMap<>(initSet)));
	}

	public BTreeCountedSortedSet(int order, Comparator<? super E> comparator)
	{
		super(new BTreeCountedSortedMap<>(order, comparator));
	}

	public BTreeCountedSortedSet(Comparator<? super E> comparator)
	{
		super(new BTreeCountedSortedMap<>(comparator));
	}

	public BTreeCountedSortedSet(int order)
	{
		super(new BTreeCountedSortedMap<>(order));
	}

	public BTreeCountedSortedSet()
	{
		super(new BTreeCountedSortedMap<>());
	}

	@Override
	protected BTreeCountedSortedMap<E, Dummy> getMap()
	{
		return (BTreeCountedSortedMap<E, Dummy>) super.getMap();
	}

	@Override
	public String toString()
	{
		return MiscUtilities.toString(this);
	}

	@Deprecated
	protected void trace(PrintStream out)
	{
		getMap().trace(out);
	}

}
