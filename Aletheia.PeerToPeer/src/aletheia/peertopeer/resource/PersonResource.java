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
import java.util.Date;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.security.SignatureData;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.security.SignatureDataProtocol;

public class PersonResource extends AbstractPersonResource
{

	public PersonResource(UUID uuid)
	{
		super(uuid);
	}

	protected static class SubSubProtocol extends AbstractPersonResource.SubProtocol.SubSubProtocol<PersonResource>
	{

		@Override
		void send(DataOutput out, PersonResource r)
		{
		}

		@Override
		PersonResource recv(UUID uuid, DataInput in)
		{
			return new PersonResource(uuid);
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
		public static abstract class PersonInfo implements Exportable, Comparable<PersonInfo>
		{

			private static PersonInfo fromPerson(Person person)
			{
				if (person.isSigned())
					return new SignedPersonInfo(person);
				else
					return new UnsignedPersonInfo();
			}

			@Override
			public int hashCode()
			{
				return 31;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				return true;
			}

			public abstract boolean update(Transaction transaction, Person person) throws SignatureVerifyException;

			@ProtocolInfo(availableVersions = 0)
			private static class Protocol extends ExportableProtocol<PersonInfo>
			{
				@ExportableEnumInfo(availableVersions = 0)
				private enum Type implements ByteExportableEnum<Type>
				{
					Unsigned((byte) 0), Signed((byte) 1),;

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

				}

				private final ByteExportableEnumProtocol<Type> typeProtocol = new ByteExportableEnumProtocol<>(0, Type.class, 0);
				private final StringProtocol stringProtocol = new StringProtocol(0);
				private final NullableProtocol<String> nullableStringProtocol = new NullableProtocol<>(0, stringProtocol);
				private final DateProtocol dateProtocol = new DateProtocol(0);
				private final IntegerProtocol integerProtocol = new IntegerProtocol(0);
				private final SignatureDataProtocol signatureDataProtocol = new SignatureDataProtocol(0);

				protected Protocol(int requiredVersion)
				{
					super(0);
					checkVersionAvailability(Protocol.class, requiredVersion);
				}

				@Override
				public void send(DataOutput out, PersonInfo personInfo) throws IOException
				{
					if (personInfo instanceof UnsignedPersonInfo)
						typeProtocol.send(out, Type.Unsigned);
					else if (personInfo instanceof SignedPersonInfo)
					{
						typeProtocol.send(out, Type.Signed);
						SignedPersonInfo signedPersonInfo = (SignedPersonInfo) personInfo;
						stringProtocol.send(out, signedPersonInfo.nick);
						nullableStringProtocol.send(out, signedPersonInfo.name);
						nullableStringProtocol.send(out, signedPersonInfo.email);
						dateProtocol.send(out, signedPersonInfo.signatureDate);
						integerProtocol.send(out, signedPersonInfo.signatureVersion);
						signatureDataProtocol.send(out, signedPersonInfo.signatureData);
					}
					else
						throw new Error();
				}

				@Override
				public PersonInfo recv(DataInput in) throws IOException, ProtocolException
				{
					Type type = typeProtocol.recv(in);
					switch (type)
					{
					case Unsigned:
						return new UnsignedPersonInfo();
					case Signed:
					{
						String nick = stringProtocol.recv(in);
						String name = nullableStringProtocol.recv(in);
						String email = nullableStringProtocol.recv(in);
						Date signatureDate = dateProtocol.recv(in);
						int signatureVersion = integerProtocol.recv(in);
						SignatureData signatureData = signatureDataProtocol.recv(in);
						return new SignedPersonInfo(nick, name, email, signatureDate, signatureVersion, signatureData);
					}
					default:
						throw new ProtocolException();
					}
				}

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
					Type type = typeProtocol.recv(in);
					switch (type)
					{
					case Unsigned:
						break;
					case Signed:
					{
						nullableStringProtocol.skip(in);
						nullableStringProtocol.skip(in);
						nullableStringProtocol.skip(in);
						dateProtocol.skip(in);
						integerProtocol.skip(in);
						signatureDataProtocol.skip(in);
						break;
					}
					default:
						throw new ProtocolException();
					}
				}

			}

		}

		private static class UnsignedPersonInfo extends PersonInfo
		{

			@Override
			public String toString()
			{
				return "UnsignedPersonInfo []";
			}

			@Override
			public int compareTo(PersonInfo o)
			{
				if (o instanceof UnsignedPersonInfo)
					return 0;
				else if (o instanceof PersonInfo)
					return -1;
				else
					throw new Error();
			}

			@Override
			public boolean update(Transaction transaction, Person person) throws SignatureVerifyException
			{
				return false;
			}

		}

		private static class SignedPersonInfo extends PersonInfo
		{
			private final String nick;
			private final String name;
			private final String email;
			private final Date signatureDate;
			private final int signatureVersion;
			private final SignatureData signatureData;

			private SignedPersonInfo(String nick, String name, String email, Date signatureDate, int signatureVersion, SignatureData signatureData)
			{
				super();
				if (nick == null || signatureDate == null || signatureData == null)
					throw new IllegalArgumentException();
				this.nick = nick;
				this.name = name;
				this.email = email;
				this.signatureDate = signatureDate;
				this.signatureVersion = signatureVersion;
				this.signatureData = signatureData;
			}

			private SignedPersonInfo(Person person)
			{
				super();
				if (!person.isSigned())
					throw new IllegalArgumentException();
				this.nick = person.getNick();
				this.name = person.getName();
				this.email = person.getEmail();
				this.signatureDate = person.getSignatureDate();
				this.signatureVersion = person.getSignatureVersion();
				this.signatureData = person.getSignatureData();
			}

			@Override
			public String toString()
			{
				return "SignedPersonInfo [nick=" + nick + ", name=" + name + ", email=" + email + ", signatureDate=" + signatureDate + ", signatureData="
						+ signatureData + "]";
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + ((email == null) ? 0 : email.hashCode());
				result = prime * result + ((name == null) ? 0 : name.hashCode());
				result = prime * result + ((nick == null) ? 0 : nick.hashCode());
				result = prime * result + ((signatureData == null) ? 0 : signatureData.hashCode());
				result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				SignedPersonInfo other = (SignedPersonInfo) obj;
				if (email == null)
				{
					if (other.email != null)
						return false;
				}
				else if (!email.equals(other.email))
					return false;
				if (name == null)
				{
					if (other.name != null)
						return false;
				}
				else if (!name.equals(other.name))
					return false;
				if (nick == null)
				{
					if (other.nick != null)
						return false;
				}
				else if (!nick.equals(other.nick))
					return false;
				if (signatureData == null)
				{
					if (other.signatureData != null)
						return false;
				}
				else if (!signatureData.equals(other.signatureData))
					return false;
				if (signatureDate == null)
				{
					if (other.signatureDate != null)
						return false;
				}
				else if (!signatureDate.equals(other.signatureDate))
					return false;
				return true;
			}

			@Override
			public boolean update(Transaction transaction, Person person) throws SignatureVerifyException
			{
				if (signatureDate != null && (person.getSignatureDate() == null || signatureDate.compareTo(person.getSignatureDate()) > 0))
				{
					person.update(transaction, nick, name, email, signatureDate, signatureVersion, signatureData);
					return true;
				}
				else
					return false;
			}

			@Override
			public int compareTo(PersonInfo o)
			{
				if (o instanceof UnsignedPersonInfo)
					return +1;
				else if (o instanceof PersonInfo)
					return signatureDate.compareTo(((SignedPersonInfo) o).signatureDate);
				else
					throw new Error();
			}

		}

		private final PersonInfo personInfo;

		public Metadata(PersonResource resource, PersonInfo personInfo)
		{
			super(resource);
			this.personInfo = personInfo;
		}

		public PersonInfo getPersonInfo()
		{
			return personInfo;
		}

		public Metadata(Person person)
		{
			this(new PersonResource(person.getUuid()), PersonInfo.fromPerson(person));
		}

		@Override
		public PersonResource getResource()
		{
			return (PersonResource) super.getResource();
		}

		@Override
		public String toString()
		{
			return "[PersonResource.Metadata " + super.toString() + " personInfo=" + personInfo + "]";
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((personInfo == null) ? 0 : personInfo.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Metadata other = (Metadata) obj;
			if (personInfo == null)
			{
				if (other.personInfo != null)
					return false;
			}
			else if (!personInfo.equals(other.personInfo))
				return false;
			return true;
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends AbstractPersonResource.Metadata.SubProtocol<Metadata>
		{
			private final PersonInfo.Protocol personInfoProtocol = new PersonInfo.Protocol(0);

			public SubProtocol(int requiredVersion, PersonResource resource)
			{
				super(0, resource);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			protected PersonResource getResource()
			{
				return (PersonResource) super.getResource();
			}

			@Override
			public Metadata recv(DataInput in) throws IOException, ProtocolException
			{
				PersonInfo personInfo = personInfoProtocol.recv(in);
				return new Metadata(getResource(), personInfo);
			}

			@Override
			public void send(DataOutput out, Metadata m) throws IOException
			{
				personInfoProtocol.send(out, m.getPersonInfo());
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				personInfoProtocol.skip(in);
			}

		}

	}

}
