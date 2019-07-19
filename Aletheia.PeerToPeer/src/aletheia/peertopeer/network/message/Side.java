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
package aletheia.peertopeer.network.message;

import aletheia.peertopeer.network.message.BeltConnectMessage.SubProtocol;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;

@ExportableEnumInfo(availableVersions = 0)
public enum Side implements ByteExportableEnum<Side>
{
	Left((byte) 0), Right((byte) 1),;

	private final byte code;

	private Side(byte code)
	{
		this.code = code;
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ByteExportableEnumProtocol<Side>
	{

		public Protocol(int requiredVersion)
		{
			super(0, Side.class, 0);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}
	}
}
