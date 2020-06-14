/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.protocol.term;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FoldingCastTypeTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.TauTerm;
import aletheia.model.term.Term;
import aletheia.model.term.UnprojectedCastTypeTerm;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumInfo;

/**
 * Encoding for the subclasses of the class {@link Term}.
 */
@ExportableEnumInfo(availableVersions = 0)
public enum TermCode implements ByteExportableEnum<TermCode>
{
	//@formatter:off
	_TauTerm((byte)'T', TauTerm.class),
	_ParameterVariableTerm((byte)'L', ParameterVariableTerm.class),
	_IdentifiableVariableTerm((byte)'V', IdentifiableVariableTerm.class),
	_CompositionTerm((byte)'C', CompositionTerm.class),
	_FunctionTerm((byte)'F', FunctionTerm.class),
	_ProjectionTerm((byte)'P', ProjectionTerm.class),
	_ProjectedCastTypeTerm((byte)'[', ProjectedCastTypeTerm.class),
	_UnprojectedCastTypeTerm((byte)'{', UnprojectedCastTypeTerm.class),
	_FoldingCastTypeTerm((byte)'(', FoldingCastTypeTerm.class)
	;
	//@formatter:on

	private static final Map<Byte, TermCode> codeMap;
	private static final Map<Class<? extends Term>, TermCode> classMap;

	static
	{
		codeMap = new HashMap<>();
		classMap = new HashMap<>();

		for (TermCode termCode : values())
		{
			if (codeMap.put(termCode.code, termCode) != null)
				throw new Error("Duplicate symbol code");
			for (Class<? extends Term> clazz : termCode.classes)
			{
				if (classMap.put(clazz, termCode) != null)
					throw new Error("Duplicate symbol class");
			}
		}
	}

	private final byte code;
	private final Set<Class<? extends Term>> classes;

	@SuppressWarnings("unchecked")
	private TermCode(byte code, Class<?>... classes)
	{
		this.code = code;
		this.classes = new HashSet<>();
		for (Class<?> clazz : classes)
			this.classes.add((Class<? extends Term>) clazz);
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	/**
	 * Mapping from byte codes to {@link TermCode}.
	 *
	 * @return The map.
	 */
	public static Map<Byte, TermCode> codeMap()
	{
		return Collections.unmodifiableMap(codeMap);
	}

	/**
	 * Mapping from {@link Class} to {@link TermCode}.
	 *
	 * @return The map.
	 */
	public static Map<Class<? extends Term>, TermCode> classMap()
	{
		return Collections.unmodifiableMap(classMap);
	}

}
