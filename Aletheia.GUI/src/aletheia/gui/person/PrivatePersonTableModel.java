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

import java.util.Collections;

import aletheia.model.authority.Person;
import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableSortedMap;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;
import aletheia.utilities.collections.TrivialCloseableSet;

public class PrivatePersonTableModel extends AbstractPersonTableModel
{
	protected class PrivatePersonStateListener extends AbstractPersonTableModel.PersonStateListener
	{
		@Override
		public void personAdded(Transaction transaction, Person person)
		{
			if (person instanceof PrivatePerson)
				super.personAdded(transaction, person);
		}
	}

	public PrivatePersonTableModel(PersistenceManager persistenceManager)
	{
		super(persistenceManager);
	}

	@Override
	protected PrivatePersonStateListener makePersonStateListener()
	{
		return new PrivatePersonStateListener();
	}

	@Override
	protected CloseableSortedMap<String, CloseableSet<Person>> personsByNick(Transaction transaction)
	{
		return new BijectionCloseableSortedMap<>(new Bijection<PrivatePerson, CloseableSet<Person>>()
		{

			@Override
			public CloseableSet<Person> forward(PrivatePerson person)
			{
				return new TrivialCloseableSet<>(Collections.<Person> singleton(person));
			}

			@Override
			public PrivatePerson backward(CloseableSet<Person> output)
			{
				throw new UnsupportedOperationException();
			}
		}, getPersistenceManager().privatePersonsByNick(transaction));

	}

	@Override
	protected boolean isRowPrivate(int rowIndex)
	{
		return true;
	}

	@Override
	protected PrivatePerson getPerson(int row)
	{
		return (PrivatePerson) super.getPerson(row);
	}

}
