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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import aletheia.peertopeer.base.message.Message;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.PersistentMessage;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions =
{ 0, 1 })
public class PersistentMessageProtocol extends MessageProtocol
{
	private static int requiredSuperVersion(int requiredVersion)
	{
		switch (requiredVersion)
		{
		case 0:
			return 0;
		case 1:
			return 1;
		default:
			return -1;
		}
	}

	private final PersistenceManager persistenceManager;
	private final Transaction transaction;

	public PersistentMessageProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(requiredSuperVersion(requiredVersion));
		checkVersionAvailability(PersistentMessageProtocol.class, requiredVersion);
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected Transaction getTransaction()
	{
		return transaction;
	}

	@Override
	protected MessageSubProtocol<? extends Message> obtainSubProtocol(MessageCode code, int requiredVersion)
	{
		try
		{
			Class<? extends MessageSubProtocol<? extends Message>> subProtocolClazz = code.getSubProtocolClazz();
			if (PersistentMessage.class.isAssignableFrom(code.getClazz()))
			{
				Constructor<? extends MessageSubProtocol<? extends Message>> constructor = subProtocolClazz.getConstructor(int.class, PersistenceManager.class,
						Transaction.class, MessageCode.class);
				return constructor.newInstance(requiredVersion, getPersistenceManager(), getTransaction(), code);
			}
			else
			{
				Constructor<? extends MessageSubProtocol<? extends Message>> constructor = subProtocolClazz.getConstructor(int.class, MessageCode.class);
				return constructor.newInstance(requiredVersion, code);
			}
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
