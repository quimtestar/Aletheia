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

import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

public abstract class AbstractRequisiteMessage extends AbstractUUIDInfoMessage<Collection<UUID>>
{
	public static class Entry extends AbstractUUIDInfoMessage.Entry<Collection<UUID>>
	{

		public Entry(UUID uuid, Collection<UUID> value)
		{
			super(uuid, value);
		}

	}

	public AbstractRequisiteMessage(Collection<? extends AbstractUUIDInfoMessage.Entry<Collection<UUID>>> entries)
	{
		super(entries);
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends AbstractUUIDInfoMessage<Collection<UUID>>> extends
			AbstractUUIDInfoMessage.SubProtocol<Collection<UUID>, M>
	{
		private final CollectionProtocol<UUID> uuidCollectionProtocol = new CollectionProtocol<UUID>(0, new UUIDProtocol(0));

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, Collection<UUID> uuids) throws IOException
		{
			uuidCollectionProtocol.send(out, uuids);
		}

		@Override
		protected Collection<UUID> recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			return uuidCollectionProtocol.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			uuidCollectionProtocol.skip(in);
		}

	}

}
