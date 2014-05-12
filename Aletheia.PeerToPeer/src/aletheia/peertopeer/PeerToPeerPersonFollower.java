/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 * 
 * This file is part of the Aletheia Proof Assistant.
 * 
 * The Aletheia Proof Assistant is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * The Aletheia Proof Assistant is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer;

import aletheia.model.authority.Person;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.Aborter;

public class PeerToPeerPersonFollower extends PeerToPeerFollower
{
	public interface Listener extends PeerToPeerFollower.Listener
	{
		public void personAdded(Person person);

		public void personModified(Person person);

		public void personRemoved(Person person);
	}

	private class PersonListener implements Person.AddStateListener, Person.StateListener
	{
		@Override
		public void personAdded(Transaction transaction, final Person person)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					person.addStateListener(personListener);
					getListener().personAdded(person);
				}
			});
		}

		@Override
		public void personModified(Transaction transaction, final Person person)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					getListener().personModified(person);
				}
			});
		}

		@Override
		public void personRemoved(Transaction transaction, final Person person)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					person.removeStateListener(personListener);
					getListener().personRemoved(person);
				}
			});
		}

	}

	private final PersonListener personListener;

	public PeerToPeerPersonFollower(PersistenceManager persistenceManager, Listener listener)
	{
		super(persistenceManager, listener);
		this.personListener = new PersonListener();
	}

	@Override
	public Listener getListener()
	{
		return (Listener) super.getListener();
	}

	@Override
	public void follow(Aborter aborter)
	{
		getPersistenceManager().getListenerManager().getPersonAddStateListeners().add(personListener);
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			for (Person person : getPersistenceManager().persons(transaction).values())
			{
				person.addStateListener(personListener);
				getListener().personAdded(person);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public void unfollow()
	{
		getPersistenceManager().getListenerManager().getPersonAddStateListeners().remove(personListener);
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			for (Person person : getPersistenceManager().persons(transaction).values())
			{
				person.removeStateListener(personListener);
				getListener().personRemoved(person);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

}
