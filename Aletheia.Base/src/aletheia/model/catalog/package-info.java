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