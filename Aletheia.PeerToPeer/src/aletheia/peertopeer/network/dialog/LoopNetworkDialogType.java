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
package aletheia.peertopeer.network.dialog;

import aletheia.peertopeer.base.dialog.LoopDialogType;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ExportableEnumInfo;

@ExportableEnumInfo(availableVersions = 0)
public enum LoopNetworkDialogType implements LoopDialogType<LoopNetworkDialogType>
{
	//@formatter:off
	Valediction((byte) 0),
	RouterSet((byte) 1),
	RouteableMessage((byte) 2),
	UpdateBindPort((byte) 3),
	ComplementingInvitation((byte) 4),
	BeltConnect((byte) 5),
	RouterSetNeighbour((byte) 6),
	ResourceTreeNode((byte) 7),
	BeltDisconnect((byte) 8),
	PropagateDeferredMessages((byte) 9),
	DeferredMessageQueue((byte) 10),
	PropagateDeferredMessageRemoval((byte) 11),
	UpdateRouterCumulationValue((byte) 12),
	UpdateNeighbourCumulationValue((byte) 13),
	RemoveRouterCumulationValue((byte) 14),
	RequestRouterCumulationValue((byte) 15),
	;
	//@formatter:on

	private final byte code;

	private LoopNetworkDialogType(byte code)
	{
		this.code = code;
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends LoopDialogType.Protocol<LoopNetworkDialogType>
	{
		public Protocol(int requiredVersion)
		{
			super(0, LoopNetworkDialogType.class, 0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}
	}

}
