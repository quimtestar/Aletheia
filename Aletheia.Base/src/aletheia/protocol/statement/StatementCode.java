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
package aletheia.protocol.statement;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumInfo;

/**
 * Encoding for the subclasses for the class {@link Statement}.
 */
@ExportableEnumInfo(availableVersions = 0)
public enum StatementCode implements ByteExportableEnum<StatementCode>
{
	//@formatter:off
	_Assumption((byte)'A', Assumption.class),
	_Declaration((byte)'D', Declaration.class),
	_Specialization((byte)'S', Specialization.class),
	_Context((byte)'X', Context.class),
	_RootContext((byte)'R', RootContext.class),
	_UnfoldingContext((byte)'U', UnfoldingContext.class),
	;
	//@formatter:on

	private static final Map<Byte, StatementCode> codeMap;
	private static final Map<Class<? extends Statement>, StatementCode> classMap;

	static
	{
		codeMap = new HashMap<>();
		classMap = new HashMap<>();

		for (StatementCode statementCode : values())
		{
			if (codeMap.put(statementCode.code, statementCode) != null)
				throw new Error("Duplicate symbol code");
			for (Class<? extends Statement> clazz : statementCode.classes)
			{
				if (classMap.put(clazz, statementCode) != null)
					throw new Error("Duplicate symbol class");
			}
		}
	}

	private final byte code;
	private final Set<Class<? extends Statement>> classes;

	@SuppressWarnings("unchecked")
	private StatementCode(byte code, Class<?>... classes)
	{
		this.code = code;
		this.classes = new HashSet<>();
		for (Class<?> clazz : classes)
			this.classes.add((Class<? extends Statement>) clazz);
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	/**
	 * Mapping from byte codes to {@link StatementCode}.
	 *
	 * @return The map.
	 */
	public static Map<Byte, StatementCode> codeMap()
	{
		return Collections.unmodifiableMap(codeMap);
	}

	/**
	 * Mapping from {@link Class} to {@link StatementCode}.
	 *
	 * @return The map.
	 */
	public static Map<Class<? extends Statement>, StatementCode> classMap()
	{
		return Collections.unmodifiableMap(classMap);
	}

}
