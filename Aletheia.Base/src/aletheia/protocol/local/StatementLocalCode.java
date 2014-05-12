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
package aletheia.protocol.local;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumInfo;

/**
 * Encoding for the subclasses for the class {@link StatementLocal}.
 */
@ExportableEnumInfo(availableVersions = 0)
public enum StatementLocalCode implements ByteExportableEnum<StatementLocalCode>
{
	//@formatter:off
	_StatementLocal((byte)'S', StatementLocal.class), 
	_ContextLocal((byte)'C', ContextLocal.class, RootContextLocal.class), 
	;
	//@formatter:on

	private static final Map<Byte, StatementLocalCode> codeMap;
	private static final Map<Class<? extends StatementLocal>, StatementLocalCode> classMap;

	static
	{
		codeMap = new HashMap<Byte, StatementLocalCode>();
		classMap = new HashMap<Class<? extends StatementLocal>, StatementLocalCode>();

		for (StatementLocalCode statementCode : values())
		{
			if (codeMap.put(statementCode.code, statementCode) != null)
				throw new Error("Duplicate symbol code");
			for (Class<? extends StatementLocal> clazz : statementCode.classes)
			{
				if (classMap.put(clazz, statementCode) != null)
					throw new Error("Duplicate symbol class");
			}
		}
	}

	private final byte code;
	private final Set<Class<? extends StatementLocal>> classes;

	@SuppressWarnings("unchecked")
	private StatementLocalCode(byte code, Class<?>... classes)
	{
		this.code = code;
		this.classes = new HashSet<Class<? extends StatementLocal>>();
		for (Class<?> clazz : classes)
			this.classes.add((Class<? extends StatementLocal>) clazz);
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	/**
	 * Mapping from byte codes to {@link StatementLocalCode}.
	 * 
	 * @return The map.
	 */
	public static Map<Byte, StatementLocalCode> codeMap()
	{
		return Collections.unmodifiableMap(codeMap);
	}

	/**
	 * Mapping from {@link Class} to {@link StatementLocalCode}.
	 * 
	 * @return The map.
	 */
	public static Map<Class<? extends StatementLocal>, StatementLocalCode> classMap()
	{
		return Collections.unmodifiableMap(classMap);
	}

}
