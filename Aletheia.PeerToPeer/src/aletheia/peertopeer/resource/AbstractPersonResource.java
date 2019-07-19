/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;

public abstract class AbstractPersonResource extends Resource
{

	public AbstractPersonResource(UUID uuid)
	{
		super(Type.Person, uuid);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends Resource.SubProtocol<AbstractPersonResource>
	{

		protected SubProtocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@ExportableEnumInfo(availableVersions = 0)
		private static enum Type implements ByteExportableEnum<Type>
		{
			Person((byte) 0), PrivatePerson((byte) 1),;

			private final byte code;

			private Type(byte code)
			{
				this.code = code;
			}

			@Override
			public Byte getCode(int version)
			{
				return code;
			}

			@ProtocolInfo(availableVersions = 0)
			private static class Protocol extends ByteExportableEnumProtocol<Type>
			{

				public Protocol(int requiredVersion)
				{
					super(0, Type.class, 0);
					checkVersionAvailability(Protocol.class, requiredVersion);
				}

			}

		}

		private final Type.Protocol typeProtocol = new Type.Protocol(0);

		protected static abstract class SubSubProtocol<R extends AbstractPersonResource>
		{
			abstract void send(DataOutput out, R r);

			abstract R recv(UUID uuid, DataInput in);

			abstract void skip(DataInput in);

		}

		private final PersonResource.SubSubProtocol personResourceSubSubProtocol = new PersonResource.SubSubProtocol();
		private final PrivatePersonResource.SubSubProtocol privatePersonResourceSubSubProtocol = new PrivatePersonResource.SubSubProtocol();

		@Override
		public void send(DataOutput out, AbstractPersonResource r) throws IOException
		{
			super.send(out, r);
			if (r instanceof PersonResource)
			{
				typeProtocol.send(out, Type.Person);
				personResourceSubSubProtocol.send(out, (PersonResource) r);
			}
			else if (r instanceof PrivatePersonResource)
			{
				typeProtocol.send(out, Type.PrivatePerson);
				privatePersonResourceSubSubProtocol.send(out, (PrivatePersonResource) r);
			}
			else
				throw new Error();

		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			Type type = typeProtocol.recv(in);
			switch (type)
			{
			case Person:
				personResourceSubSubProtocol.skip(in);
				break;
			case PrivatePerson:
				privatePersonResourceSubSubProtocol.skip(in);
				break;
			default:
				throw new ProtocolException();
			}
		}

		@Override
		protected AbstractPersonResource recv(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			Type type = typeProtocol.recv(in);
			switch (type)
			{
			case Person:
				return personResourceSubSubProtocol.recv(uuid, in);
			case PrivatePerson:
				return privatePersonResourceSubSubProtocol.recv(uuid, in);
			default:
				throw new ProtocolException();
			}
		}

	}

	public static abstract class Metadata extends Resource.Metadata
	{
		public Metadata(AbstractPersonResource resource)
		{
			super(resource);
		}

		@Override
		public AbstractPersonResource getResource()
		{
			return (AbstractPersonResource) super.getResource();
		}

		@ProtocolInfo(availableVersions = 0)
		public static abstract class SubProtocol<M extends Metadata> extends Resource.Metadata.SubProtocol<M>
		{
			public SubProtocol(int requiredVersion, AbstractPersonResource resource)
			{
				super(0, resource);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			protected AbstractPersonResource getResource()
			{
				return (AbstractPersonResource) super.getResource();
			}

		}

	}

}
