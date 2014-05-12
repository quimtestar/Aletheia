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

public class SubscribedStatementsContextResource extends Resource
{
	public SubscribedStatementsContextResource(UUID uuid)
	{
		super(Type.SubscribedStatementsContext, uuid);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends Resource.SubProtocol<SubscribedStatementsContextResource>
	{
		protected SubProtocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected SubscribedStatementsContextResource recv(UUID uuid, DataInput in)
		{
			return new SubscribedStatementsContextResource(uuid);
		}

	}

	@Override
	protected Metadata.SubProtocol metadataSubProtocol()
	{
		return new Metadata.SubProtocol(getMetadataSubProtocolVersion(), this);
	}

	public static class Metadata extends Resource.Metadata
	{
		public Metadata(SubscribedStatementsContextResource resource)
		{
			super(resource);
		}

		public Metadata(UUID uuid)
		{
			this(new SubscribedStatementsContextResource(uuid));
		}

		@Override
		public SubscribedStatementsContextResource getResource()
		{
			return (SubscribedStatementsContextResource) super.getResource();
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends Resource.Metadata.SubProtocol<Metadata>
		{
			public SubProtocol(int requiredVersion, SubscribedStatementsContextResource resource)
			{
				super(0, resource);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			protected SubscribedStatementsContextResource getResource()
			{
				return (SubscribedStatementsContextResource) super.getResource();
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
