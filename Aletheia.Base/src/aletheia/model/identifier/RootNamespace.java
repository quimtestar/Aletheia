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
package aletheia.model.identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * The root name space is the name space with zero components. This class have a
 * unique instance, following the singleton pattern.
 */
public class RootNamespace extends Namespace
{
	private static final long serialVersionUID = -6716406315002596409L;

	/**
	 * The unique instance of this class.
	 */
	public static final RootNamespace instance = new RootNamespace();

	private RootNamespace()
	{
		super();
	}

	@Override
	public boolean isRoot()
	{
		return true;
	}

	@Override
	public String qualifiedName()
	{
		return "";
	}

	@Override
	public Namespace concat(Namespace right)
	{
		return right;
	}

	/**
	 * The hash code of the root name space is hard coded to be always the same.
	 */
	@Override
	public int hashCode()
	{
		return 0xb98c65f9;
	}

	/**
	 * Any two root name spaces are one and the same.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RootNamespace))
			return false;
		return true;
	}

	@Override
	public boolean isPrefixOf(Namespace namespace)
	{
		return true;
	}

	@Override
	public List<NodeNamespace> prefixList()
	{
		return new ArrayList<NodeNamespace>();
	}

	@Override
	public int compareTo(Namespace namespace)
	{
		if (equals(namespace))
			return 0;
		else
			return -1;
	}

	@Override
	public Namespace makeSuffix(Namespace prefix)
	{
		if (prefix instanceof RootNamespace)
			return this;
		else
			return null;
	}

}
