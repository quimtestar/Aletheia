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
package aletheia.peertopeer.base.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;

@MessageSubProtocolInfo(subProtocolClass = LockResponseMessage.SubProtocol.class)
public class LockResponseMessage extends LockMessage
{
	@ExportableEnumInfo(availableVersions = 0)
	public enum Response implements ByteExportableEnum<Response>
	{
		ACKNOWLEDGE((byte) 0), REFUSE((byte) 1),;

		private final byte code;

		private Response(byte code)
		{
			this.code = code;
		}

		@Override
		public Byte getCode(int version)
		{
			return code;
		}

	}

	private final Response response;

	public LockResponseMessage(Response response)
	{
		super();
		this.response = response;
	}

	public Response getResponse()
	{
		return response;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends LockMessage.SubProtocol<LockResponseMessage>
	{

		private final ByteExportableEnumProtocol<Response> responseProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.responseProtocol = new ByteExportableEnumProtocol<>(0, Response.class, 0);
		}

		@Override
		public void send(DataOutput out, LockResponseMessage m) throws IOException
		{
			responseProtocol.send(out, m.getResponse());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			responseProtocol.skip(in);
		}

		@Override
		public LockResponseMessage recv(DataInput in) throws IOException, ProtocolException
		{
			Response r = responseProtocol.recv(in);
			return new LockResponseMessage(r);
		}

	}

}
