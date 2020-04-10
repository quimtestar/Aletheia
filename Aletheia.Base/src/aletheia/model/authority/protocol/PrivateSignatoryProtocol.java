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
package aletheia.model.authority.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import aletheia.model.authority.PrivateSignatory;
import aletheia.model.authority.PrivateSignatory.KeysDontMatchException;
import aletheia.model.authority.Signatory;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.security.PrivateKeyProtocol;

@ProtocolInfo(availableVersions = 0)
public class PrivateSignatoryProtocol extends AbstractSignatoryProtocol<PrivateSignatory>
{
	private final StringProtocol stringProtocol;
	private final PrivateKeyProtocol privateKeyProtocol;

	public PrivateSignatoryProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(PrivateSignatoryProtocol.class, requiredVersion);
		this.stringProtocol = new StringProtocol(0);
		this.privateKeyProtocol = new PrivateKeyProtocol(0);
	}

	@Override
	protected PrivateSignatory recv(Signatory old, UUID uuid, PublicKey publicKey, DataInput in) throws ProtocolException, IOException
	{
		String signatureAlgorithm = stringProtocol.recv(in);
		PrivateKey privateKey = privateKeyProtocol.recv(in);
		if (old == null || !(old instanceof PrivateSignatory))
		{
			try
			{
				return PrivateSignatory.create(getPersistenceManager(), getTransaction(), uuid, signatureAlgorithm, publicKey, privateKey);
			}
			catch (KeysDontMatchException e)
			{
				throw new ProtocolException(e);
			}
		}
		else
		{
			PrivateSignatory privateSignatory = (PrivateSignatory) old;
			if (!privateSignatory.getSignatureAlgorithm().equals(signatureAlgorithm))
				throw new ProtocolException();
			if (!privateSignatory.getPrivateKey().equals(privateKey))
				throw new ProtocolException();
			return privateSignatory;
		}
	}

	@Override
	public void send(DataOutput out, PrivateSignatory s) throws IOException
	{
		super.send(out, s);
		stringProtocol.send(out, s.getSignatureAlgorithm());
		privateKeyProtocol.send(out, s.getPrivateKey());
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		super.skip(in);
		stringProtocol.skip(in);
		privateKeyProtocol.skip(in);
	}

}
