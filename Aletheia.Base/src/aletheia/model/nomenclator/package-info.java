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
 * <b>Nomenclators</b> are data structures that map bi-directionally identifiers
 * to statements and are associated to contexts. A nomenclator does not only
 * gives us the correspondence for the statements local to the context but also
 * all the ancestors of the context; that is, it will map all the statements
 * accessible from a given context.
 * </p>
 * <p>
 * The identifier must be unique to all the local statements of a context, but a
 * statement may have the same identifier than another statement in a context
 * that is an strict ancestor of the former's context. In that case, the upper
 * statement gets <b>shadowed</b> by the lower one, and it won't be accessible
 * via its identifier with this particular nomenclator (but, of course, it will
 * be accessible with a nomenclator associated to a higher context in the
 * hierarchy).
 * </p>
 */
package aletheia.model.nomenclator;