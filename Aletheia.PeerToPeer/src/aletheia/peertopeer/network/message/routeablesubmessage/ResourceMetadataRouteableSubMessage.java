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

@RouteableSubMessageSubProtocolInfo(subProtocolClass = ResourceMetadataRouteableSubMessage.SubProtocol.class)
public class ResourceMetadataRouteableSubMessage extends TargetRouteableSubMessage
{
	private final Resource.Metadata resourceMetadata;

	public ResourceMetadataRouteableSubMessage(UUID origin, int sequence, UUID target, Resource.Metadata resourceMetadata)
	{
		super(origin, sequence, target);
		this.resourceMetadata = resourceMetadata;
	}

	public Resource.Metadata getResourceMetadata()
	{
		return resourceMetadata;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends TargetRouteableSubMessage.SubProtocol<ResourceMetadataRouteableSubMessage>
	{
		private final Resource.Metadata.Protocol resourceMetadataProtocol = new Resource.Metadata.Protocol(0);

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, ResourceMetadataRouteableSubMessage m) throws IOException
		{
			super.send(out, m);
			resourceMetadataProtocol.send(out, m.getResourceMetadata());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			resourceMetadataProtocol.skip(in);
		}

		@Override
		protected ResourceMetadataRouteableSubMessage recv(UUID origin, int sequence, UUID target, DataInput in) throws ProtocolException, IOException
		{
			Resource.Metadata resourceMetadata = resourceMetadataProtocol.recv(in);
			return new ResourceMetadataRouteableSubMessage(origin, sequence, target, resourceMetadata);
		}

	}

}
