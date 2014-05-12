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
package aletheia.gui.person;

import aletheia.model.authority.Person;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;

public class PersonTableModel extends AbstractPersonTableModel
{

	protected class PersonStateListener extends AbstractPersonTableModel.PersonStateListener
	{
	}

	public PersonTableModel(PersistenceManager persistenceManager)
	{
		super(persistenceManager);
		getPersistenceManager().getListenerManager().getPersonAddStateListeners().add(getPersonStateListener());
	}

	@Override
	protected PersonStateListener makePersonStateListener()
	{
		return new PersonStateListener();
	}

	@Override
	protected CloseableSortedMap<String, CloseableSet<Person>> personsByNick(Transaction transaction)
	{
		return getPersistenceManager().personsByNick(transaction);
	}

	@Override
	public void shutdown()
	{
		super.shutdown();
		getPersistenceManager().getListenerManager().getPersonAddStateListeners().remove(getPersonStateListener());
	}

}
