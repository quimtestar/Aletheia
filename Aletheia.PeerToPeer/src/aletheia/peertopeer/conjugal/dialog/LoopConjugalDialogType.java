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
package aletheia.peertopeer.conjugal.dialog;

import aletheia.peertopeer.base.dialog.LoopDialogType;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ExportableEnumInfo;

@ExportableEnumInfo(availableVersions = 0)
public enum LoopConjugalDialogType implements LoopDialogType<LoopConjugalDialogType>
{
	//@formatter:off
	Valediction((byte) 0),
	UpdateBindPort((byte) 1),
	MaleOpenConnection((byte) 2),
	FemaleOpenConnection((byte) 3),
	UpdateMaleNodeUuids((byte) 4),
	;
	//@formatter:on

	private final byte code;

	private LoopConjugalDialogType(byte code)
	{
		this.code = code;
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends LoopDialogType.Protocol<LoopConjugalDialogType>
	{
		public Protocol(int requiredVersion)
		{
			super(0, LoopConjugalDialogType.class, 0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}
	}
}
