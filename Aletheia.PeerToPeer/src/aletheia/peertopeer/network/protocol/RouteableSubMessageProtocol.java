/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer.network.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessageCode;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class RouteableSubMessageProtocol extends ExportableProtocol<RouteableSubMessage>
{
	private final RouteableSubMessageCodeProtocol routeableSubMessageCodeProtocol = new RouteableSubMessageCodeProtocol(0);
	private final Map<RouteableSubMessageCode, RouteableSubMessageSubProtocol<? extends RouteableSubMessage>> subProtocols = new EnumMap<>(
			RouteableSubMessageCode.class);

	public RouteableSubMessageProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(RouteableSubMessageProtocol.class, requiredVersion);
	}

	protected RouteableSubMessageSubProtocol<? extends RouteableSubMessage> obtainSubProtocol(RouteableSubMessageCode code, int requiredVersion)
	{
		try
		{
			Class<? extends RouteableSubMessageSubProtocol<? extends RouteableSubMessage>> subProtocolClazz = code.getSubProtocolClazz();
			Constructor<? extends RouteableSubMessageSubProtocol<? extends RouteableSubMessage>> constructor = subProtocolClazz.getConstructor(int.class,
					RouteableSubMessageCode.class);
			return constructor.newInstance(requiredVersion, code);
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			throw new Error("Creating subProtocol for: " + code.toString(), e);
		}
		finally
		{

		}
	}

	private RouteableSubMessageSubProtocol<? extends RouteableSubMessage> subProtocol(RouteableSubMessageCode code)
	{
		RouteableSubMessageSubProtocol<? extends RouteableSubMessage> subProtocol = subProtocols.get(code);
		if (subProtocol == null)
		{
			subProtocol = obtainSubProtocol(code, code.getSubProtocolVersion(routeableSubMessageCodeProtocol.getEnumVersion()));
			subProtocols.put(code, subProtocol);
		}
		return subProtocol;
	}

	@Override
	public void send(DataOutput out, RouteableSubMessage r) throws IOException
	{
		RouteableSubMessageCode code = RouteableSubMessageCode.codeFor(r.getClass());
		routeableSubMessageCodeProtocol.send(out, code);
		subProtocol(code).sendMessage(out, r);
	}

	public RouteableSubMessage recv(DataInput in, Set<RouteableSubMessageCode> codeSet) throws IOException, ProtocolException
	{
		RouteableSubMessageCode code = routeableSubMessageCodeProtocol.recv(in);
		if (codeSet != null && !codeSet.contains(code))
			throw new ProtocolException();
		return subProtocol(code).recv(in);
	}

	@Override
	public RouteableSubMessage recv(DataInput in) throws IOException, ProtocolException
	{
		return recv(in, (Set<RouteableSubMessageCode>) null);
	}

	public RouteableSubMessage recv(DataInput in, RouteableSubMessageCode code) throws IOException, ProtocolException
	{
		return recv(in, EnumSet.of(code));
	}

	@SuppressWarnings("unchecked")
	public <M extends RouteableSubMessage> M recv(DataInput in, Class<M> messageClass) throws IOException, ProtocolException
	{
		try
		{
			return (M) recv(in, RouteableSubMessageCode.generalizedClassMap().get(messageClass));
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		RouteableSubMessageCode code = routeableSubMessageCodeProtocol.recv(in);
		subProtocol(code).skip(in);

	}

	public RouteableSubMessage skipTo(DataInput in, RouteableSubMessageCode code) throws IOException, ProtocolException
	{
		while (true)
		{
			RouteableSubMessageCode code_ = routeableSubMessageCodeProtocol.recv(in);
			if (code.equals(code_))
				break;
			subProtocol(code_).skip(in);
		}
		return subProtocol(code).recv(in);
	}

	@SuppressWarnings("unchecked")
	public <M extends RouteableSubMessage> M skipTo(DataInput in, Class<M> messageClass) throws IOException, ProtocolException
	{
		try
		{
			return (M) skipTo(in, RouteableSubMessageCode.classMap().get(messageClass));
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

}
