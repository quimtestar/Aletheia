/*******************************************************************************
 * Copyright (c) 2019 Quim Testar.
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
 *******************************************************************************/
/**
 * <p>
 * Classes that define the {@linkplain aletheia.model.identifier.Identifier
 * <b>identifier</b>} objects. Identifiers are hierarchical textual ordered
 * labels used by humans to refer to statements inside its context.
 * </p>
 * <p>
 * The general syntactic structure of an identifier is as follows:
 *
 * <pre>
 *      ([_a-zA-Z][_a-zA-Z0-9]*.)*[_a-zA-Z][_a-zA-Z0-9]*
 * </pre>
 *
 * Any prefix consisting on components delimited by dots is called a
 * {@linkplain aletheia.model.identifier.Namespace <b>name space</b>}, so an
 * identifier is also considered to be a name space, but a name space might not
 * be an identifier. The special name space that consists of no components is
 * called the {@linkplain aletheia.model.identifier.RootNamespace <b>root name
 * space</b>}. The root name space cannot be considered as an identifier, since
 * an identifier has at least one component. Any name space has two special
 * identifiers called its extremes (so special that they don't match the regular
 * expression presented before) represented textually by the empty string (the
 * name space initiator) and the string "~" (the name space terminator). These
 * two might not be used to identify any statement.
 * </p>
 * <p>
 * The order of name spaces (and, by extension, identifiers) is defined to be as
 * follows:
 * <ul>
 * <li>
 *
 * <pre>
 * The root name space <= any name space.
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * P1 <= P2 -> P1.L1 <= P2.L2, where P1, P2 are any prefix name space (might be the root name space) and L1 and L2 are any single label component (the parts between dots in the regular expression), or represent an extreme of the former name space.
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * L1 <= L2 -> P.L1 <= P.L2, where P is any prefix name space and L1 and L2 are any single label component, or represent a name space extreme.
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * I <= L, where I is the root name space initiator and L is any single label component.
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * L <= T, where T is the root name space terminator and L is any single label component.
 * </pre>
 *
 * </li>
 * <li>
 *
 * <pre>
 * Two single label components are ordered using the regular lexicographical order.
 * </pre>
 *
 * </li>
 * </ul>
 * </p>
 *
 */
package aletheia.model.identifier;