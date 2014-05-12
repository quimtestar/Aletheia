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
package aletheia.peertopeer.network.message.routeablesubmessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.peertopeer.resource.Resource;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

public abstract class ResourceRouteableSubMessage extends RouteableSubMessage
{
	private final Resource resource;

	public ResourceRouteableSubMessage(UUID origin, int sequence, Resource resource)
	{
		super(origin, sequence);
		this.resource = resource;
	}

	public Resource getResource()
	{
		return resource;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends ResourceRouteableSubMessage> extends RouteableSubMessage.SubProtocol<M>
	{
		private final Resource.Protocol resourceProtocol = new Resource.Protocol(0);

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		protected abstract M recv(UUID origin, int sequence, Resource resource, DataInput in) throws IOException, ProtocolException;

		@Override
		protected M recv(UUID origin, int sequence, DataInput in) throws IOException, ProtocolException
		{
			Resource resource = resourceProtocol.recv(in);
			return recv(origin, sequence, resource, in);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			super.send(out, m);
			resourceProtocol.send(out, m.getResource());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			resourceProtocol.skip(in);
		}

	}

}
