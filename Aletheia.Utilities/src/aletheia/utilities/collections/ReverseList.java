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

import java.util.AbstractList;
import java.util.List;

/**
 * A view of a {@link List} that reverts it.
 * 
 * @param <E>
 */
public class ReverseList<E> extends AbstractList<E>
{
	private final List<E> inner;

	/**
	 * Creates a new reverse list view.
	 * 
	 * @param inner
	 *            The list to be reverted.
	 */
	public ReverseList(List<E> inner)
	{
		super();
		this.inner = inner;
	}

	@Override
	public E get(int index)
	{
		return inner.get(inner.size() - index - 1);
	}

	@Override
	public int size()
	{
		return inner.size();
	}

}
