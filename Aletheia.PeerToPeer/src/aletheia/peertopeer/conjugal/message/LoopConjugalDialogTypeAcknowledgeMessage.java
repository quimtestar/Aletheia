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
package aletheia.peertopeer.conjugal.message;

import java.util.List;

import aletheia.peertopeer.base.message.LoopDialogTypeAcknowledgeMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.conjugal.dialog.LoopConjugalDialogType;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = LoopConjugalDialogTypeAcknowledgeMessage.SubProtocol.class)
public class LoopConjugalDialogTypeAcknowledgeMessage extends LoopDialogTypeAcknowledgeMessage<LoopConjugalDialogType>
{

	public LoopConjugalDialogTypeAcknowledgeMessage(LoopConjugalDialogType loopDialogType)
	{
		super(loopDialogType);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends LoopDialogTypeAcknowledgeMessage.SubProtocol<LoopConjugalDialogType, LoopConjugalDialogTypeAcknowledgeMessage>
	{

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode, new LoopConjugalDialogType.Protocol(0));
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected LoopConjugalDialogTypeAcknowledgeMessage makeMessage(List<Object> args)
		{
			return new LoopConjugalDialogTypeAcknowledgeMessage((LoopConjugalDialogType) args.get(0));
		}
	}

}
