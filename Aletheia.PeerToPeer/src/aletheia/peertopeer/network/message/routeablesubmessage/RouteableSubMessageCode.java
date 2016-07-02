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
package aletheia.peertopeer.network.message.routeablesubmessage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aletheia.peertopeer.network.protocol.RouteableSubMessageSubProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.enumerate.ShortExportableEnum;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionMap;

@ExportableEnumInfo(availableVersions = 0)
public enum RouteableSubMessageCode implements ShortExportableEnum<RouteableSubMessageCode>
{
	//@formatter:off
	_ClosestNode((short)0x00,ClosestNodeRouteableSubMessage.class, 0),
	_ClosestNodeResponse((short)0x01,ClosestNodeResponseRouteableSubMessage.class, 0),
	_ComplementingInvitation((short)0x02,ComplementingInvitationRouteableSubMessage.class, 0),
	_BeltConnect((short)0x03,BeltConnectRouteableSubMessage.class, 0),
	_LocateResource((short)0x04,LocateResourceRouteableSubMessage.class, 0),
	_FoundLocateResourceResponse((short)0x05,FoundLocateResourceResponseRouteableSubMessage.class, 0),
	_NotFoundLocateResourceResponse((short)0x06,NotFoundLocateResourceResponseRouteableSubMessage.class, 0),
	_ResourceMetadata((short)0x07,ResourceMetadataRouteableSubMessage.class, 0),
	;
	//@formatter:on

	private static final Map<Short, RouteableSubMessageCode> codeMap;
	private static final Map<Class<? extends RouteableSubMessage>, RouteableSubMessageCode> classMap;
	private static final Map<Class<? extends RouteableSubMessage>, Set<RouteableSubMessageCode>> generalizedClassMap;

	static
	{
		codeMap = new HashMap<>();
		classMap = new HashMap<>();
		generalizedClassMap = new HashMap<>();

		for (RouteableSubMessageCode messageCode : values())
		{
			if (codeMap.put(messageCode.code, messageCode) != null)
				throw new Error("Duplicate symbol code");
			if (classMap.put(messageCode.clazz, messageCode) != null)
				throw new Error("Duplicate symbol class");
			Class<? extends RouteableSubMessage> c = messageCode.clazz;
			while (true)
			{
				Set<RouteableSubMessageCode> set = generalizedClassMap.get(c);
				if (set == null)
				{
					set = EnumSet.of(messageCode);
					generalizedClassMap.put(c, set);
				}
				else
					set.add(messageCode);
				Class<?> s = c.getSuperclass();
				if (!RouteableSubMessage.class.isAssignableFrom(s))
					break;
				@SuppressWarnings("unchecked")
				Class<? extends RouteableSubMessage> s2 = (Class<? extends RouteableSubMessage>) s;
				c = s2;
			}
		}
	}

	private final short code;
	private final Class<? extends RouteableSubMessage> clazz;
	private final Class<? extends RouteableSubMessageSubProtocol<? extends RouteableSubMessage>> subProtocolClazz;
	private final int subProtocolVersion;

	private RouteableSubMessageCode(short code, Class<? extends RouteableSubMessage> clazz, int subProtocolVersion)
	{
		this.code = code;
		this.clazz = clazz;
		RouteableSubMessageSubProtocolInfo messageProtocolInfo = clazz.getAnnotation(RouteableSubMessageSubProtocolInfo.class);
		if (messageProtocolInfo == null)
			throw new Error("Creating subProtocol for: " + this);
		this.subProtocolClazz = messageProtocolInfo.subProtocolClass();
		this.subProtocolVersion = subProtocolVersion;
	}

	@Override
	public Short getCode(int version)
	{
		return code;
	}

	public Class<? extends RouteableSubMessage> getClazz()
	{
		return clazz;
	}

	public Class<? extends RouteableSubMessageSubProtocol<? extends RouteableSubMessage>> getSubProtocolClazz()
	{
		return subProtocolClazz;
	}

	public int getSubProtocolVersion(int version)
	{
		return subProtocolVersion;
	}

	public static Map<Class<? extends RouteableSubMessage>, RouteableSubMessageCode> classMap()
	{
		return Collections.unmodifiableMap(classMap);
	}

	public static Map<Class<? extends RouteableSubMessage>, Set<RouteableSubMessageCode>> generalizedClassMap()
	{
		return Collections.unmodifiableMap(new BijectionMap<>(
				new Bijection<Set<RouteableSubMessageCode>, Set<RouteableSubMessageCode>>()
				{

					@Override
					public Set<RouteableSubMessageCode> forward(Set<RouteableSubMessageCode> input)
					{
						return Collections.unmodifiableSet(input);
					}

					@Override
					public Set<RouteableSubMessageCode> backward(Set<RouteableSubMessageCode> output)
					{
						throw new UnsupportedOperationException();
					}
				}, generalizedClassMap));
	}

	public static RouteableSubMessageCode codeFor(Class<? extends RouteableSubMessage> clazz)
	{
		RouteableSubMessageCode code = classMap.get(clazz);
		if (code == null)
			throw new RuntimeException("No code for class: " + clazz.getName());
		return code;
	}

	public static Set<RouteableSubMessageCode> generalizedCodesFor(Class<? extends RouteableSubMessage> clazz)
	{
		Set<RouteableSubMessageCode> codes = generalizedClassMap.get(clazz);
		if (codes == null)
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet(codes);
	}

}
