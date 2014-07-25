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

import aletheia.model.authority.SignatureRequest;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.authority.SignatureRequestProtocol;
import aletheia.protocol.peertopeer.deferredmessagecontent.DeferredMessageContentCode;

@DeferredMessageContentSubProtocolInfo(subProtocolClass = SignatureRequestDeferredMessageContent.SubProtocol.class)
public class SignatureRequestDeferredMessageContent extends CipheredDeferredMessageContent<SignatureRequest>
{
	private static final long serialVersionUID = 5239044733989041182L;

	public SignatureRequestDeferredMessageContent(PersistenceManager persistenceManager, Transaction transaction, PublicKey publicKey,
			SignatureRequest signatureRequest)
	{
		super(0, new SignatureRequestProtocol(0, persistenceManager, transaction), publicKey, signatureRequest);
	}

	public SignatureRequestDeferredMessageContent(int version, byte[] cipher)
	{
		super(version, cipher);
	}

	public SignatureRequest signatureRequest(PersistenceManager persistenceManager, Transaction transaction, PrivateKey privateKey) throws DecipherException
	{
		if (getVersion() != 0)
			throw new VersionDecipherException();
		return decipher(new SignatureRequestProtocol(0, persistenceManager, transaction), privateKey);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends CipheredDeferredMessageContent.SubProtocol<SignatureRequest>
	{
		public SubProtocol(int requiredVersion, DeferredMessageContentCode code)
		{
			super(0, code);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected CipheredDeferredMessageContent<SignatureRequest> recv(int version, byte[] cipher, DataInput in)
		{
			return new SignatureRequestDeferredMessageContent(version, cipher);
		}

	}

}
