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
package aletheia.protocol.peertopeer.deferredmessagecontent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContentSubProtocolInfo;
import aletheia.model.peertopeer.deferredmessagecontent.PersonsDeferredMessageContent;
import aletheia.model.peertopeer.deferredmessagecontent.SignatureRequestDeferredMessageContent;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.enumerate.ShortExportableEnum;

@ExportableEnumInfo(availableVersions = 0)
public enum DeferredMessageContentCode implements ShortExportableEnum<DeferredMessageContentCode>
{
	//@formatter:off
	@SuppressWarnings("deprecation")
	_Dummy((short)0x0000, aletheia.model.peertopeer.deferredmessagecontent.DummyDeferredMessageContent.class, 0),
	_SignatureRequest((short)0x0001, SignatureRequestDeferredMessageContent.class,0),
	_Persons((short)0x0002, PersonsDeferredMessageContent.class,0),
	;
	//@formatter:on

	private static final Map<Short, DeferredMessageContentCode> codeMap;
	private static final Map<Class<? extends DeferredMessageContent>, DeferredMessageContentCode> classMap;
	private static final Map<Class<? extends DeferredMessageContent>, Set<DeferredMessageContentCode>> generalizedClassMap;

	static
	{
		codeMap = new HashMap<Short, DeferredMessageContentCode>();
		classMap = new HashMap<Class<? extends DeferredMessageContent>, DeferredMessageContentCode>();
		generalizedClassMap = new HashMap<Class<? extends DeferredMessageContent>, Set<DeferredMessageContentCode>>();

		for (DeferredMessageContentCode deferredMessageContentCode : values())
		{
			if (codeMap.put(deferredMessageContentCode.code, deferredMessageContentCode) != null)
				throw new Error("Duplicate symbol code");
			if (classMap.put(deferredMessageContentCode.clazz, deferredMessageContentCode) != null)
				throw new Error("Duplicate symbol class");
			Class<? extends DeferredMessageContent> c = deferredMessageContentCode.clazz;
			while (true)
			{
				Set<DeferredMessageContentCode> set = generalizedClassMap.get(c);
				if (set == null)
				{
					set = EnumSet.of(deferredMessageContentCode);
					generalizedClassMap.put(c, set);
				}
				else
					set.add(deferredMessageContentCode);
				Class<?> s = c.getSuperclass();
				if (!DeferredMessageContent.class.isAssignableFrom(s))
					break;
				@SuppressWarnings("unchecked")
				Class<? extends DeferredMessageContent> s2 = (Class<? extends DeferredMessageContent>) s;
				c = s2;
			}
		}
	}

	private final short code;
	private final Class<? extends DeferredMessageContent> clazz;
	private final Class<? extends DeferredMessageContent.SubProtocol<? extends DeferredMessageContent>> subProtocolClazz;
	private final int subProtocolVersion;

	private DeferredMessageContentCode(short code, Class<? extends DeferredMessageContent> clazz, int subProtocolVersion)
	{
		this.code = code;
		this.clazz = clazz;
		DeferredMessageContentSubProtocolInfo messageProtocolInfo = clazz.getAnnotation(DeferredMessageContentSubProtocolInfo.class);
		this.subProtocolClazz = messageProtocolInfo.subProtocolClass();
		this.subProtocolVersion = subProtocolVersion;
	}

	@Override
	public Short getCode(int version)
	{
		return code;
	}

	public Class<? extends DeferredMessageContent> getClazz()
	{
		return clazz;
	}

	public Class<? extends DeferredMessageContent.SubProtocol<? extends DeferredMessageContent>> getSubProtocolClazz()
	{
		return subProtocolClazz;
	}

	public int getSubProtocolVersion(int version)
	{
		return subProtocolVersion;
	}

	public static DeferredMessageContentCode codeFor(Class<? extends DeferredMessageContent> clazz)
	{
		DeferredMessageContentCode code = classMap.get(clazz);
		if (code == null)
			throw new RuntimeException("No code for class: " + clazz.getName());
		return code;
	}

	public static Set<DeferredMessageContentCode> generalizedCodesFor(Class<? extends DeferredMessageContent> clazz)
	{
		Set<DeferredMessageContentCode> codes = generalizedClassMap.get(clazz);
		if (codes == null)
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet(codes);
	}

}
