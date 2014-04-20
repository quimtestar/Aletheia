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

import java.util.Set;

/**
 * A combined set any two other sets. The elements in the front set will shadow
 * the elements on the back set. This class is read-only, to alter the set we
 * need to alter their front and back components.
 * 
 * @param <E>
 *            The elements' type.
 */
public class CombinedSet<E> extends AbstractCombinedSet<E> implements Set<E>
{
	private static final long serialVersionUID = 2341397957245092788L;

	private final Set<E> back;

	public CombinedSet(Set<E> front, Set<E> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected Set<E> getBack()
	{
		return back;
	}

}
