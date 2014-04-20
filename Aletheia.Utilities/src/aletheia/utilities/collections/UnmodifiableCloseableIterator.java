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

public class UnmodifiableCloseableIterator<E> implements CloseableIterator<E>
{
	private final CloseableIterator<E> inner;

	public UnmodifiableCloseableIterator(CloseableIterator<E> inner)
	{
		super();
		this.inner = inner;
	}

	protected CloseableIterator<E> getInner()
	{
		return inner;
	}

	@Override
	public void close()
	{
		inner.close();
	}

	@Override
	public boolean hasNext()
	{
		return inner.hasNext();
	}

	@Override
	public E next()
	{
		return inner.next();
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
