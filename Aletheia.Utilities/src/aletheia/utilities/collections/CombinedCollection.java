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

import java.io.Serializable;
import java.util.Collection;

/**
 * A combined collection of any other two collections. This class is read-only,
 * to alter the collection we need to alter their front and back components.
 * 
 * @param <E>
 *            The elements' type.
 */
public class CombinedCollection<E> extends AbstractCombinedCollection<E> implements Serializable
{
	private static final long serialVersionUID = -3737647852678501284L;

	private final Collection<E> back;

	public CombinedCollection(Collection<E> front, Collection<E> back)
	{
		super(front);
		this.back = back;
	}

	@Override
	protected Collection<E> getBack()
	{
		return back;
	}

}
