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
package aletheia.persistence.berkeleydb.lowlevelbackuprestore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;

import com.sleepycat.je.DatabaseEntry;

@ProtocolInfo(availableVersions = 0)
class DatabaseEntryProtocol extends Protocol<DatabaseEntry>
{
	private final ByteArrayProtocol byteArrayProtocol;

	public DatabaseEntryProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(DatabaseEntryProtocol.class, requiredVersion);
		this.byteArrayProtocol = new ByteArrayProtocol(0);
	}

	@Override
	public void send(DataOutput out, DatabaseEntry de) throws IOException
	{
		byteArrayProtocol.send(out, de.getData(), de.getOffset(), de.getSize());
	}

	@Override
	public DatabaseEntry recv(DataInput in) throws IOException, ProtocolException
	{
		byte[] data = byteArrayProtocol.recv(in);
		return new DatabaseEntry(data);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		byteArrayProtocol.skip(in);
	}
}
