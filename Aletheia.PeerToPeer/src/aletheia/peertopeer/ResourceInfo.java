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
package aletheia.peertopeer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.peertopeer.resource.Resource;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

public class ResourceInfo implements Exportable
{
	private final NodeAddress nodeAddress;
	private final Resource.Metadata resourceMetadata;

	public ResourceInfo(NodeAddress nodeAddress, Resource.Metadata resourceMetadata)
	{
		super();
		this.nodeAddress = nodeAddress;
		this.resourceMetadata = resourceMetadata;
	}

	public NodeAddress getNodeAddress()
	{
		return nodeAddress;
	}

	public Resource.Metadata getResourceMetadata()
	{
		return resourceMetadata;
	}

	@Override
	public String toString()
	{
		return "[ " + nodeAddress + ": " + resourceMetadata + "]";
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ExportableProtocol<ResourceInfo>
	{
		private final NodeAddress.Protocol nodeAddressProtocol = new NodeAddress.Protocol(0);
		private final Resource.Metadata.Protocol resourceMetadataProtocol = new Resource.Metadata.Protocol(0);

		protected Protocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, ResourceInfo resourceInfo) throws IOException
		{
			nodeAddressProtocol.send(out, resourceInfo.getNodeAddress());
			resourceMetadataProtocol.send(out, resourceInfo.getResourceMetadata());
		}

		@Override
		public ResourceInfo recv(DataInput in) throws IOException, ProtocolException
		{
			NodeAddress nodeAddress = nodeAddressProtocol.recv(in);
			Resource.Metadata resourceMetadata = resourceMetadataProtocol.recv(in);
			return new ResourceInfo(nodeAddress, resourceMetadata);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			nodeAddressProtocol.skip(in);
			resourceMetadataProtocol.skip(in);
		}
	}

}
