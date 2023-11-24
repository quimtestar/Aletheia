/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
package aletheia.protocol.enumerate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import aletheia.utilities.collections.ArrayAsList;
import aletheia.utilities.collections.BijectionKeyMap;
import aletheia.utilities.collections.BijectionMap;
import aletheia.utilities.collections.CacheMap;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.SoftCacheMap;

public class ExportableEnumCodeMapFactory
{
	private final CacheMap<Class<? extends ExportableEnum<?, ?>>, Map<Integer, Map<Object, ExportableEnum<?, ?>>>> map;

	public ExportableEnumCodeMapFactory()
	{
		this.map = new SoftCacheMap<>();
	}

	public class DuplicateCodeException extends Exception
	{
		private static final long serialVersionUID = -5685380262036372542L;

		private final Class<? extends ExportableEnum<?, ?>> enumClass;
		private final ExportableEnum<?, ?> e1;
		private final ExportableEnum<?, ?> e2;

		private DuplicateCodeException(Class<? extends ExportableEnum<?, ?>> enumClass, ExportableEnum<?, ?> e1, ExportableEnum<?, ?> e2)
		{
			this.enumClass = enumClass;
			this.e1 = e1;
			this.e2 = e2;
		}

		@Override
		public String getMessage()
		{
			return "Enumerate '" + enumClass.getName() + "' has duplicate code in values: '" + e1 + "','" + e2 + "'";
		}

	}

	public static abstract class ExportableEnumVersionException extends RuntimeException
	{
		private static final long serialVersionUID = 8219429564619690297L;

		@SuppressWarnings("rawtypes")
		private final Class<? extends ExportableEnum> exportableEnumClass;

		protected ExportableEnumVersionException(String message, @SuppressWarnings("rawtypes") Class<? extends ExportableEnum> exportableEnumClass)
		{
			super(message);
			this.exportableEnumClass = exportableEnumClass;
		}

		@SuppressWarnings("rawtypes")
		public Class<? extends ExportableEnum> getExportableEnumClass()
		{
			return exportableEnumClass;
		}

	}

	public static class MissingExportableEnumInfoException extends ExportableEnumVersionException
	{
		private static final long serialVersionUID = -4522709680184852547L;

		private MissingExportableEnumInfoException(@SuppressWarnings("rawtypes") Class<? extends ExportableEnum> exportableEnumclass)
		{
			super("Missing protocol info exception for class " + exportableEnumclass.getName(), exportableEnumclass);
		}

	}

	public static class VersionNotMatchingException extends ExportableEnumVersionException
	{
		private static final long serialVersionUID = -588762277325939321L;

		private final int requiredVersion;

		private VersionNotMatchingException(@SuppressWarnings("rawtypes") Class<? extends ExportableEnum> exportableEnumclass, int requiredVersion)
		{
			super("Required version " + requiredVersion + " does not match for class " + exportableEnumclass.getName(), exportableEnumclass);
			this.requiredVersion = requiredVersion;
		}

		public int getRequiredVersion()
		{
			return requiredVersion;
		}

	}

	protected static void checkExportableEnumVersionAvailability(@SuppressWarnings("rawtypes") Class<? extends ExportableEnum> exportableEnumClass,
			int requiredVersion) throws MissingExportableEnumInfoException, VersionNotMatchingException
	{
		if (!availableVersionsForEnum(exportableEnumClass).contains(requiredVersion))
			throw new VersionNotMatchingException(exportableEnumClass, requiredVersion);
	}

	protected static Collection<Integer> availableVersionsForEnum(@SuppressWarnings("rawtypes") Class<? extends ExportableEnum> exportableEnumClass)
			throws MissingExportableEnumInfoException
	{
		ExportableEnumInfo exportableEnumInfo = exportableEnumClass.getAnnotation(ExportableEnumInfo.class);
		if (exportableEnumInfo == null)
			throw new MissingExportableEnumInfoException(exportableEnumClass);
		return new ArrayAsList<>(exportableEnumInfo.availableVersions());
	}

	public synchronized <C, E extends ExportableEnum<C, ?>> Map<C, E> codeMap(Class<? extends E> enumClass, int enumVersion) throws DuplicateCodeException
	{
		checkExportableEnumVersionAvailability(enumClass, enumVersion);
		Map<Integer, Map<Object, ExportableEnum<?, ?>>> versionCodeMap = map.get(enumClass);
		if (versionCodeMap == null)
		{
			versionCodeMap = new HashMap<>();
			map.put(enumClass, versionCodeMap);
		}
		Map<Object, ExportableEnum<?, ?>> codeMap = versionCodeMap.get(enumVersion);
		if (codeMap == null)
		{
			Map<C, ExportableEnum<C, ?>> codeMap_ = new HashMap<>();
			for (E e : enumClass.getEnumConstants())
			{
				ExportableEnum<?, ?> e_ = codeMap_.put(e.getCode(enumVersion), e);
				if (e_ != null)
					throw new DuplicateCodeException(enumClass, e_, e);
			}
			codeMap = Collections.<Object, ExportableEnum<?, ?>> unmodifiableMap(codeMap_);
			versionCodeMap.put(enumVersion, codeMap);
		}
		return new BijectionMap<>(new CastBijection<>(), new BijectionKeyMap<>(new CastBijection<>(), codeMap));
	}

}
