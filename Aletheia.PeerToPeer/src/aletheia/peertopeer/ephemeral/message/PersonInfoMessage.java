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
package aletheia.peertopeer.ephemeral.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.security.SignatureData;
import aletheia.peertopeer.base.message.AbstractUUIDInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.security.SignatureDataProtocol;

@MessageSubProtocolInfo(subProtocolClass = PersonInfoMessage.SubProtocol.class)
public class PersonInfoMessage extends AbstractUUIDInfoMessage<PersonInfoMessage.PersonInfo>
{

	public static class PersonInfo implements Exportable
	{
		private final Date signatureDate;
		private final SignatureData signatureData;

		private PersonInfo(Date signatureDate, SignatureData signatureData)
		{
			super();
			this.signatureDate = signatureDate;
			this.signatureData = signatureData;
		}

		public PersonInfo(Person person)
		{
			super();
			this.signatureDate = person.getSignatureDate();
			this.signatureData = person.getSignatureData();
		}

		public Date getSignatureDate()
		{
			return signatureDate;
		}

		public SignatureData getSignatureData()
		{
			return signatureData;
		}

	}

	@ProtocolInfo(availableVersions = 0)
	public static class PrivatePersonInfoProtocol extends ExportableProtocol<PersonInfo>
	{
		private final DateProtocol dateProtocol;
		private final SignatureDataProtocol signatureDataProtocol;

		public PrivatePersonInfoProtocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(PrivatePersonInfoProtocol.class, requiredVersion);
			this.dateProtocol = new DateProtocol(0);
			this.signatureDataProtocol = new SignatureDataProtocol(0);
		}

		@Override
		public void send(DataOutput out, PersonInfo t) throws IOException
		{
			dateProtocol.send(out, t.getSignatureDate());
			signatureDataProtocol.send(out, t.getSignatureData());
		}

		@Override
		public PersonInfo recv(DataInput in) throws IOException, ProtocolException
		{
			Date signatureDate = dateProtocol.recv(in);
			SignatureData signatureData = signatureDataProtocol.recv(in);
			return new PersonInfo(signatureDate, signatureData);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			dateProtocol.skip(in);
			signatureDataProtocol.skip(in);
		}

	}

	public static class Entry extends AbstractUUIDInfoMessage.Entry<PersonInfo>
	{
		public Entry(Person person)
		{
			super(person.getUuid(), new PersonInfo(person));
		}
	}

	public PersonInfoMessage(Collection<? extends AbstractUUIDInfoMessage.Entry<PersonInfo>> entries)
	{
		super(entries);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDInfoMessage.SubProtocol<PersonInfo, PersonInfoMessage>
	{
		private final PrivatePersonInfoProtocol privatePersonInfoProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.privatePersonInfoProtocol = new PrivatePersonInfoProtocol(0);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, PersonInfo v) throws IOException
		{
			privatePersonInfoProtocol.send(out, v);
		}

		@Override
		protected PersonInfo recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			return privatePersonInfoProtocol.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			privatePersonInfoProtocol.skip(in);
		}

		@Override
		public PersonInfoMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new PersonInfoMessage(recvEntries(in));
		}

	}

}
