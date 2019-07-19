/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import java.util.Set;

/**
 * A view as a {@link Set} of the difference (in the terms of set theory)
 * between two collections.
 *
 * @param <E>
 *            The element's type.
 *
 * @author Quim Testar
 */
public class DifferenceSet<E> extends DifferenceCollection<E> implements Set<E>
{

	public DifferenceSet(Set<E> minuend, Set<E> subtrahend)
	{
		super(minuend, subtrahend);
	}

	@Override
	public Set<E> getMinuend()
	{
		return (Set<E>) super.getMinuend();
	}

	@Override
	public Set<E> getSubtrahend()
	{
		return (Set<E>) super.getSubtrahend();
	}

}
