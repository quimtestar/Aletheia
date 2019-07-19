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

/**
 * An {@link AbstractCombinedIterable} for {@link CloseableIterable}s.
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedCloseableIterable<E> extends AbstractCombinedIterable<E> implements CloseableIterable<E>
{

	private static final long serialVersionUID = 4890877631059836451L;

	public AbstractCombinedCloseableIterable(CloseableIterable<E> front)
	{
		super(front);
	}

	@Override
	protected CloseableIterable<E> getFront()
	{
		return (CloseableIterable<E>) super.getFront();
	}

	@Override
	protected abstract CloseableIterable<E> getBack();

	@Override
	public CloseableIterator<E> iterator()
	{
		return new CombinedCloseableIterator<>(getFront().iterator(), getBack().iterator());
	}

}
