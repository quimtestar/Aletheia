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
package aletheia.peertopeer.base.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

public abstract class AbstractUUIDPersistentInfoMessage<V> extends PersistentMessage
{
	public static class Entry<V> implements Map.Entry<UUID, V>
	{
		public final UUID uuid;
		public final V value;

		public Entry(UUID uuid, V value)
		{
			super();
			this.uuid = uuid;
			this.value = value;
		}

		@Override
		public UUID getKey()
		{
			return uuid;
		}

		@Override
		public V getValue()
		{
			return value;
		}

		@Override
		public V setValue(V value)
		{
			throw new UnsupportedOperationException();
		}
	}

	private final List<Entry<V>> entries;
	private final Map<UUID, V> map;

	public AbstractUUIDPersistentInfoMessage(Collection<? extends Entry<V>> entries)
	{
		super();
		this.entries = new ArrayList<Entry<V>>(entries);
		this.map = new HashMap<UUID, V>();
		for (Entry<V> e : this.entries)
		{
			if (!map.containsKey(e.getKey()))
				map.put(e.getKey(), e.getValue());
		}
	}

	public List<Entry<V>> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	public Map<UUID, V> getMap()
	{
		return Collections.unmodifiableMap(map);
	}

	@ProtocolInfo(availableVersions = 0)
	public abstract static class SubProtocol<V, M extends AbstractUUIDPersistentInfoMessage<V>> extends PersistentMessage.SubProtocol<M>
	{
		protected final IntegerProtocol integerProtocol;
		protected final UUIDProtocol uuidProtocol;

		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.integerProtocol = new IntegerProtocol(0);
			this.uuidProtocol = new UUIDProtocol(0);
		}

		protected abstract void sendValue(UUID uuid, DataOutput out, V v) throws IOException;

		protected abstract V recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException;

		protected abstract void skipValue(DataInput in) throws IOException, ProtocolException;

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			integerProtocol.send(out, m.getEntries().size());
			for (Entry<V> e : m.getEntries())
			{
				uuidProtocol.send(out, e.uuid);
				sendValue(e.uuid, out, e.value);
			}
		}

		protected List<Entry<V>> recvEntries(DataInput in) throws IOException, ProtocolException
		{
			int n = integerProtocol.recv(in);
			List<Entry<V>> entries = new ArrayList<Entry<V>>();
			for (int i = 0; i < n; i++)
			{
				UUID uuid = uuidProtocol.recv(in);
				V v = recvValue(uuid, in);
				entries.add(new Entry<V>(uuid, v));
			}
			return entries;
		}

		protected void skipEntries(DataInput in) throws IOException, ProtocolException
		{
			int n = integerProtocol.recv(in);
			for (int i = 0; i < n; i++)
			{
				uuidProtocol.skip(in);
				skipValue(in);
			}
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			skipEntries(in);
		}

	}

}
