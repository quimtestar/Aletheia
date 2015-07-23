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
package aletheia.peertopeer.base.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import aletheia.peertopeer.base.message.Message;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.UnionCollection;

@ProtocolInfo(availableVersions =
{ 0, 2 })
public abstract class MessageProtocol extends ExportableProtocol<Message>
{
	private final MessageCodeProtocol messageCodeProtocol;
	private final Map<MessageCode, MessageSubProtocol<? extends Message>> subProtocols;

	private static final int requiredMessageCodeProtocolVersion(int requiredVersion)
	{
		switch (requiredVersion)
		{
		case 0:
			return 0;
		case 2:
			return 2;
		default:
			return -1;
		}
	}

	public MessageProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(MessageProtocol.class, requiredVersion);
		this.messageCodeProtocol = new MessageCodeProtocol(requiredMessageCodeProtocolVersion(requiredVersion));
		this.subProtocols = new EnumMap<MessageCode, MessageSubProtocol<? extends Message>>(MessageCode.class);
	}

	protected abstract MessageSubProtocol<? extends Message> obtainSubProtocol(MessageCode code, int requiredVersion);

	private MessageSubProtocol<? extends Message> subProtocol(MessageCode code)
	{
		MessageSubProtocol<? extends Message> subProtocol = subProtocols.get(code);
		if (subProtocol == null)
		{
			subProtocol = obtainSubProtocol(code, code.getSubProtocolVersion(messageCodeProtocol.getEnumVersion()));
			subProtocols.put(code, subProtocol);
		}
		return subProtocol;
	}

	@Override
	public void send(DataOutput out, Message m) throws IOException
	{
		MessageCode code = MessageCode.codeFor(m.getClass());
		messageCodeProtocol.send(out, MessageCode.codeFor(m.getClass()));
		subProtocol(code).sendMessage(out, m);
	}

	public Message recv(DataInput in, Collection<MessageCode> codeSet) throws IOException, ProtocolException
	{
		MessageCode code = messageCodeProtocol.recv(in);
		if (codeSet != null && !codeSet.contains(code))
			throw new ProtocolException();
		return subProtocol(code).recv(in);
	}

	@Override
	public Message recv(DataInput in) throws IOException, ProtocolException
	{
		return recv(in, (Collection<MessageCode>) null);
	}

	public Message recv(DataInput in, MessageCode code) throws IOException, ProtocolException
	{
		return recv(in, EnumSet.of(code));
	}

	@SuppressWarnings("unchecked")
	public <M extends Message> M recv(DataInput in, Class<? extends M> messageClass) throws IOException, ProtocolException
	{
		try
		{
			return (M) recv(in, MessageCode.generalizedCodesFor(messageClass));
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <M extends Message> M recvClasses(DataInput in, Collection<Class<? extends M>> messageClasses) throws IOException, ProtocolException
	{
		try
		{
			return (M) recv(in,
					new UnionCollection<>(new BijectionCollection<Class<? extends M>, Set<MessageCode>>(new Bijection<Class<? extends M>, Set<MessageCode>>()
					{

						@Override
						public Set<MessageCode> forward(Class<? extends M> input)
						{
							return MessageCode.generalizedCodesFor(input);
						}

						@Override
						public Class<M> backward(Set<MessageCode> output)
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
		MessageCode code = messageCodeProtocol.recv(in);
		subProtocol(code).skip(in);
	}

	public Message skipTo(DataInput in, MessageCode code) throws IOException, ProtocolException
	{
		while (true)
		{
			MessageCode code_ = messageCodeProtocol.recv(in);
			if (code.equals(code_))
				break;
			subProtocol(code_).skip(in);
		}
		return subProtocol(code).recv(in);
	}

	@SuppressWarnings("unchecked")
	public <M extends Message> M skipTo(DataInput in, Class<M> messageClass) throws IOException, ProtocolException
	{
		try
		{
			MessageCode code = MessageCode.codeFor(messageClass);
			return (M) skipTo(in, code);
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

}
