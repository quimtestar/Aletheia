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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.UnionCollection;

@ProtocolInfo(availableVersions = 0)
public class DeferredMessageContentProtocol extends ExportableProtocol<DeferredMessageContent>
{
	private final DeferredMessageContentCodeProtocol deferredMessageContentCodeProtocol;
	private final Map<DeferredMessageContentCode, DeferredMessageContent.SubProtocol<? extends DeferredMessageContent>> subProtocols;

	public DeferredMessageContentProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(DeferredMessageContentProtocol.class, requiredVersion);
		this.deferredMessageContentCodeProtocol = new DeferredMessageContentCodeProtocol(0);
		this.subProtocols = new EnumMap<>(DeferredMessageContentCode.class);
	}

	protected DeferredMessageContent.SubProtocol<? extends DeferredMessageContent> obtainSubProtocol(DeferredMessageContentCode code, int requiredVersion)
	{
		try
		{
			Class<? extends DeferredMessageContent.SubProtocol<? extends DeferredMessageContent>> subProtocolClazz = code.getSubProtocolClazz();
			Constructor<? extends DeferredMessageContent.SubProtocol<? extends DeferredMessageContent>> constructor = subProtocolClazz.getConstructor(int.class,
					DeferredMessageContentCode.class);
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

	private DeferredMessageContent.SubProtocol<? extends DeferredMessageContent> subProtocol(DeferredMessageContentCode code)
	{
		DeferredMessageContent.SubProtocol<? extends DeferredMessageContent> subProtocol = subProtocols.get(code);
		if (subProtocol == null)
		{
			subProtocol = obtainSubProtocol(code, code.getSubProtocolVersion(deferredMessageContentCodeProtocol.getEnumVersion()));
			subProtocols.put(code, subProtocol);
		}
		return subProtocol;
	}

	@Override
	public void send(DataOutput out, DeferredMessageContent m) throws IOException
	{
		DeferredMessageContentCode code = DeferredMessageContentCode.codeFor(m.getClass());
		deferredMessageContentCodeProtocol.send(out, DeferredMessageContentCode.codeFor(m.getClass()));
		subProtocol(code).sendDeferredMessageContent(out, m);
	}

	public DeferredMessageContent recv(DataInput in, Collection<DeferredMessageContentCode> codeSet) throws IOException, ProtocolException
	{
		DeferredMessageContentCode code = deferredMessageContentCodeProtocol.recv(in);
		if (codeSet != null && !codeSet.contains(code))
			throw new ProtocolException();
		return subProtocol(code).recv(in);
	}

	@Override
	public DeferredMessageContent recv(DataInput in) throws IOException, ProtocolException
	{
		return recv(in, (Collection<DeferredMessageContentCode>) null);
	}

	public DeferredMessageContent recv(DataInput in, DeferredMessageContentCode code) throws IOException, ProtocolException
	{
		return recv(in, EnumSet.of(code));
	}

	@SuppressWarnings("unchecked")
	public <M extends DeferredMessageContent> M recv(DataInput in, Class<? extends M> messageClass) throws IOException, ProtocolException
	{
		try
		{
			return (M) recv(in, DeferredMessageContentCode.generalizedCodesFor(messageClass));
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <M extends DeferredMessageContent> M recvClasses(DataInput in, Collection<Class<? extends M>> messageClasses) throws IOException, ProtocolException
	{
		try
		{
			return (M) recv(in, new UnionCollection<>(new BijectionCollection<>(new Bijection<Class<? extends M>, Set<DeferredMessageContentCode>>()
			{

				@Override
				public Set<DeferredMessageContentCode> forward(Class<? extends M> input)
				{
					return DeferredMessageContentCode.generalizedCodesFor(input);
				}

				@Override
				public Class<M> backward(Set<DeferredMessageContentCode> output)
				{
					throw new UnsupportedOperationException();
				}
			}, messageClasses)));
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		DeferredMessageContentCode code = deferredMessageContentCodeProtocol.recv(in);
		subProtocol(code).skip(in);
	}

	public DeferredMessageContent skipTo(DataInput in, DeferredMessageContentCode code) throws IOException, ProtocolException
	{
		while (true)
		{
			DeferredMessageContentCode code_ = deferredMessageContentCodeProtocol.recv(in);
			if (code.equals(code_))
				break;
			subProtocol(code_).skip(in);
		}
		return subProtocol(code).recv(in);
	}

	@SuppressWarnings("unchecked")
	public <M extends DeferredMessageContent> M skipTo(DataInput in, Class<M> messageClass) throws IOException, ProtocolException
	{
		try
		{
			DeferredMessageContentCode code = DeferredMessageContentCode.codeFor(messageClass);
			return (M) skipTo(in, code);
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

}
