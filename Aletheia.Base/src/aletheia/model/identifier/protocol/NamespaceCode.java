/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.model.identifier.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ExportableEnumInfo;

/**
 * Encoding for the subclasses of the class {@link Namespace}.
 */
@ExportableEnumInfo(availableVersions = 0)
public enum NamespaceCode implements ByteExportableEnum<NamespaceCode>
{
	//@formatter:off
	_RootNamespace((byte)'Y', RootNamespace.class),
	_NodeNamespace((byte)'I', NodeNamespace.class, Identifier.class),
	;
	//@formatter:on

	private static final Map<Byte, NamespaceCode> codeMap;
	private static final Map<Class<? extends Namespace>, NamespaceCode> classMap;

	static
	{
		codeMap = new HashMap<>();
		classMap = new HashMap<>();

		for (NamespaceCode namespaceCode : values())
		{
			if (codeMap.put(namespaceCode.code, namespaceCode) != null)
				throw new Error("Duplicate symbol code");
			for (Class<? extends Namespace> clazz : namespaceCode.classes)
			{
				if (classMap.put(clazz, namespaceCode) != null)
					throw new Error("Duplicate symbol class");
			}
		}
	}

	private final byte code;
	private final Set<Class<? extends Namespace>> classes;

	@SuppressWarnings("unchecked")
	private NamespaceCode(byte code, Class<?>... classes)
	{
		this.code = code;
		this.classes = new HashSet<>();
		for (Class<?> clazz : classes)
			this.classes.add((Class<? extends Namespace>) clazz);
	}

	@Override
	public Byte getCode(int version)
	{
		return code;
	}

	/**
	 * Mapping from byte codes to {@link NamespaceCode}.
	 *
	 * @return The map.
	 */
	public static Map<Byte, NamespaceCode> codeMap()
	{
		return Collections.unmodifiableMap(codeMap);
	}

	/**
	 * Mapping from {@link Class} to {@link NamespaceCode}.
	 *
	 * @return The map.
	 */
	public static Map<Class<? extends Namespace>, NamespaceCode> classMap()
	{
		return Collections.unmodifiableMap(classMap);
	}

}
