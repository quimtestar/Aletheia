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
package aletheia.protocol.security;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.model.security.MessageDigestData;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.StringProtocol;

@ProtocolInfo(availableVersions = 0)
public class MessageDigestDataProtocol extends Protocol<MessageDigestData>
{
	private final StringProtocol stringProtocol;
	private final ByteArrayProtocol byteArrayProtocol;

	public MessageDigestDataProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(MessageDigestDataProtocol.class, requiredVersion);
		this.stringProtocol = new StringProtocol(0);
		this.byteArrayProtocol = new ByteArrayProtocol(0);
	}

	@Override
	public void send(DataOutput out, MessageDigestData messageDigestData) throws IOException
	{
		stringProtocol.send(out, messageDigestData.getAlgorithm());
		byteArrayProtocol.send(out, messageDigestData.getEncoded());
	}

	@Override
	public MessageDigestData recv(DataInput in) throws IOException, ProtocolException
	{
		String algorithm = stringProtocol.recv(in);
		byte[] encoded = byteArrayProtocol.recv(in);
		return new MessageDigestData(algorithm, encoded);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		stringProtocol.skip(in);
		byteArrayProtocol.skip(in);
	}

}
