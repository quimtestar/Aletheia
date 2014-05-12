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
package aletheia.peertopeer.resource;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

public class PrivatePersonResource extends AbstractPersonResource
{

	public PrivatePersonResource(UUID uuid)
	{
		super(uuid);
	}

	@Override
	public String toString()
	{
		return super.toString() + " (private)";
	}

	protected static class SubSubProtocol extends AbstractPersonResource.SubProtocol.SubSubProtocol<PrivatePersonResource>
	{

		@Override
		void send(DataOutput out, PrivatePersonResource r)
		{
		}

		@Override
		PrivatePersonResource recv(UUID uuid, DataInput in)
		{
			return new PrivatePersonResource(uuid);
		}

		@Override
		void skip(DataInput in)
		{
		}

	}

	@Override
	protected Resource.Metadata.SubProtocol<Metadata> metadataSubProtocol()
	{
		return new Metadata.SubProtocol(getMetadataSubProtocolVersion(), this);
	}

	public static class Metadata extends AbstractPersonResource.Metadata
	{
		public Metadata(PrivatePersonResource resource)
		{
			super(resource);
		}

		public Metadata(UUID uuid)
		{
			this(new PrivatePersonResource(uuid));
		}

		@Override
		public PrivatePersonResource getResource()
		{
			return (PrivatePersonResource) super.getResource();
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends AbstractPersonResource.Metadata.SubProtocol<Metadata>
		{
			public SubProtocol(int requiredVersion, PrivatePersonResource resource)
			{
				super(0, resource);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			protected PrivatePersonResource getResource()
			{
				return (PrivatePersonResource) super.getResource();
			}

			@Override
			public Metadata recv(DataInput in) throws IOException, ProtocolException
			{
				return new Metadata(getResource());
			}

			@Override
			public void send(DataOutput out, Metadata m) throws IOException
			{
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
			}

		}

	}

}
