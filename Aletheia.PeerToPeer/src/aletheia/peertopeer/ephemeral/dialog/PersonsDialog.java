/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
package aletheia.peertopeer.ephemeral.dialog;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.PersonInfoMessage;
import aletheia.peertopeer.ephemeral.message.PersonInfoMessage.PersonInfo;
import aletheia.peertopeer.statement.message.PersonRequestMessage;
import aletheia.peertopeer.statement.message.PersonResponseMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;

public abstract class PersonsDialog extends EphemeralDialog
{

	public PersonsDialog(Phase phase)
	{
		super(phase);
	}

	protected Collection<PersonInfoMessage.Entry> dialogatePersonInfoSend(Collection<Person> persons) throws IOException, InterruptedException
	{
		Collection<PersonInfoMessage.Entry> entries = new BijectionCollection<>(new Bijection<Person, PersonInfoMessage.Entry>()
		{

			@Override
			public PersonInfoMessage.Entry forward(Person person)
			{
				return new PersonInfoMessage.Entry(person);
			}

			@Override
			public Person backward(PersonInfoMessage.Entry output)
			{
				throw new UnsupportedOperationException();
			}
		}, new FilteredCollection<>(new Filter<Person>()
		{

			@Override
			public boolean filter(Person person)
			{
				return person.isSigned();
			}
		}, persons));

		sendMessage(new PersonInfoMessage(entries));
		return entries;
	}

	protected PersonInfoMessage dialogatePersonInfoRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonInfoMessage.class);
	}

	protected Collection<UUID> dialogatePersonRequestSend(PersonInfoMessage personsInfoMessage) throws IOException, InterruptedException
	{
		Collection<UUID> requestUuids = new BijectionCollection<>(new Bijection<Map.Entry<UUID, PersonInfo>, UUID>()
		{

			@Override
			public UUID forward(Map.Entry<UUID, PersonInfo> input)
			{
				return input.getKey();
			}

			@Override
			public Map.Entry<UUID, PersonInfo> backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		}, new FilteredCollection<>(new Filter<Map.Entry<UUID, PersonInfo>>()
		{
			@Override
			public boolean filter(Map.Entry<UUID, PersonInfo> e)
			{
				UUID uuid = e.getKey();
				Person person = getPersistenceManager().getPerson(getTransaction(), uuid);
				if ((person == null) || !person.isSigned())
					return true;
				PersonInfo info = e.getValue();
				if (person.getSignatureDate().compareTo(info.getSignatureDate()) < 0)
					return true;
				return false;
			}
		}, personsInfoMessage.getMap().entrySet()));
		sendMessage(new PersonRequestMessage(requestUuids));
		return requestUuids;
	}

	protected PersonRequestMessage dialogatePersonRequestRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonRequestMessage.class);
	}

	protected Collection<Person> dialogatePersonResponseSend(PersonRequestMessage personRequestMessage) throws IOException, InterruptedException
	{
		Collection<Person> persons = new FilteredCollection<>(new NotNullFilter<Person>(), new BijectionCollection<>(new Bijection<UUID, Person>()
		{

			@Override
			public Person forward(UUID uuid)
			{
				return getPersistenceManager().getPerson(getTransaction(), uuid);
			}

			@Override
			public UUID backward(Person person)
			{
				return person.getUuid();
			}
		}, personRequestMessage.getUuids()));
		sendMessage(PersonResponseMessage.create(persons));
		return persons;
	}

	protected PersonResponseMessage dialogatePersonResponseRecv() throws IOException, ProtocolException
	{
		return recvMessage(PersonResponseMessage.class);
	}

}
