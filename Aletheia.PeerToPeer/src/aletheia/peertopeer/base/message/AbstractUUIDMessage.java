/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer.base.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

public abstract class AbstractUUIDMessage extends NonPersistentMessage
{
	private final List<UUID> uuids;

	public class RequestingNothingException extends RuntimeException
	{
		private static final long serialVersionUID = 7274591968153496556L;
	}

	public AbstractUUIDMessage(Collection<UUID> uuids)
	{
		super();
		this.uuids = new ArrayList<>(uuids);
	}

	public List<UUID> getUuids()
	{
		return Collections.unmodifiableList(uuids);
	}

	public boolean isEmpty()
	{
		return uuids.isEmpty();
	}

	@ProtocolInfo(availableVersions = 0)
	public abstract static class SubProtocol<M extends AbstractUUIDMessage> extends NonPersistentMessage.SubProtocol<M>
	{
		protected final IntegerProtocol integerProtocol;
		protected final UUIDProtocol uuidProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.integerProtocol = new IntegerProtocol(0);
			this.uuidProtocol = new UUIDProtocol(0);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			integerProtocol.send(out, m.getUuids().size());
			for (UUID uuid : m.getUuids())
				uuidProtocol.send(out, uuid);
		}

		protected Collection<UUID> recvUuids(DataInput in) throws IOException, ProtocolException
		{
			int n = integerProtocol.recv(in);
			List<UUID> uuids = new ArrayList<>();
			for (int i = 0; i < n; i++)
				uuids.add(uuidProtocol.recv(in));
			return uuids;
		}

		protected void skipUuids(DataInput in) throws IOException
		{
			int n = integerProtocol.recv(in);
			for (int i = 0; i < n; i++)
				uuidProtocol.skip(in);
		}

		@Override
		final public void skip(DataInput in) throws IOException
		{
			skipUuids(in);
		}

	}

}
