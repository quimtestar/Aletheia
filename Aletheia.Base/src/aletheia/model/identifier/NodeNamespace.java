/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A node name space is every name space that is not the root name space. The
 * main properties of a node name space are a parent name space and his name (a
 * string).
 *
 */
public class NodeNamespace extends Namespace
{
	private static final long serialVersionUID = -4682835474244189181L;

	/**
	 * The regular expression that all name space names must match.
	 */
	private final static String nameRegex = "([_a-zA-Z][_a-zA-Z0-9]*)";

	/**
	 * Collection of reserved words that can't be a name.
	 */
	private final static Collection<String> reservedWordSet = Arrays.asList("Tau");

	public static boolean validName(String name)
	{
		return name.matches(nameRegex) && !reservedWordSet.contains(name);
	}

	private final Namespace parent;
	private final String name;

	public static class InvalidNameException extends Exception
	{
		private static final long serialVersionUID = 5229778346187529307L;

		private final String name;

		protected InvalidNameException(String name)
		{
			super("Invalid name:" + name);
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

	}

	/**
	 * @param parent
	 *            The parent name space of the new one.
	 * @param name
	 *            The name of the new name space. Must match {@link #nameRegex}.
	 * @throws InvalidNameException
	 */
	public NodeNamespace(Namespace parent, String name) throws InvalidNameException
	{
		super();
		this.parent = parent;
		this.name = name;
		validateName();
	}

	protected void validateName() throws InvalidNameException
	{
		if (!validName(name))
			throw new InvalidNameException(name);
	}

	@Override
	public boolean isRoot()
	{
		return false;
	}

	/**
	 * Creates a direct descendant of the root name space with the specified
	 * name.
	 *
	 * @param name
	 *            The name.
	 * @throws InvalidNameException
	 */
	public NodeNamespace(String name) throws InvalidNameException
	{
		this(RootNamespace.instance, name);
	}

	/**
	 * Builds a name space by concatenating two name spaces.
	 *
	 * @param parent
	 *            The left side.
	 * @param right
	 *            The right side.
	 * @throws InvalidNameException
	 */
	public NodeNamespace(Namespace parent, NodeNamespace right) throws InvalidNameException
	{
		this(parent.concat(right.parent), right.name);
	}

	/**
	 * Builds a copy of a name space.
	 *
	 * @param namespace
	 *            The name space to be copied.
	 * @throws InvalidNameException
	 */
	public NodeNamespace(NodeNamespace namespace) throws InvalidNameException
	{
		this(namespace.parent, namespace.name);
	}

	/**
	 * The parent name space of this name space.
	 *
	 * @return The parent.
	 */
	public Namespace getParent()
	{
		return parent;
	}

	/**
	 * The name of this name space.
	 *
	 * @return The name.
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public String qualifiedName()
	{
		if (parent instanceof RootNamespace)
			return name;
		else
			return parent.qualifiedName() + "." + name;
	}

	@Override
	public NodeNamespace concat(Namespace right)
	{
		if (right instanceof RootNamespace)
			return this;
		else if (right instanceof NodeNamespace)
			try
			{
				return new NodeNamespace(concat(((NodeNamespace) right).getParent()), ((NodeNamespace) right).getName());
			}
			catch (InvalidNameException e)
			{
				throw new Error(e);
			}
		else
			throw new Error();
	}

	/**
	 * The hash code depends exclusively on the hash codes of the name and the
	 * parent.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	/**
	 * Two node name spaces are equal if and only if their parents are equal and
	 * their names are also equal.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj instanceof NodeNamespace))
			return false;
		NodeNamespace other = (NodeNamespace) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (parent == null)
		{
			if (other.parent != null)
				return false;
		}
		else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public boolean isPrefixOf(Namespace namespace)
	{
		if (namespace instanceof RootNamespace)
			return false;
		else if (namespace instanceof NodeNamespace)
		{
			if (equals(namespace))
				return true;
			else
				return isPrefixOf(((NodeNamespace) namespace).getParent());
		}
		else
			throw new Error();

	}

	@Override
	public Namespace makeSuffix(Namespace prefix)
	{
		if (equals(prefix))
			return RootNamespace.instance;
		else
		{
			Namespace pSuf = parent.makeSuffix(prefix);
			if (pSuf == null)
				return null;
			try
			{
				return new NodeNamespace(pSuf, name);
			}
			catch (InvalidNameException e)
			{
				throw new Error(e);
			}
		}
	}

	@Override
	public List<NodeNamespace> prefixList()
	{
		List<NodeNamespace> list = parent.prefixList();
		list.add(this);
		return list;
	}

	@Override
	public int compareTo(Namespace namespace)
	{
		Iterator<String> i1 = nameList().iterator();
		Iterator<String> i2 = namespace.nameList().iterator();

		while (true)
		{
			if (!i1.hasNext())
			{
				if (!i2.hasNext())
					return 0;
				else
					return -1;
			}
			if (!i2.hasNext())
				return +1;
			String s1 = i1.next().replaceFirst("!$", "");
			String s2 = i2.next().replaceFirst("!$", "");
			int c = s1.compareTo(s2);
			if (c != 0)
				return c;
		}
	}

	public NodeNamespace min(NodeNamespace other)
	{
		return (NodeNamespace) super.min(other);
	}

	public NodeNamespace max(NodeNamespace other)
	{
		return (NodeNamespace) super.max(other);
	}

	public Identifier asIdentifier()
	{
		if (this instanceof Identifier)
			return (Identifier) this;
		else
			try
			{
				return new Identifier(this);
			}
			catch (InvalidNameException e)
			{
				throw new Error(e);
			}
	}

	public String headName()
	{
		if (parent instanceof RootNamespace)
			return name;
		else if (parent instanceof NodeNamespace)
			return ((NodeNamespace) parent).headName();
		else
			throw new Error();
	}

	@Override
	public int length()
	{
		return parent.length() + 1;
	}

}
