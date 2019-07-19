/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.protocol.authority;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import aletheia.model.authority.Person;
import aletheia.model.authority.Person.PersonCreationException;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.SignatureVerifyException;
import aletheia.model.security.SignatureData;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.security.SignatureDataProtocol;

@ProtocolInfo(availableVersions = 0)
public class PersonProtocol extends PersistentExportableProtocol<Person>
{
	private final SignatoryProtocol signatoryProtocol;
	private final StringProtocol stringProtocol;
	private final NullableProtocol<String> nullableStringProtocol;
	private final DateProtocol dateProtocol;
	private final IntegerProtocol integerProtocol;
	private final SignatureDataProtocol signatureDataProtocol;

	public PersonProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(PersonProtocol.class, requiredVersion);
		this.signatoryProtocol = new SignatoryProtocol(0, persistenceManager, transaction);
		this.stringProtocol = new StringProtocol(0);
		this.nullableStringProtocol = new NullableProtocol<>(0, stringProtocol);
		this.dateProtocol = new DateProtocol(0);
		this.integerProtocol = new IntegerProtocol(0);
		this.signatureDataProtocol = new SignatureDataProtocol(0);

	}

	@Override
	public void send(DataOutput out, Person person) throws IOException
	{
		signatoryProtocol.send(out, person.getSignatory(getTransaction()));
		stringProtocol.send(out, person.getNick());
		nullableStringProtocol.send(out, person.getName());
		nullableStringProtocol.send(out, person.getEmail());
		dateProtocol.send(out, person.getSignatureDate());
		integerProtocol.send(out, person.getSignatureVersion());
		signatureDataProtocol.send(out, person.getSignatureData());
	}

	@Override
	public Person recv(DataInput in) throws IOException, ProtocolException
	{
		Signatory signatory = signatoryProtocol.recv(in);
		String nick = stringProtocol.recv(in);
		String name = nullableStringProtocol.recv(in);
		String email = nullableStringProtocol.recv(in);
		Date signatureDate = dateProtocol.recv(in);
		int signatureVersion = integerProtocol.recv(in);
		SignatureData signatureData = signatureDataProtocol.recv(in);
		Person old = getPersistenceManager().getPerson(getTransaction(), signatory.getUuid());
		if (old == null)
		{
			try
			{
				return Person.create(getPersistenceManager(), getTransaction(), signatory, nick, name, email, signatureDate, signatureVersion, signatureData);
			}
			catch (PersonCreationException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			try
			{
				old.update(getTransaction(), nick, name, email, signatureDate, signatureVersion, signatureData);
			}
			catch (SignatureVerifyException e)
			{
				throw new ProtocolException(e);
			}
			return old;
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		signatoryProtocol.skip(in);
		stringProtocol.skip(in);
		nullableStringProtocol.skip(in);
		nullableStringProtocol.skip(in);
		dateProtocol.skip(in);
		integerProtocol.skip(in);
		signatureDataProtocol.skip(in);
	}

}
