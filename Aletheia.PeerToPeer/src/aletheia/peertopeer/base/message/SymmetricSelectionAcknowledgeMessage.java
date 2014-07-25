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
package aletheia.peertopeer.base.message;

import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumProtocol;

public abstract class SymmetricSelectionAcknowledgeMessage<C, E extends ExportableEnum<C, ?>> extends SymmetricSelectionMessage<C, E>
{

	public SymmetricSelectionAcknowledgeMessage(E selection)
	{
		super(selection);
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<C, E extends ExportableEnum<C, ?>, M extends SymmetricSelectionAcknowledgeMessage<C, E>> extends
	SymmetricSelectionMessage.SubProtocol<C, E, M>
	{

		public SubProtocol(int requiredVersion, MessageCode messageCode, ExportableEnumProtocol<C, E> selectionProtocol)
		{
			super(0, messageCode, selectionProtocol);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

	}

}
