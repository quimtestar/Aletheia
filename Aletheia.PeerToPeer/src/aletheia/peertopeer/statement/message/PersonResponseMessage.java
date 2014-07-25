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
package aletheia.peertopeer.statement.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.authority.PersonProtocol;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

@MessageSubProtocolInfo(subProtocolClass = PersonResponseMessage.SubProtocol.class)
public class PersonResponseMessage extends AbstractUUIDPersistentInfoMessage<Person>
{
	private final static Bijection<Person, Entry<Person>> entryBijection = new Bijection<Person, Entry<Person>>()
			{

		@Override
		public Entry<Person> forward(Person person)
		{
			return new Entry<Person>(person.getUuid(), person);
		}

		@Override
		public Person backward(Entry<Person> entry)
		{
			return entry.getValue();
		}
			};

			public static PersonResponseMessage create(Collection<Person> persons)
			{
				return new PersonResponseMessage(new BijectionCollection<Person, Entry<Person>>(entryBijection, persons));
			}

			private PersonResponseMessage(Collection<Entry<Person>> entries)
			{
				super(entries);
			}

			@ProtocolInfo(availableVersions = 0)
			public static class SubProtocol extends AbstractUUIDPersistentInfoMessage.SubProtocol<Person, PersonResponseMessage>
			{
				private final PersonProtocol personProtocol;

				public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
				{
					super(0, persistenceManager, transaction, messageCode);
					checkVersionAvailability(SubProtocol.class, requiredVersion);
					this.personProtocol = new PersonProtocol(0, persistenceManager, transaction);
				}

				@Override
				protected void sendValue(UUID uuid, DataOutput out, Person person) throws IOException
				{
					personProtocol.send(out, person);
				}

				@Override
				protected Person recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
				{
					return personProtocol.recv(in);
				}

				@Override
				protected void skipValue(DataInput in) throws IOException, ProtocolException
				{
					personProtocol.skip(in);
				}

				@Override
				public PersonResponseMessage recv(DataInput in) throws IOException, ProtocolException
				{
					return new PersonResponseMessage(recvEntries(in));
				}

			}

}
