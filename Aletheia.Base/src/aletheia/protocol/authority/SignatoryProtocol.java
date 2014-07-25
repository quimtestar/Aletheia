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
package aletheia.protocol.authority;

import java.io.DataInput;
import java.security.PublicKey;
import java.util.UUID;

import aletheia.model.authority.Signatory;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class SignatoryProtocol extends AbstractSignatoryProtocol<Signatory>
{
	public SignatoryProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(SignatoryProtocol.class, requiredVersion);
	}

	@Override
	protected Signatory recv(Signatory old, UUID uuid, PublicKey publicKey, DataInput in) throws ProtocolException
	{
		if (old == null)
			return Signatory.create(getPersistenceManager(), getTransaction(), uuid, publicKey);
		else
			return old;
	}

}
