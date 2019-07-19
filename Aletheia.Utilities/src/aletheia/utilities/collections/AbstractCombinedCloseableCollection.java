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

/**
 * An {@link AbstractCombinedCollection} for {@link CloseableCollection}s.
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedCloseableCollection<E> extends AbstractCombinedCollection<E> implements CloseableCollection<E>
{
	private static final long serialVersionUID = -6251719920758330387L;

	public AbstractCombinedCloseableCollection(CloseableCollection<E> front)
	{
		super(front);
	}

	@Override
	protected CloseableCollection<E> getFront()
	{
		return (CloseableCollection<E>) super.getFront();
	}

	@Override
	protected abstract CloseableCollection<E> getBack();

	protected class CombinedCloseableIterator extends CombinedIterator implements CloseableIterator<E>
	{
		protected CombinedCloseableIterator(CloseableIterator<E> frontIterator, CloseableIterator<E> backIterator)
		{
			super(frontIterator, backIterator);
		}

		@Override
		protected CloseableIterator<E> getFrontIterator()
		{
			return (CloseableIterator<E>) super.getFrontIterator();
		}

		@Override
		protected CloseableIterator<E> getBackIterator()
		{
			return (CloseableIterator<E>) super.getBackIterator();
		}

		@Override
		public void close()
		{
			getFrontIterator().close();
			getBackIterator().close();
		}

	}

	@Override
	public CloseableIterator<E> iterator()
	{
		return new CombinedCloseableIterator(getFront().iterator(), getBack().iterator());
	}

}
