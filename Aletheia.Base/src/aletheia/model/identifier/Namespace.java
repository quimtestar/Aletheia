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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.protocol.Exportable;

/**
 * <p>
 * The abstract representation of a generic name space; including the root name
 * space and all the identifiers.
 * </p>
 *
 * @see aletheia.model.identifier
 */
public abstract class Namespace implements Comparable<Namespace>, Serializable, Exportable
{
	private static final long serialVersionUID = 4577061568453288593L;

	public Namespace()
	{
		super();
	}

	public abstract boolean isRoot();

	/**
	 * Builds a full string representation of this name space, using dots to
	 * separate its components (if any).
	 *
	 * @return The string representation.
	 */
	public abstract String qualifiedName();

	/**
	 * Calls to {@link #qualifiedName()}
	 */
	@Override
	public String toString()
	{
		return qualifiedName();
	}

	/**
	 * Concatenates this name space with another one.
	 *
	 * @param right
	 *            The right side of the concatenation.
	 * @return The concatenated name space.
	 */
	public abstract Namespace concat(Namespace right);

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Decides if this name space is a prefix of another one.
	 *
	 * @param namespace
	 *            The name space to check if is prefixed by this.
	 * @return The decision.
	 */
	public abstract boolean isPrefixOf(Namespace namespace);

	/**
	 * Computes the list of prefixes of this component, starting with the root
	 * name space.
	 *
	 * @return The list.
	 */
	public abstract List<NodeNamespace> prefixList();

	/**
	 * Converts this name space into a list of string representations of its
	 * components.
	 *
	 * @return The list.
	 */
	public final List<String> nameList()
	{
		ArrayList<String> list = new ArrayList<>();
		for (NodeNamespace ns : prefixList())
			list.add(ns.getName());
		return list;
	}

	/**
	 * Compares two name spaces according to the order specified in
	 * {@link aletheia.model.identifier}
	 */
	@Override
	public abstract int compareTo(Namespace namespace);

	/**
	 * Parses a fully qualified name (all the components separated by dots) into
	 * a name space.
	 *
	 * @param fullName
	 *            The string to be parsed
	 * @return The parsed name space.
	 * @throws InvalidNameException
	 */
	public static Namespace parse(String fullName) throws InvalidNameException
	{
		if (fullName.equals(""))
			return RootNamespace.instance;
		Namespace namespace = RootNamespace.instance;
		for (String name : fullName.split("\\."))
		{
			if (namespace instanceof Identifier)
				throw new InvalidNameException(fullName);
			if (name.equals(NamespaceInitiator.mark))
				namespace = namespace.initiator();
			else if (name.equals(NamespaceTerminator.mark))
				namespace = namespace.terminator();
			else
				namespace = new NodeNamespace(namespace, name);
		}
		return namespace;
	}

	/**
	 * Returns the initiator of this name space. The initiator is a special kind
	 * of identifier that is by definition the first element in this name space
	 * in the natural name space order specified in
	 * {@link aletheia.model.identifier}.
	 *
	 * @return The name space initiator.
	 * @see #compareTo(Namespace)
	 */
	public NamespaceExtreme initiator()
	{
		try
		{
			return new NamespaceInitiator(this);
		}
		catch (InvalidNameException e)
		{
			throw new Error(e);
		}
	}

	/**
	 * Returns the terminator of this name space. The terminator is a special
	 * kind of identifier that is by definition the last element in this name
	 * space in the natural name space order specified in
	 * {@link aletheia.model.identifier}.
	 *
	 * @return The name space terminator.
	 * @see #compareTo(Namespace)
	 */
	public NamespaceExtreme terminator()
	{
		try
		{
			return new NamespaceTerminator(this);
		}
		catch (InvalidNameException e)
		{
			throw new Error(e);
		}
	}

	/**
	 * Returns which is the minimal name space between this one and the
	 * parameter according to the natural order. Null is allowed as parameter
	 * but is never returned as result.
	 *
	 * @param other
	 *            The other name space to consider.
	 * @return The minimal name space.
	 *
	 * @see #compareTo(Namespace)
	 */
	public Namespace min(Namespace other)
	{
		if (other == null || compareTo(other) <= 0)
			return this;
		else
			return other;
	}

	/**
	 * Returns which is the maximal name space between this one and the
	 * parameter according to the natural order. Null is allowed as parameter
	 * but is never returned as result.
	 *
	 * @param other
	 *            The other name space to consider.
	 * @return The maximal name space.
	 *
	 * @see #compareTo(Namespace)
	 */
	public Namespace max(Namespace other)
	{
		if (other == null || compareTo(other) >= 0)
			return this;
		else
			return other;
	}

	/**
	 * Takes a prefix out of this name space and returns the result. If the
	 * parameter is not a prefix of this name space, return null.
	 *
	 * @param prefix
	 *            The prefix to take out.
	 * @return The computed suffix.
	 */
	abstract public Namespace makeSuffix(Namespace prefix);

	public static Namespace identifierToPrefix(Identifier identifier)
	{
		return identifier != null ? identifier : RootNamespace.instance;
	}

	public Namespace commonPrefix(Namespace namespace)
	{
		Iterator<String> i1 = nameList().iterator();
		Iterator<String> i2 = namespace.nameList().iterator();
		Namespace prefix = RootNamespace.instance;
		while (i1.hasNext() && i2.hasNext())
		{
			String n1 = i1.next();
			String n2 = i2.next();
			if (!n1.equals(n2))
				break;
			try
			{
				prefix = new NodeNamespace(prefix, n1);
			}
			catch (InvalidNameException e)
			{
				throw new Error(e);
			}
		}
		return prefix;
	}
}
