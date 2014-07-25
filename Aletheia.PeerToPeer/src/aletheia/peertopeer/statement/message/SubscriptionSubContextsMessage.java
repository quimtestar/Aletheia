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
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.SetProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = SubscriptionSubContextsMessage.SubProtocol.class)
public class SubscriptionSubContextsMessage extends AbstractUUIDInfoMessage<SubscriptionSubContextsMessage.SubContextSubscriptionUuids>
{

	public static class Entry extends AbstractUUIDInfoMessage.Entry<SubContextSubscriptionUuids>
	{

		public Entry(UUID uuid, SubContextSubscriptionUuids value)
		{
			super(uuid, value);
		}

	}

	public SubscriptionSubContextsMessage(Collection<? extends AbstractUUIDInfoMessage.Entry<SubContextSubscriptionUuids>> entries)
	{
		super(entries);
	}

	public static class SubContextSubscriptionUuids
	{
		private final Set<UUID> contextUuids;
		private final Set<UUID> proofUuids;

		public SubContextSubscriptionUuids(Set<UUID> contextUuids, Set<UUID> proofUuids)
		{
			this.contextUuids = contextUuids;
			this.proofUuids = proofUuids;
		}

		public Set<UUID> getContextUuids()
		{
			return contextUuids;
		}

		public Set<UUID> getProofUuids()
		{
			return proofUuids;
		}
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDInfoMessage.SubProtocol<SubContextSubscriptionUuids, SubscriptionSubContextsMessage>
	{
		private final UUIDProtocol uuidProtocol;
		private final SetProtocol<UUID> uuidSetProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.uuidProtocol = new UUIDProtocol(0);
			this.uuidSetProtocol = new SetProtocol<>(0, uuidProtocol);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, SubContextSubscriptionUuids v) throws IOException
		{
			uuidSetProtocol.send(out, v.getContextUuids());
			uuidSetProtocol.send(out, v.getProofUuids());
		}

		@Override
		protected SubContextSubscriptionUuids recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			Set<UUID> contextUuids = uuidSetProtocol.recv(in);
			Set<UUID> proofUuids = uuidSetProtocol.recv(in);
			return new SubContextSubscriptionUuids(contextUuids, proofUuids);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			uuidSetProtocol.skip(in);
			uuidSetProtocol.skip(in);
		}

		@Override
		public SubscriptionSubContextsMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new SubscriptionSubContextsMessage(recvEntries(in));
		}

	}

}
