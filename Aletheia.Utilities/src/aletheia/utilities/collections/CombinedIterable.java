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

/**
 * A combined {@link Iterable} consists of two {@link Iterable}s: the front
 * {@link Iterable} and the back {@link Iterable}. The resulting
 * {@link Iterable} has both the front's elements and the back's elements. When
 * iterating across the elements of this {@link Iterable} we first iterate
 * across the front {@link Iterable} and the across the back {@link Iterable}.
 *
 * @author Quim Testar
 */
public class CombinedIterable<E> extends AbstractCombinedIterable<E>
{
	private static final long serialVersionUID = 15322847256592071L;
	private final Iterable<E> back;

	public CombinedIterable(Iterable<E> front, Iterable<E> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected Iterable<E> getBack()
	{
		return back;
	}

}
