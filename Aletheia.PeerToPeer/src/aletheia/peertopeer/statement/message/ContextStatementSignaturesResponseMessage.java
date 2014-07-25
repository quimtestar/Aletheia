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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = ContextStatementSignaturesResponseMessage.SubProtocol.class)
public class ContextStatementSignaturesResponseMessage extends AbstractUUIDInfoMessage<Set<StatementAuthoritySubMessage>>
{

	public static class Entry extends AbstractUUIDInfoMessage.Entry<Set<StatementAuthoritySubMessage>>
	{

		public Entry(UUID uuid, Set<StatementAuthoritySubMessage> value)
		{
			super(uuid, value);
		}

	}

	public ContextStatementSignaturesResponseMessage(Collection<? extends AbstractUUIDInfoMessage.Entry<Set<StatementAuthoritySubMessage>>> entries)
	{
		super(entries);
	}

	@ProtocolInfo(availableVersions = 1)
	public static class SubProtocol extends AbstractUUIDInfoMessage.SubProtocol<Set<StatementAuthoritySubMessage>, ContextStatementSignaturesResponseMessage>
	{
		private final StatementAuthoritySubMessage.SubProtocol statementMessageDataProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.statementMessageDataProtocol = new StatementAuthoritySubMessage.SubProtocol(1);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, Set<StatementAuthoritySubMessage> v) throws IOException
		{
			integerProtocol.send(out, v.size());
			for (StatementAuthoritySubMessage statementAuthoritySubMessage : v)
				statementMessageDataProtocol.send(out, statementAuthoritySubMessage);
		}

		@Override
		protected Set<StatementAuthoritySubMessage> recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			Set<StatementAuthoritySubMessage> v = new HashSet<StatementAuthoritySubMessage>();
			int n = integerProtocol.recv(in);
			for (int i = 0; i < n; i++)
				v.add(statementMessageDataProtocol.recv(in));
			return v;
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			int n = integerProtocol.recv(in);
			for (int i = 0; i < n; i++)
				statementMessageDataProtocol.skip(in);
		}

		@Override
		public ContextStatementSignaturesResponseMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new ContextStatementSignaturesResponseMessage(recvEntries(in));
		}

	}

}
