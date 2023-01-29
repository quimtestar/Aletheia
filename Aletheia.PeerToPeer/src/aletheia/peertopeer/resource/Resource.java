/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.primitive.UUIDProtocol;

public abstract class Resource implements Exportable
{
	@ExportableEnumInfo(availableVersions = 0)
	public enum Type implements ByteExportableEnum<Type>
	{
		//@formatter:off
		RootContextSignature((byte)0,new RootContextSignatureResource.SubProtocol(0),0),
		SubscribedStatementsContext((byte)1,new SubscribedStatementsContextResource.SubProtocol(0),0),
		StatementProof((byte)2, new StatementProofResource.SubProtocol(0),0),
		Person((byte)3,new AbstractPersonResource.SubProtocol(0),0),
		;
		//@formatter:on

		private final byte code;
		private final SubProtocol<? extends Resource> subProtocol;
		private final int metadataSubProtocolVersion;

		private Type(byte code, SubProtocol<? extends Resource> subProtocol, int metadataSubProtocolVersion)
		{
			this.code = code;
			this.subProtocol = subProtocol;
			this.metadataSubProtocolVersion = metadataSubProtocolVersion;
		}

		@Override
		public Byte getCode(int version)
		{
			return code;
		}

		public SubProtocol<? extends Resource> getSubProtocol()
		{
			return subProtocol;
		}

		public int getMetadataSubProtocolVersion()
		{
			return metadataSubProtocolVersion;
		}

		@ProtocolInfo(availableVersions = 0)
		public static class Protocol extends ByteExportableEnumProtocol<Type>
		{
			public Protocol(int requiredVersion)
			{
				super(0, Type.class, 0);
				checkVersionAvailability(Protocol.class, requiredVersion);
			}
		}
	}

	private final Type type;
	private final UUID uuid;

	public Resource(Type type, UUID uuid)
	{
		this.type = type;
		this.uuid = uuid;
	}

	public Type getType()
	{
		return type;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		Resource other = (Resource) obj;
		if (type != other.type)
			return false;
		if (uuid == null)
		{
			if (other.uuid != null)
				return false;
		}
		else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Resource [type=" + type + ", uuid=" + uuid + "]";
	}

	protected int getMetadataSubProtocolVersion()
	{
		return getType().getMetadataSubProtocolVersion();
	}

	protected abstract Metadata.SubProtocol<? extends Metadata> metadataSubProtocol();

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ExportableProtocol<Resource>
	{
		private final Type.Protocol typeProtocol = new Type.Protocol(0);

		public Protocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, Resource resource) throws IOException
		{
			typeProtocol.send(out, resource.getType());
			resource.getType().getSubProtocol().sendResource(out, resource);
		}

		@Override
		public Resource recv(DataInput in) throws IOException, ProtocolException
		{
			Type type = typeProtocol.recv(in);
			Resource resource = type.getSubProtocol().recv(in);
			if (resource.getType() != type)
				throw new Error();
			return resource;
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			Type type = typeProtocol.recv(in);
			type.getSubProtocol().skip(in);
		}
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends Resource> extends ExportableProtocol<M>
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);

		protected SubProtocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			uuidProtocol.send(out, m.getUuid());
		}

		@SuppressWarnings("unchecked")
		public void sendResource(DataOutput out, Resource m) throws IOException
		{
			send(out, (M) m);
		}

		protected abstract M recv(UUID uuid, DataInput in) throws IOException, ProtocolException;

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			UUID uuid = uuidProtocol.recv(in);
			return recv(uuid, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
		}
	}

	public static abstract class Metadata implements Exportable
	{
		private final Resource resource;

		public Metadata(Resource resource)
		{
			this.resource = resource;
		}

		public Resource getResource()
		{
			return resource;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((resource == null) ? 0 : resource.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			Metadata other = (Metadata) obj;
			if (resource == null)
			{
				if (other.resource != null)
					return false;
			}
			else if (!resource.equals(other.resource))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "Metadata [resource=" + resource + "]";
		}

		@ProtocolInfo(availableVersions = 0)
		public static class Protocol extends ExportableProtocol<Metadata>
		{
			private final Resource.Protocol resourceProtocol = new Resource.Protocol(0);

			public Protocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(Protocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, Metadata metadata) throws IOException
			{
				resourceProtocol.send(out, metadata.getResource());
				metadata.getResource().metadataSubProtocol().sendMetadata(out, metadata);
			}

			@Override
			public Metadata recv(DataInput in) throws IOException, ProtocolException
			{
				Resource resource = resourceProtocol.recv(in);
				Metadata metadata = resource.metadataSubProtocol().recv(in);
				if (!metadata.getResource().equals(resource))
					throw new Error();
				return metadata;
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				Resource resource = resourceProtocol.recv(in);
				resource.metadataSubProtocol().skip(in);
			}
		}

		@ProtocolInfo(availableVersions = 0)
		public abstract static class SubProtocol<M extends Metadata> extends ExportableProtocol<M>
		{
			private final Resource resource;

			public SubProtocol(int requiredVersion, Resource resource)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
				this.resource = resource;
			}

			protected Resource getResource()
			{
				return resource;
			}

			@SuppressWarnings("unchecked")
			public void sendMetadata(DataOutput out, Metadata m) throws IOException
			{
				send(out, (M) m);
			}

			@Override
			public abstract void send(DataOutput out, M m) throws IOException;

			@Override
			public abstract M recv(DataInput in) throws IOException, ProtocolException;

			@Override
			public abstract void skip(DataInput in) throws IOException, ProtocolException;
		}

	}

}
