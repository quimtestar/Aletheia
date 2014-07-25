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

import aletheia.model.authority.PackedSignatureRequest;
import aletheia.model.authority.SignatureRequest;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.PersistentMessage;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.authority.SignatureRequestProtocol;

@MessageSubProtocolInfo(subProtocolClass = SignatureRequestMessage.SubProtocol.class)
public class SignatureRequestMessage extends PersistentMessage
{
	private final SignatureRequest signatureRequest;

	public SignatureRequestMessage(SignatureRequest signatureRequest)
	{
		this.signatureRequest = signatureRequest;
	}

	public SignatureRequest getSignatureRequest()
	{
		return signatureRequest;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends PersistentMessage.SubProtocol<SignatureRequestMessage>
	{
		private final SignatureRequestProtocol signatureRequestProtocol;

		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.signatureRequestProtocol = new SignatureRequestProtocol(0, persistenceManager, transaction);
		}

		@Override
		public void send(DataOutput out, SignatureRequestMessage m) throws IOException
		{
			signatureRequestProtocol.send(out, m.getSignatureRequest());
		}

		public class CollisionPackedSignatureRequestProtocolException extends ProtocolException
		{
			private static final long serialVersionUID = -3513670308920759657L;

			private CollisionPackedSignatureRequestProtocolException(SignatureRequestProtocol.CollisionPackedSignatureRequestProtocolException cause)
			{
				super(cause);
			}

			@Override
			public synchronized SignatureRequestProtocol.CollisionPackedSignatureRequestProtocolException getCause()
			{
				return (SignatureRequestProtocol.CollisionPackedSignatureRequestProtocolException) super.getCause();
			}
		}

		@Override
		public SignatureRequestMessage recv(DataInput in) throws IOException, ProtocolException
		{
			try
			{
				PackedSignatureRequest packedSignatureRequest = signatureRequestProtocol.recv(in);
				return new SignatureRequestMessage(packedSignatureRequest);
			}
			catch (SignatureRequestProtocol.CollisionPackedSignatureRequestProtocolException e)
			{
				throw new CollisionPackedSignatureRequestProtocolException(e);
			}
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			signatureRequestProtocol.skip(in);
		}
	}

}
