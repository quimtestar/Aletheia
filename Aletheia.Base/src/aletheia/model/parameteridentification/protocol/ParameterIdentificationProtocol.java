/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.protocol.NamespaceProtocol;
import aletheia.model.parameteridentification.CompositionParameterIdentification;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.protocol.CastProtocol;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.NullableProtocol;

@ProtocolInfo(availableVersions = 0)
public class ParameterIdentificationProtocol extends ExportableProtocol<ParameterIdentification>
{
	private final ParameterIdentificationCodeProtocol parameterIdentificationCodeProtocol;
	private final Protocol<Identifier> identifierProtocol;

	public ParameterIdentificationProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(ParameterIdentificationProtocol.class, requiredVersion);
		this.parameterIdentificationCodeProtocol = new ParameterIdentificationCodeProtocol(0);
		this.identifierProtocol = new NullableProtocol<>(0, new CastProtocol<>(0, new NamespaceProtocol(0)));
	}

	@Override
	public void send(DataOutput out, ParameterIdentification parameterIdentification) throws IOException
	{
		ParameterIdentificationCode parameterIdentificationCode = ParameterIdentificationCode.codeFor(parameterIdentification);
		parameterIdentificationCodeProtocol.send(out, parameterIdentificationCode);
		switch (parameterIdentificationCode)
		{
		case _CompositionParameterIdentification:
			sendCompositionParameterIdentification(out, (CompositionParameterIdentification) parameterIdentification);
			break;
		case _FunctionParameterIdentification:
			sendFunctionParameterIdentification(out, (FunctionParameterIdentification) parameterIdentification);
			break;
		case _NullParameterIdentification:
			break;
		default:
			throw new Error();
		}
	}

	@Override
	public ParameterIdentification recv(DataInput in) throws IOException, ProtocolException
	{
		ParameterIdentificationCode parameterIdentificationCode = parameterIdentificationCodeProtocol.recv(in);
		switch (parameterIdentificationCode)
		{
		case _CompositionParameterIdentification:
			return recvCompositionParameterIdentification(in);
		case _FunctionParameterIdentification:
			return recvFunctionParameterIdentification(in);
		case _NullParameterIdentification:
			return null;
		default:
			throw new ProtocolException();
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		ParameterIdentificationCode parameterIdentificationCode = parameterIdentificationCodeProtocol.recv(in);
		switch (parameterIdentificationCode)
		{
		case _CompositionParameterIdentification:
			skipCompositionParameterIdentification(in);
			break;
		case _FunctionParameterIdentification:
			skipFunctionParameterIdentification(in);
			break;
		case _NullParameterIdentification:
			break;
		default:
			throw new ProtocolException();
		}
	}

	private void sendCompositionParameterIdentification(DataOutput out, CompositionParameterIdentification parameterIdentification) throws IOException
	{
		send(out, parameterIdentification.getHead());
		send(out, parameterIdentification.getTail());
	}

	private CompositionParameterIdentification recvCompositionParameterIdentification(DataInput in) throws IOException, ProtocolException
	{
		CompositionParameterIdentification head = (CompositionParameterIdentification) recv(in);
		ParameterIdentification tail = recv(in);
		return CompositionParameterIdentification.make(head, tail);
	}

	private void skipCompositionParameterIdentification(DataInput in) throws IOException, ProtocolException
	{
		skip(in);
		skip(in);
	}

	private void sendFunctionParameterIdentification(DataOutput out, FunctionParameterIdentification parameterIdentification) throws IOException
	{
		identifierProtocol.send(out, parameterIdentification.getParameter());
		send(out, parameterIdentification.getDomain());
		send(out, parameterIdentification.getBody());
	}

	private FunctionParameterIdentification recvFunctionParameterIdentification(DataInput in) throws IOException, ProtocolException
	{
		Identifier parameter = identifierProtocol.recv(in);
		ParameterIdentification domain = recv(in);
		ParameterIdentification body = recv(in);
		return FunctionParameterIdentification.make(parameter, domain, body);
	}

	private void skipFunctionParameterIdentification(DataInput in) throws IOException, ProtocolException
	{
		identifierProtocol.skip(in);
		skip(in);
		skip(in);
	}

}
