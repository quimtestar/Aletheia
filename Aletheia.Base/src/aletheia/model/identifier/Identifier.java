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

import aletheia.protocol.Exportable;

/**
 * An identifier is nothing more than a {@linkplain NodeNamespace node name
 * space}.
 */
public class Identifier extends NodeNamespace implements Exportable
{
	private static final long serialVersionUID = -4726934472827635678L;

	/**
	 * @param namespace
	 *            The parent name space of this identifier.
	 * @param name
	 *            The name of this identifier. Must match {@link #nameRegex}.
	 * @throws InvalidNameException
	 */
	public Identifier(Namespace namespace, String name) throws InvalidNameException
	{
		super(namespace, name);
	}

	/**
	 * Creates new identifier as a direct descendant of the root name space with the
	 * specified name.
	 *
	 * @param name
	 *            The name.
	 * @throws InvalidNameException
	 */
	public Identifier(String name) throws InvalidNameException
	{
		super(name);
	}

	/**
	 * Builds an identifier by concatenating two name spaces.
	 *
	 * @param parent
	 *            The left side.
	 * @param right
	 *            The right side.
	 * @throws InvalidNameException
	 */
	public Identifier(Namespace parent, NodeNamespace right) throws InvalidNameException
	{
		super(parent, right);
	}

	/**
	 * Builds a identifier as a copy of a name space.
	 *
	 * @param namespace
	 *            The name space to be copied.
	 * @throws InvalidNameException
	 */
	public Identifier(NodeNamespace namespace) throws InvalidNameException
	{
		super(namespace);
	}

	/**
	 * Calls to {@link #getParent()}.
	 *
	 * @return The name space of this identifier.
	 */
	public Namespace getNamespace()
	{
		return getParent();
	}

	public boolean hasRootNamespace()
	{
		return getNamespace().isRoot();
	}

	/**
	 * Parse a string representing a fully qualified identifier into the identifier
	 * it represents.
	 *
	 * @param fullName
	 *            The string to parse.
	 * @return The identifier.
	 * @throws InvalidNameException
	 */
	public static Identifier parse(String fullName) throws InvalidNameException
	{
		int idx = fullName.lastIndexOf('.');
		Namespace namespace = idx < 0 ? RootNamespace.instance : Namespace.parse(fullName.substring(0, idx));
		if (namespace instanceof Identifier)
			throw new InvalidNameException(fullName);
		String name = fullName.substring(idx + 1);
		if (name.equals(NamespaceInitiator.mark))
			return namespace.initiator();
		else if (name.equals(NamespaceTerminator.mark))
			return namespace.terminator();
		else
			return new Identifier(namespace, name);
	}

}
