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

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.TrivialCloseableMap;

/**
 * The kind of nomenclator for the root context.
 * 
 * @see RootContext
 */
public class RootNomenclator extends Nomenclator
{
	private static final long serialVersionUID = -6700430493939998152L;

	private final RootContext rootContext;

	public RootNomenclator(PersistenceManager persistenceManager, Transaction transaction, RootContext rootContext)
	{
		super(persistenceManager, transaction, new RootLocalIdentifierToStatement(persistenceManager, transaction, rootContext),
				new RootLocalStatementToIdentifier(persistenceManager, transaction, rootContext));
		this.rootContext = rootContext;
	}

	@Override
	protected CloseableMap<IdentifiableVariableTerm, Statement> localStatements()
	{
		return new TrivialCloseableMap<>(statements());
	}

	@Override
	protected Map<IdentifiableVariableTerm, Statement> statements()
	{
		return Collections.<IdentifiableVariableTerm, Statement> singletonMap(rootContext.getVariable(), rootContext);
	}

	@Override
	public SortedMap<Identifier, Statement> identifierToStatement()
	{
		return localIdentifierToStatement();
	}

	@Override
	public Map<Statement, Identifier> statementToIdentifier()
	{
		return localStatementToIdentifier();
	}

	@Override
	public void addListener(Listener listener)
	{
		rootContext.addRootNomenclatorListener(listener);
	}

	@Override
	public void removeListener(Listener listener)
	{
		rootContext.removeRootNomenclatorListener(listener);
	}

	@Override
	protected Iterable<Listener> listeners()
	{
		return getPersistenceManager().getListenerManager().getRootNomenclatorListeners().iterable(rootContext.getUuid());
	}

}
