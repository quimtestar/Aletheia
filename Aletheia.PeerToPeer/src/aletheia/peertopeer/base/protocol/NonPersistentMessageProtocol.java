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
package aletheia.peertopeer.base.protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import aletheia.peertopeer.base.message.Message;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions =
{ 0, 3 })
public class NonPersistentMessageProtocol extends MessageProtocol
{

	private static int requiredSuperVersion(int requiredVersion)
	{
		switch (requiredVersion)
		{
		case 0:
			return 0;
		case 3:
			return 3;
		default:
			return -1;
		}
	}

	public NonPersistentMessageProtocol(int requiredVersion)
	{
		super(requiredSuperVersion(requiredVersion));
		checkVersionAvailability(NonPersistentMessageProtocol.class, requiredVersion);
	}

	@Override
	protected MessageSubProtocol<? extends Message> obtainSubProtocol(MessageCode code, int requiredVersion)
	{
		try
		{
			Class<? extends MessageSubProtocol<? extends Message>> subProtocolClazz = code.getSubProtocolClazz();
			Constructor<? extends MessageSubProtocol<? extends Message>> constructor = subProtocolClazz.getConstructor(int.class, MessageCode.class);
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

}
