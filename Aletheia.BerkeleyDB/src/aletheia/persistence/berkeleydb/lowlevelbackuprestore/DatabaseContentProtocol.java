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
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DiskOrderedCursor;
import com.sleepycat.je.DiskOrderedCursorConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

@ProtocolInfo(availableVersions = 0)
class DatabaseContentProtocol extends Protocol<Void>
{
	@ExportableEnumInfo(availableVersions = 0)
	private enum EntryType implements ByteExportableEnum<EntryType>
	{
		RECORD((byte) 0), END((byte) 1), ;

		private final byte code;

		private EntryType(byte code)
		{
			this.code = code;
		}

		@Override
		public Byte getCode(int version)
		{
			return code;
		}

	}

	private final Database database;
	private final DatabaseEntryProtocol databaseEntryProtocol;
	private final ByteExportableEnumProtocol<EntryType> entryTypeProtocol;
	private final DiskOrderedCursorConfig cursorConfig;

	public DatabaseContentProtocol(int requiredVersion, Database database)
	{
		super(0);
		checkVersionAvailability(DatabaseContentProtocol.class, requiredVersion);
		this.database = database;
		this.databaseEntryProtocol = new DatabaseEntryProtocol(0);
		this.entryTypeProtocol = new ByteExportableEnumProtocol<>(0, EntryType.class, 0);
		this.cursorConfig = new DiskOrderedCursorConfig();
	}

	public void send(DataOutput out) throws IOException
	{
		DiskOrderedCursor cursor = database.openCursor(cursorConfig);
		try
		{
			while (true)
			{
				DatabaseEntry key = new DatabaseEntry();
				DatabaseEntry data = new DatabaseEntry();
				OperationStatus status = cursor.getNext(key, data, LockMode.READ_UNCOMMITTED);
				if (status == OperationStatus.NOTFOUND)
					break;
				entryTypeProtocol.send(out, EntryType.RECORD);
				databaseEntryProtocol.send(out, key);
				databaseEntryProtocol.send(out, data);
			}
			entryTypeProtocol.send(out, EntryType.END);
		}
		finally
		{
			cursor.close();
		}
	}

	@Override
	public void send(DataOutput out, Void v) throws IOException
	{
		send(out);
	}

	@Override
	public Void recv(DataInput in) throws IOException, ProtocolException
	{
		loop: while (true)
		{
			EntryType t = entryTypeProtocol.recv(in);
			switch (t)
			{
			case RECORD:
			{
				DatabaseEntry key = databaseEntryProtocol.recv(in);
				DatabaseEntry value = databaseEntryProtocol.recv(in);
				OperationStatus status = database.put(null, key, value);
				if (status != OperationStatus.SUCCESS)
					throw new ProtocolException();
				break;
			}
			case END:
				break loop;
			}
		}
		return null;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		loop: while (true)
		{
			EntryType t = entryTypeProtocol.recv(in);
			switch (t)
			{
			case RECORD:
			{
				databaseEntryProtocol.skip(in);
				databaseEntryProtocol.skip(in);
				break;
			}
			case END:
				break loop;
			}
		}
	}

}
