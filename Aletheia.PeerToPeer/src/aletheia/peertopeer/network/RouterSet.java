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
package aletheia.peertopeer.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.SetProtocol;
import aletheia.protocol.primitive.BooleanProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

public interface RouterSet extends Exportable
{
	public interface Router
	{
		public int getDistance();

		public Set<UUID> getSpindle();

	}

	public List<? extends Router> getRouters();

	public Router getRouter(int i);

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ExportableProtocol<RouterSet>
	{
		private final IntegerProtocol integerProtocol;
		private final BooleanProtocol booleanProtocol;
		private final SetProtocol<UUID> uuidSetProtocol;

		public Protocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(Protocol.class, requiredVersion);
			this.integerProtocol = new IntegerProtocol(0);
			this.booleanProtocol = new BooleanProtocol(0);
			this.uuidSetProtocol = new SetProtocol<>(0, new UUIDProtocol(0));
		}

		@Override
		public void send(DataOutput out, RouterSet routerSet) throws IOException
		{
			synchronized (routerSet)
			{
				List<? extends Router> routers = routerSet.getRouters();
				integerProtocol.send(out, routers.size());
				for (int i = 0; i < routers.size(); i++)
				{
					Router router = routers.get(i);
					booleanProtocol.send(out, router != null);
					if (router != null)
					{
						integerProtocol.send(out, router.getDistance());
						uuidSetProtocol.send(out, router.getSpindle());
					}
				}
			}
		}

		@Override
		public RemoteRouterSet recv(DataInput in) throws IOException, ProtocolException
		{
			RemoteRouterSet remoteRouterSet = new RemoteRouterSet();
			int size = integerProtocol.recv(in);
			for (int i = 0; i < size; i++)
			{
				boolean present = booleanProtocol.recv(in);
				if (present)
				{
					int distance = integerProtocol.recv(in);
					Set<UUID> spindle = uuidSetProtocol.recv(in);
					remoteRouterSet.addRouter(new RemoteRouterSet.RemoteRouter(distance, spindle));
				}
				else
					remoteRouterSet.addRouter(null);
			}
			return remoteRouterSet;
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			int size = integerProtocol.recv(in);
			for (int i = 0; i < size; i++)
			{
				boolean present = booleanProtocol.recv(in);
				if (present)
				{
					integerProtocol.skip(in);
					uuidSetProtocol.skip(in);
				}
			}
		}
	}

}
