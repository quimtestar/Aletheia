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
 * This is the first identifier of its name space.
 */
public class NamespaceInitiator extends NamespaceExtreme
{
	private static final long serialVersionUID = -3742891511411197852L;

	/**
	 * The name of a initiator (the mark) will always be the empty string. The
	 * empty string is lexicographically smaller than any other valid name,
	 * making it consistent with the sorting rules specified in
	 * {@link aletheia.model.identifier}.
	 */
	public static final String mark = "!";

	protected NamespaceInitiator(Namespace namespace) throws InvalidNameException
	{
		super(namespace, mark);
	}

}
