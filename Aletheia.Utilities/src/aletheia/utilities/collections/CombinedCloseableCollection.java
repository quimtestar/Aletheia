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
 * A {@link CombinedCollection} that is a {@link CloseableCollection}.
 *
 * @author Quim Testar
 */
public class CombinedCloseableCollection<E> extends AbstractCombinedCloseableCollection<E>
{
	private static final long serialVersionUID = 6874275533839125964L;

	private final CloseableCollection<E> back;

	public CombinedCloseableCollection(CloseableCollection<E> front, CloseableCollection<E> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected CloseableCollection<E> getBack()
	{
		return back;
	}

}
