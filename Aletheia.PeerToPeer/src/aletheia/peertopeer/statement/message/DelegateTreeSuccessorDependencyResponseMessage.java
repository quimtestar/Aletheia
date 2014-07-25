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
import java.util.List;
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

@MessageSubProtocolInfo(subProtocolClass = DelegateTreeSuccessorDependencyResponseMessage.SubProtocol.class)
public class DelegateTreeSuccessorDependencyResponseMessage extends DelegateTreeDependencyResponseMessage<Person>
{

	public DelegateTreeSuccessorDependencyResponseMessage(Collection<? extends AbstractUUIDPersistentInfoMessage.Entry<Person>> entries)
	{
		super(entries);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends DelegateTreeDependencyResponseMessage.SubProtocol<Person, DelegateTreeSuccessorDependencyResponseMessage>
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
		public DelegateTreeSuccessorDependencyResponseMessage recv(DataInput in) throws IOException, ProtocolException
		{
			List<AbstractUUIDPersistentInfoMessage.Entry<Person>> entries = recvEntries(in);
			return new DelegateTreeSuccessorDependencyResponseMessage(entries);
		}

	}

}
