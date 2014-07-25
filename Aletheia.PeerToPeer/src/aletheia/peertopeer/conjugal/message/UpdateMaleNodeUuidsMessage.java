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
package aletheia.peertopeer.conjugal.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = UpdateMaleNodeUuidsMessage.SubProtocol.class)
public class UpdateMaleNodeUuidsMessage extends NonPersistentMessage
{
	private final Collection<UUID> addUuids;
	private final Collection<UUID> removeUuids;

	public UpdateMaleNodeUuidsMessage(Collection<UUID> addUuids, Collection<UUID> removeUuids)
	{
		this.addUuids = addUuids;
		this.removeUuids = removeUuids;
	}

	public Collection<UUID> getAddUuids()
	{
		return addUuids;
	}

	public Collection<UUID> getRemoveUuids()
	{
		return removeUuids;
	}

	public static class SubProtocol extends NonPersistentMessage.SubProtocol<UpdateMaleNodeUuidsMessage>
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		private final CollectionProtocol<UUID> uuidCollectionProtocol = new CollectionProtocol<>(0, uuidProtocol);
		private final NullableProtocol<Collection<UUID>> nullableUuidCollectionProtocol = new NullableProtocol<>(0, uuidCollectionProtocol);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(requiredVersion, messageCode);
		}

		@Override
		public void send(DataOutput out, UpdateMaleNodeUuidsMessage m) throws IOException
		{
			nullableUuidCollectionProtocol.send(out, m.getAddUuids());
			nullableUuidCollectionProtocol.send(out, m.getRemoveUuids());
		}

		@Override
		public UpdateMaleNodeUuidsMessage recv(DataInput in) throws IOException, ProtocolException
		{
			Collection<UUID> addUuids = nullableUuidCollectionProtocol.recv(in);
			Collection<UUID> removeUuids = nullableUuidCollectionProtocol.recv(in);
			return new UpdateMaleNodeUuidsMessage(addUuids, removeUuids);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			nullableUuidCollectionProtocol.skip(in);
			nullableUuidCollectionProtocol.skip(in);
		}

	}

}
