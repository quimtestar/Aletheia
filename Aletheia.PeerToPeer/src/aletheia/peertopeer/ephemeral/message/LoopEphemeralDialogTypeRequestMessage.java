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

import java.util.List;

import aletheia.peertopeer.base.message.LoopDialogTypeRequestMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.ephemeral.dialog.LoopEphemeralDialogType;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = LoopEphemeralDialogTypeRequestMessage.SubProtocol.class)
public class LoopEphemeralDialogTypeRequestMessage extends LoopDialogTypeRequestMessage<LoopEphemeralDialogType>
{
	public LoopEphemeralDialogTypeRequestMessage(LoopEphemeralDialogType loopDialogType, boolean resolver)
	{
		super(loopDialogType, resolver);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends LoopDialogTypeRequestMessage.SubProtocol<LoopEphemeralDialogType, LoopEphemeralDialogTypeRequestMessage>
	{
		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode, new LoopEphemeralDialogType.Protocol(0));
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected LoopEphemeralDialogTypeRequestMessage makeMessage(List<Object> args)
		{
			return new LoopEphemeralDialogTypeRequestMessage((LoopEphemeralDialogType) args.get(0), (boolean) args.get(1));
		}

	}

}
