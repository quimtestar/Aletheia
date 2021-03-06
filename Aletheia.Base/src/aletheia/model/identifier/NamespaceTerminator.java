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

/**
 * This is the last identifier of its name space.
 */
public class NamespaceTerminator extends NamespaceExtreme
{
	private static final long serialVersionUID = -7409248011701915817L;

	/**
	 * The name of a initiator (the mark) will always be the string "~". This
	 * string is lexicographically greater than any other valid name, making it
	 * consistent with the sorting rules specified in
	 * {@link aletheia.model.identifier}.
	 */
	public static final String mark = "~";

	protected NamespaceTerminator(Namespace namespace) throws InvalidNameException
	{
		super(namespace, mark);
	}

}
