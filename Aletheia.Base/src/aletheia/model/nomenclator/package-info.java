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