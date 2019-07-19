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
 * A catalog is a data structures associated to the context that serves as a
 * hierarchical view of the set of all the identified statements accessible from
 * that context from the side of the identifier structure. In other words, it is
 * a tree where the nodes are {@linkplain aletheia.model.identifier.Namespace
 * name spaces} and leafs are {@linkplain aletheia.model.identifier.Identifier
 * identifiers}. The root node of the tree, of course, will be the
 * {@linkplain aletheia.model.identifier.RootNamespace root name space}.
 * </p>
 * <p>
 * From the outside of this package, the only catalog that can be directly
 * created is the {@linkplain aletheia.model.catalog.RootCatalog root catalog},
 * which is the root of the catalog tree and the catalog linked to the empty
 * name space (in other words, the
 * {@linkplain aletheia.model.identifier.RootNamespace root name space}. Other
 * catalogs will be accessed via successive (and recursive) calls to the method
 * {@link aletheia.model.catalog.Catalog#children()}.
 * </p>
 *
 * @see aletheia.model.identifier
 */
package aletheia.model.catalog;