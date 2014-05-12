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
package aletheia.peertopeer.base;

import java.util.HashMap;
import java.util.Map;

import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.peertopeer.conjugal.phase.ConjugalPhase;
import aletheia.peertopeer.ephemeral.phase.EphemeralPhase;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.peertopeer.spliced.phase.SplicedPhase;
import aletheia.peertopeer.statement.phase.StatementPhase;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;

@ExportableEnumInfo(availableVersions = 0)
public enum SubRootPhaseType implements ByteExportableEnum<SubRootPhaseType>
{
	//@formatter:off
	Statement((byte) 0,StatementPhase.class),
	Network((byte) 1,NetworkPhase.class),
	Ephemeral((byte) 2,EphemeralPhase.class),
	Conjugal((byte) 3,ConjugalPhase.class),
	Spliced((byte) 4,SplicedPhase.class),
	;
	//@formatter:on

	private final byte code;
	private final Class<? extends SubRootPhase> subRootPhaseClass;

	private final static Map<Class<? extends SubRootPhase>, SubRootPhaseType> classTypeMap;
	static
	{
		classTypeMap = new HashMap<Class<? extends SubRootPhase>, SubRootPhaseType>();
		for (SubRootPhaseType type : values())
		{
			SubRootPhaseType old = classTypeMap.put(type.getSubRootPhaseClass(), type);
			if (old != null)
				throw new Error();
		}
	}

	public static SubRootPhaseType classType(Class<? extends SubRootPhase> subRootPhaseClass)
	{
		return classTypeMap.get(subRootPhaseClass);
	}

	public static SubRootPhaseType objectType(SubRootPhase subRootPhase)
	{
		return classType(subRootPhase.getClass());
	}

	private SubRootPhaseType(byte code, Class<? extends SubRootPhase> subRootPhaseClass)
	{
		this.code = code;
		this.subRootPhaseClass = subRootPhaseClass;
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	public Class<? extends SubRootPhase> getSubRootPhaseClass()
	{
		return subRootPhaseClass;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ByteExportableEnumProtocol<SubRootPhaseType>
	{
		public Protocol(int requiredVersion)
		{
			super(0, SubRootPhaseType.class, 0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}
	}
}
