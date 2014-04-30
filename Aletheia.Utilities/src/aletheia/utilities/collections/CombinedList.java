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

import java.util.List;

/**
 * A combined list of any two other lists. This class is read-only, to alter the
 * list we need to alter their front and back components.
 * 
 * @param <E>
 *            The elements' type.
 * 
 * @author Quim Testar
 */
public class CombinedList<E> extends AbstractCombinedList<E> implements List<E>
{
	private static final long serialVersionUID = 5337265736945576134L;

	private final List<E> back;

	public CombinedList(List<E> front, List<E> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected List<E> getBack()
	{
		return back;
	}

}
