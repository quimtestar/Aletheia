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
import java.io.DataOutput;
import java.io.IOException;

import aletheia.model.peertopeer.deferredmessagecontent.protocol.DeferredMessageContentCode;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;

@Deprecated
@DeferredMessageContentSubProtocolInfo(subProtocolClass = DummyDeferredMessageContent.SubProtocol.class)
public class DummyDeferredMessageContent extends DeferredMessageContent
{

	private static final long serialVersionUID = -1174071198450464808L;

	private final byte[] payload;

	public DummyDeferredMessageContent(byte[] payload)
	{
		this.payload = payload.clone();
	}

	public byte[] getPayload()
	{
		return payload.clone();
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends DeferredMessageContent.SubProtocol<DummyDeferredMessageContent>
	{
		private final ByteArrayProtocol byteArrayProtocol;

		public SubProtocol(int requiredVersion, DeferredMessageContentCode code)
		{
			super(0, code);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.byteArrayProtocol = new ByteArrayProtocol(0);
		}

		@Override
		public void send(DataOutput out, DummyDeferredMessageContent m) throws IOException
		{
			byteArrayProtocol.send(out, m.payload);
		}

		@Override
		public DummyDeferredMessageContent recv(DataInput in) throws IOException, ProtocolException
		{
			byte[] payload = byteArrayProtocol.recv(in);
			return new DummyDeferredMessageContent(payload);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			byteArrayProtocol.skip(in);
		}
	}

}
