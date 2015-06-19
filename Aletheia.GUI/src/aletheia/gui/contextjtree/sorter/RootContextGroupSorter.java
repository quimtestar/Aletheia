/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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
package aletheia.gui.contextjtree.sorter;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.persistence.collections.statement.SortedRootContexts;
import aletheia.utilities.MiscUtilities;

public class RootContextGroupSorter extends GroupSorter<RootContext>
{
	private final PersistenceManager persistenceManager;

	protected RootContextGroupSorter(RootContextGroupSorter group, Identifier prefix, PersistenceManager persistenceManager)
	{
		super(group, prefix);
		this.persistenceManager = persistenceManager;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public RootContextGroupSorter getGroup()
	{
		return (RootContextGroupSorter) super.getGroup();
	}

	@Override
	public RootContext getStatement(Transaction transaction)
	{
		if (getPrefix() == null)
			return null;
		GenericRootContextsMap rcMap = persistenceManager.identifierToRootContexts(transaction).get(getPrefix());
		if (rcMap == null)
			return null;
		if (rcMap.size() != 1)
			return null;
		return MiscUtilities.firstFromCloseableIterable(rcMap.values());
	}

	@Override
	public SortedRootContexts sortedStatements(Transaction transaction)
	{
		SortedRootContexts sortedStatements = persistenceManager.sortedRootContexts(transaction);
		if (getPrefix() != null)
			return sortedStatements.subSet(getPrefix(), getPrefix().terminator());
		else
			return sortedStatements;
	}

	@Override
	protected RootContextGroupSorter subGroupSorter(Identifier prefix)
	{
		return new RootContextGroupSorter(this, prefix, persistenceManager);
	}

}
