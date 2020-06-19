/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class ExportableEnumProtocol<C, E extends ExportableEnum<C, ?>> extends Protocol<E>
{
	private static final ExportableEnumCodeMapFactory exportableEnumCodeMapFactory = new ExportableEnumCodeMapFactory();

	private final int enumVersion;
	private final Map<C, E> codeMap;
	private final Protocol<C> codeProtocol;

	public ExportableEnumProtocol(int requiredVersion, Class<? extends E> enumClass, int enumVersion, Protocol<C> codeProtocol)
	{
		super(0);
		checkVersionAvailability(ExportableEnumProtocol.class, requiredVersion);
		this.enumVersion = enumVersion;
		try
		{
			this.codeMap = exportableEnumCodeMapFactory.codeMap(enumClass, enumVersion);
		}
		catch (ExportableEnumCodeMapFactory.DuplicateCodeException e)
		{
			throw new RuntimeException(e);
		}
		this.codeProtocol = codeProtocol;
	}

	public int getEnumVersion()
	{
		return enumVersion;
	}

	@Override
	public void send(DataOutput out, E e) throws IOException
	{
		codeProtocol.send(out, e.getCode(enumVersion));
	}

	@Override
	public E recv(DataInput in) throws IOException, ProtocolException
	{
		C code = codeProtocol.recv(in);
		E e = codeMap.get(code);
		if (e == null)
			throw new ProtocolException();
		return e;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		codeProtocol.skip(in);
	}

}
