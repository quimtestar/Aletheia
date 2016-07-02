/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.model.peertopeer.deferredmessagecontent;

import java.io.DataInput;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;

import aletheia.model.authority.Person;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.authority.PersonProtocol;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.peertopeer.deferredmessagecontent.DeferredMessageContentCode;

@DeferredMessageContentSubProtocolInfo(subProtocolClass = PersonsDeferredMessageContent.SubProtocol.class)
public class PersonsDeferredMessageContent extends CipheredDeferredMessageContent<Collection<Person>>
{
	private static final long serialVersionUID = -1433679118439963148L;

	private static CollectionProtocol<Person> makeProtocol(int version, PersistenceManager persistenceManager, Transaction transaction)
	{
		if (version != 0)
			throw new RuntimeException();
		return new CollectionProtocol<>(0, new PersonProtocol(0, persistenceManager, transaction));
	}

	private final static int version = 0;

	public PersonsDeferredMessageContent(PersistenceManager persistenceManager, Transaction transaction, PublicKey publicKey, Collection<Person> persons)
	{
		super(version, makeProtocol(version, persistenceManager, transaction), publicKey, persons);
	}

	public PersonsDeferredMessageContent(int version, byte[] cipher)
	{
		super(version, cipher);
	}

	public Collection<Person> persons(PersistenceManager persistenceManager, Transaction transaction, PrivateKey privateKey) throws DecipherException
	{
		if (getVersion() != 0)
			throw new VersionDecipherException();
		return decipher(makeProtocol(getVersion(), persistenceManager, transaction), privateKey);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends CipheredDeferredMessageContent.SubProtocol<Collection<Person>>
	{
		public SubProtocol(int requiredVersion, DeferredMessageContentCode code)
		{
			super(0, code);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected CipheredDeferredMessageContent<Collection<Person>> recv(int version, byte[] cipher, DataInput in)
		{
			return new PersonsDeferredMessageContent(version, cipher);
		}

	}

}
