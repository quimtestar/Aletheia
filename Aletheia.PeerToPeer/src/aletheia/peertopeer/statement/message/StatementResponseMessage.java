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

import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.authority.StatementAuthorityProtocol;
import aletheia.protocol.statement.StatementProtocol;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

@MessageSubProtocolInfo(subProtocolClass = StatementResponseMessage.SubProtocol.class)
public class StatementResponseMessage extends AbstractUUIDPersistentInfoMessage<Statement>
{
	private final static Bijection<Statement, Entry<Statement>> entryBijection = new Bijection<Statement, Entry<Statement>>()
	{

		@Override
		public Entry<Statement> forward(Statement statement)
		{
			return new Entry<>(statement.getUuid(), statement);
		}

		@Override
		public Statement backward(Entry<Statement> entry)
		{
			return entry.getValue();
		}
	};

	public static StatementResponseMessage create(Collection<Statement> statements)
	{
		return new StatementResponseMessage(new BijectionCollection<>(entryBijection, statements));
	}

	private StatementResponseMessage(Collection<Entry<Statement>> entries)
	{
		super(entries);
	}

	@ProtocolInfo(availableVersions = 1)
	public static class SubProtocol extends AbstractUUIDPersistentInfoMessage.SubProtocol<Statement, StatementResponseMessage>
	{
		@ProtocolInfo(availableVersions = 1)
		private static class StatementProtocolWithAuthority extends PersistentExportableProtocol<Statement>
		{
			private final StatementProtocol statementProtocol;
			private final StatementAuthorityProtocol statementAuthorityProtocol;

			public StatementProtocolWithAuthority(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
			{
				super(0, persistenceManager, transaction);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
				this.statementProtocol = new StatementProtocol(1, persistenceManager, transaction); //TODO Version 2?
				this.statementAuthorityProtocol = new StatementAuthorityProtocol(0, persistenceManager, transaction);
			}

			@Override
			public void send(DataOutput out, Statement statement) throws IOException
			{
				statementProtocol.send(out, statement);
				StatementAuthority statementAuthority = statement.getAuthority(getTransaction());
				statementAuthorityProtocol.send(out, statementAuthority);
			}

			@Override
			public Statement recv(DataInput in) throws IOException, ProtocolException
			{
				Statement statement = statementProtocol.recv(in);
				StatementAuthority statementAuthority = statementAuthorityProtocol.recv(in);
				if (!statementAuthority.getStatementUuid().equals(statement.getUuid()))
					throw new ProtocolException();
				return statement;
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				statementProtocol.skip(in);
				statementAuthorityProtocol.skip(in);
			}
		}

		private final StatementProtocolWithAuthority statementProtocolWithAuthority;

		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.statementProtocolWithAuthority = new StatementProtocolWithAuthority(1, persistenceManager, transaction);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, Statement statement) throws IOException
		{
			statementProtocolWithAuthority.send(out, statement);
		}

		@Override
		protected Statement recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			return statementProtocolWithAuthority.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			statementProtocolWithAuthority.skip(in);
		}

		@Override
		public StatementResponseMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new StatementResponseMessage(recvEntries(in));
		}

	}

}
