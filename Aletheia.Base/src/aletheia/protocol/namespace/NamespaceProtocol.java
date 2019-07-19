/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.protocol.namespace;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.StringProtocol;

/**
 * {@link Protocol} for {@link Namespace}s. The data is arranged as follows:
 * <ul>
 * <li>First, the code for the specific class is placed via the
 * {@link NamespaceCodeProtocol}.</li>
 * <li>If it's a {@link NodeNamespace}:</li> <blockquote>
 * <li>Send the parent namespace recursively, in the same way described here.
 * </li>
 * <li>Send the name of this namespace, via the {@link StringProtocol}.</li>
 * </blockquote>
 * </ul>
 *
 * @see NamespaceCodeProtocol
 * @see StringProtocol
 */
@ProtocolInfo(availableVersions = 0)
public class NamespaceProtocol extends ExportableProtocol<Namespace>
{
	private final NamespaceCodeProtocol namespaceCodeProtocol;
	private final StringProtocol stringProtocol;

	public NamespaceProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(NamespaceProtocol.class, requiredVersion);
		this.namespaceCodeProtocol = new NamespaceCodeProtocol(0);
		this.stringProtocol = new StringProtocol(0);
	}

	@Override
	public void send(DataOutput out, Namespace namespace) throws IOException
	{
		Class<? extends Exportable> clazz = namespace.getClass();
		NamespaceCode namespaceCode = NamespaceCode.classMap().get(clazz);
		namespaceCodeProtocol.send(out, namespaceCode);
		switch (namespaceCode)
		{
		case _RootNamespace:
			sendRootNamespace(out, (RootNamespace) namespace);
			break;
		case _NodeNamespace:
			sendNodeNamespace(out, (NodeNamespace) namespace);
			break;
		default:
			throw new Error();
		}
	}

	@Override
	public Namespace recv(DataInput in) throws IOException, ProtocolException
	{
		NamespaceCode namespaceCode = namespaceCodeProtocol.recv(in);
		switch (namespaceCode)
		{
		case _RootNamespace:
			return recvRootNamespace(in);
		case _NodeNamespace:
			return recvNodeNamespace(in);
		default:
			throw new ProtocolException();
		}
	}

	private void sendNodeNamespace(DataOutput out, NodeNamespace nodeNamespace) throws IOException
	{
		send(out, nodeNamespace.getParent());
		stringProtocol.send(out, nodeNamespace.getName());
	}

	private Identifier recvNodeNamespace(DataInput in) throws IOException, ProtocolException
	{
		Namespace parent = recv(in);
		String name = stringProtocol.recv(in);
		try
		{
			return new Identifier(parent, name);
		}
		catch (InvalidNameException e)
		{
			throw new ProtocolException(e);
		}
	}

	private void sendRootNamespace(DataOutput out, RootNamespace rootNamespace)
	{
	}

	private RootNamespace recvRootNamespace(DataInput in)
	{
		return RootNamespace.instance;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		NamespaceCode namespaceCode = namespaceCodeProtocol.recv(in);
		switch (namespaceCode)
		{
		case _RootNamespace:
			skipRootNamespace(in);
			break;
		case _NodeNamespace:
			skipNodeNamespace(in);
			break;
		default:
			throw new ProtocolException();
		}
	}

	private void skipNodeNamespace(DataInput in) throws IOException, ProtocolException
	{
		skip(in);
		stringProtocol.skip(in);
	}

	private void skipRootNamespace(DataInput in)
	{
	}

}
