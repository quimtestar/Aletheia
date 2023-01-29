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
import aletheia.protocol.primitive.BooleanProtocol;

public class RootContextSignatureResource extends Resource
{

	public RootContextSignatureResource(UUID uuid)
	{
		super(Type.RootContextSignature, uuid);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends Resource.SubProtocol<RootContextSignatureResource>
	{
		protected SubProtocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected RootContextSignatureResource recv(UUID uuid, DataInput in)
		{
			return new RootContextSignatureResource(uuid);
		}
	}

	@Override
	protected Metadata.SubProtocol metadataSubProtocol()
	{
		return new Metadata.SubProtocol(getMetadataSubProtocolVersion(), this);
	}

	public static class Metadata extends Resource.Metadata
	{
		private final boolean provided;
		private final boolean subscribed;

		public Metadata(RootContextSignatureResource resource, boolean provided, boolean subscribed)
		{
			super(resource);
			this.provided = provided;
			this.subscribed = subscribed;
		}

		public Metadata(UUID uuid, boolean provided, boolean subscribed)
		{
			this(new RootContextSignatureResource(uuid), provided, subscribed);
		}

		public boolean isProvided()
		{
			return provided;
		}

		public boolean isSubscribed()
		{
			return subscribed;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (provided ? 1231 : 1237);
			result = prime * result + (subscribed ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj) || (getClass() != obj.getClass()))
				return false;
			Metadata other = (Metadata) obj;
			if (provided != other.provided)
				return false;
			if (subscribed != other.subscribed)
				return false;
			return true;
		}

		@Override
		public RootContextSignatureResource getResource()
		{
			return (RootContextSignatureResource) super.getResource();
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends Resource.Metadata.SubProtocol<Metadata>
		{
			private final BooleanProtocol booleanProtocol;

			public SubProtocol(int requiredVersion, RootContextSignatureResource resource)
			{
				super(0, resource);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
				this.booleanProtocol = new BooleanProtocol(0);
			}

			@Override
			protected RootContextSignatureResource getResource()
			{
				return (RootContextSignatureResource) super.getResource();
			}

			@Override
			public Metadata recv(DataInput in) throws IOException, ProtocolException
			{
				boolean provided = booleanProtocol.recv(in);
				boolean subscribed = booleanProtocol.recv(in);
				return new Metadata(getResource(), provided, subscribed);
			}

			@Override
			public void send(DataOutput out, Metadata m) throws IOException
			{
				booleanProtocol.send(out, m.isProvided());
				booleanProtocol.send(out, m.isSubscribed());

			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				booleanProtocol.skip(in);
				booleanProtocol.skip(in);
			}

		}

	}

}
