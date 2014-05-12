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
package aletheia.model.nomenclator;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CombinedMap;
import aletheia.utilities.collections.CombinedSortedMap;

/**
 * <p>
 * Class representation of a nomenclator linked to a context.
 * </p>
 * <p>
 * A nomenclator is also associated to a persistence transaction that will be
 * used on every operation with this data structure. Note that the nomenclator
 * will only be usable while the transaction is alive.
 * </p>
 * <p>
 * This class is abstract, a subclass of this will be used depending on if the
 * context is the {@link RootContext} or not.
 * </p>
 * 
 * @see aletheia.model.nomenclator
 */
public class SubNomenclator extends Nomenclator implements Serializable, Exportable
{
	private static final long serialVersionUID = -2499321232847327463L;

	private final Context context;

	/**
	 * Creates a nomenclator for a given context with a given transaction.
	 * 
	 * @param transaction
	 *            The transaction.
	 * @param context
	 *            The context.
	 */
	public SubNomenclator(PersistenceManager persistenceManager, Transaction transaction, Context context)
	{
		super(persistenceManager, transaction, persistenceManager.contextLocalIdentifierToStatement(transaction, context), persistenceManager
				.contextLocalStatementToIdentifier(transaction, context));
		this.context = context;
	}

	/**
	 * @return The context associated to this nomenclator.
	 */
	public Context getContext()
	{
		return context;
	}

	@Override
	protected CloseableMap<IdentifiableVariableTerm, Statement> localStatements()
	{
		return context.localStatements(getTransaction());
	}

	@Override
	protected Map<IdentifiableVariableTerm, Statement> statements()
	{
		return context.statements(getTransaction());
	}

	/**
	 * Creates a nomenclator for the parent context, using the same transaction.
	 * 
	 * @return The parent nomenclator.
	 */
	protected Nomenclator getParent()
	{
		return getContext().getParentNomenclator(getTransaction());
	}

	@Override
	public SortedMap<Identifier, Statement> identifierToStatement()
	{
		return new CombinedSortedMap<Identifier, Statement>(getLocalIdentifierToStatement(), getParent().identifierToStatement());
	}

	@Override
	public Map<Statement, Identifier> statementToIdentifier()
	{
		return new CombinedMap<Statement, Identifier>(getLocalStatementToIdentifier(), getParent().statementToIdentifier());
	}

	@Override
	public void addListener(Listener listener)
	{
		context.addNomenclatorListener(listener);
	}

	@Override
	public void removeListener(Listener listener)
	{
		context.removeNomenclatorListener(listener);
	}

	@Override
	protected Iterable<Listener> listeners()
	{
		return getPersistenceManager().getListenerManager().getSubNomenclatorListeners().iterable(context.getUuid());
	}

}
