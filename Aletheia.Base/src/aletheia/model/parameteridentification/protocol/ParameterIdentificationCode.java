/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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
package aletheia.model.parameteridentification.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.parameteridentification.CompositionParameterIdentification;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumInfo;

@ExportableEnumInfo(availableVersions = 0)
public enum ParameterIdentificationCode implements ByteExportableEnum<ParameterIdentificationCode>
{
	//@formatter:off
	_CompositionParameterIdentification((byte)'C', CompositionParameterIdentification.class),
	_FunctionParameterIdentification((byte)'F', FunctionParameterIdentification.class),
	_NullParameterIdentification((byte)'N'),
	;
	//@formatter:on

	private static final Map<Byte, ParameterIdentificationCode> codeMap;
	private static final Map<Class<? extends ParameterIdentification>, ParameterIdentificationCode> classMap;

	static
	{
		codeMap = new HashMap<>();
		classMap = new HashMap<>();

		for (ParameterIdentificationCode namespaceCode : values())
		{
			if (codeMap.put(namespaceCode.code, namespaceCode) != null)
				throw new Error("Duplicate symbol code");
			for (Class<? extends ParameterIdentification> clazz : namespaceCode.classes)
			{
				if (classMap.put(clazz, namespaceCode) != null)
					throw new Error("Duplicate symbol class");
			}
		}
	}

	private final byte code;
	private final Set<Class<? extends ParameterIdentification>> classes;

	@SuppressWarnings("unchecked")
	private ParameterIdentificationCode(byte code, Class<?>... classes)
	{
		this.code = code;
		this.classes = new HashSet<>();
		for (Class<?> clazz : classes)
			this.classes.add((Class<? extends ParameterIdentification>) clazz);
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	public static ParameterIdentificationCode codeFor(ParameterIdentification parameterIdentification)
	{
		if (parameterIdentification == null)
			return ParameterIdentificationCode._NullParameterIdentification;
		else
			return classMap.get(parameterIdentification.getClass());
	}

}
