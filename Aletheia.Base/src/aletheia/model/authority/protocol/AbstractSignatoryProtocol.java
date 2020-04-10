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
import java.security.PublicKey;
import java.util.UUID;

import aletheia.model.authority.Signatory;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.protocol.security.PublicKeyProtocol;

@ProtocolInfo(availableVersions = 0)
public abstract class AbstractSignatoryProtocol<S extends Signatory> extends PersistentExportableProtocol<S>
{
	private final UUIDProtocol uuidProtocol;
	private final PublicKeyProtocol publicKeyProtocol;

	public AbstractSignatoryProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(AbstractSignatoryProtocol.class, requiredVersion);
		this.uuidProtocol = new UUIDProtocol(0);
		this.publicKeyProtocol = new PublicKeyProtocol(0);
	}

	@Override
	public void send(DataOutput out, S s) throws IOException
	{
		uuidProtocol.send(out, s.getUuid());
		publicKeyProtocol.send(out, s.getPublicKey());
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		publicKeyProtocol.skip(in);
	}

	protected abstract S recv(Signatory old, UUID uuid, PublicKey publicKey, DataInput in) throws ProtocolException, IOException;

	@Override
	public S recv(DataInput in) throws IOException, ProtocolException
	{
		UUID uuid = uuidProtocol.recv(in);
		PublicKey publicKey = publicKeyProtocol.recv(in);
		Signatory old = getPersistenceManager().getSignatory(getTransaction(), uuid);
		if (old != null)
		{
			if (!old.getPublicKey().equals(publicKey))
				throw new ProtocolException();
		}
		return recv(old, uuid, publicKey, in);
	}

}
