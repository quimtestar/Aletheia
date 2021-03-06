/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.model.identifier;

import java.util.SortedMap;
import java.util.SortedSet;

/**
 * A abstract class for name space extremes. These are special identifiers with
 * the property of being the first and last of their name space. Although being
 * instances of the class {@link Identifier}, they shouldn't be used to identify
 * any statement; but to use as bounds for manipulating sorted data structures
 * (i.e. {@link SortedSet}, {@link SortedMap}).
 */
public abstract class NamespaceExtreme extends Identifier
{
	private static final long serialVersionUID = 7363642852379005652L;

	/**
	 * @param namespace
	 *            The name space this extreme belongs to.
	 * @param mark
	 *            A special string serving as a name for extremes. It will
	 *            depend on the actual extreme we are creating. See the
	 *            sub-classes
	 * @throws InvalidNameException
	 */
	protected NamespaceExtreme(Namespace namespace, String mark) throws InvalidNameException
	{
		super(namespace, mark);
	}

	@Override
	public NamespaceExtreme initiator()
	{
		return this;
	}

	@Override
	public NamespaceExtreme terminator()
	{
		return this;
	}

}
