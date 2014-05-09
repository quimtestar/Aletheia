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
package aletheia.protocol.primitive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;

/**
 * {@link Protocol} for UUID data. A {@link LongProtocol} is used to place first
 * the most significant bits and then the least significant beats of the UUID.
 */
@ProtocolInfo(availableVersions = 0)
public class UUIDProtocol extends Protocol<UUID>
{
	private final LongProtocol longProtocol;

	public UUIDProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(UUIDProtocol.class, requiredVersion);
		this.longProtocol = new LongProtocol(0);
	}

	@Override
	public void send(DataOutput out, UUID uuid) throws IOException
	{
		longProtocol.send(out, uuid.getMostSignificantBits());
		longProtocol.send(out, uuid.getLeastSignificantBits());
	}

	@Override
	public UUID recv(DataInput in) throws IOException
	{
		long msb = longProtocol.recv(in);
		long lsb = longProtocol.recv(in);
		return new UUID(msb, lsb);
	}

	@Override
	public void skip(DataInput in) throws IOException
	{
		longProtocol.skip(in);
		longProtocol.skip(in);
	}

}
