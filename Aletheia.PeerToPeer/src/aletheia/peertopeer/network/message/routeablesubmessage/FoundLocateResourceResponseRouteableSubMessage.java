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
import java.net.InetSocketAddress;
import java.util.UUID;

import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.ResourceInfo;
import aletheia.peertopeer.resource.Resource;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.net.InetSocketAddressProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@RouteableSubMessageSubProtocolInfo(subProtocolClass = FoundLocateResourceResponseRouteableSubMessage.SubProtocol.class)
public class FoundLocateResourceResponseRouteableSubMessage extends LocateResourceResponseRouteableSubMessage
{
	private final Resource.Metadata resourceMetadata;
	private final UUID node;

	private InetSocketAddress address;

	public FoundLocateResourceResponseRouteableSubMessage(UUID origin, int sequence, UUID target, int sequenceResponse, Resource.Metadata resourceMetadata,
			UUID node)
	{
		super(origin, sequence, target, sequenceResponse, resourceMetadata.getResource());
		this.resourceMetadata = resourceMetadata;
		this.node = node;
	}

	public Resource.Metadata getResourceMetadata()
	{
		return resourceMetadata;
	}

	public UUID getNode()
	{
		return node;
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	public void setAddress(InetSocketAddress address)
	{
		this.address = address;
	}

	public NodeAddress nodeAddress()
	{
		return new NodeAddress(getNode(), getAddress());
	}

	public ResourceInfo resourceInfo()
	{
		return new ResourceInfo(nodeAddress(), getResourceMetadata());
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends LocateResourceResponseRouteableSubMessage.SubProtocol<FoundLocateResourceResponseRouteableSubMessage>
	{
		private final Resource.Metadata.Protocol resourceMetadataProtocol = new Resource.Metadata.Protocol(0);
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		private final NullableProtocol<InetSocketAddress> nullableInetSocketAddressProtocol = new NullableProtocol<>(0, new InetSocketAddressProtocol(0));

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected FoundLocateResourceResponseRouteableSubMessage recv(UUID origin, int sequence, UUID target, int sequenceResponse, Resource resource,
				DataInput in) throws IOException, ProtocolException
		{
			Resource.Metadata resourceMetadata = resourceMetadataProtocol.recv(in);
			if (!resourceMetadata.getResource().equals(resource))
				throw new ProtocolException();
			UUID node = uuidProtocol.recv(in);
			FoundLocateResourceResponseRouteableSubMessage m = new FoundLocateResourceResponseRouteableSubMessage(origin, sequence, target, sequenceResponse,
					resourceMetadata, node);
			InetSocketAddress address = nullableInetSocketAddressProtocol.recv(in);
			m.setAddress(address);
			return m;
		}

		@Override
		public void send(DataOutput out, FoundLocateResourceResponseRouteableSubMessage m) throws IOException
		{
			super.send(out, m);
			resourceMetadataProtocol.send(out, m.getResourceMetadata());
			uuidProtocol.send(out, m.getNode());
			nullableInetSocketAddressProtocol.send(out, m.getAddress());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			resourceMetadataProtocol.skip(in);
			uuidProtocol.skip(in);
			nullableInetSocketAddressProtocol.skip(in);
		}

	}

}
