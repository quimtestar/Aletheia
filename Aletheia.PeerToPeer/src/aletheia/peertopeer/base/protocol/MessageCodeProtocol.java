/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer.base.protocol;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ShortExportableEnumProtocol;

@ProtocolInfo(availableVersions =
{ 0, 3 })
public class MessageCodeProtocol extends ShortExportableEnumProtocol<MessageCode>
{

	private static final int requiredEnumVersion(int requiredVersion)
	{
		switch (requiredVersion)
		{
		case 0:
			return 0;
		case 3:
			return 3;
		default:
			return -1;
		}
	}

	public MessageCodeProtocol(int requiredVersion)
	{
		super(0, MessageCode.class, requiredEnumVersion(requiredVersion));
		checkVersionAvailability(MessageCodeProtocol.class, requiredVersion);
	}

}
