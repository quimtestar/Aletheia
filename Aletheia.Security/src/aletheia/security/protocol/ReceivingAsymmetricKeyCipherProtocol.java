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
package aletheia.security.protocol;

import java.io.DataOutput;
import java.io.IOException;
import java.security.PrivateKey;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class ReceivingAsymmetricKeyCipherProtocol<T> extends AsymmetricKeyCipherProtocol<T>
{
	public ReceivingAsymmetricKeyCipherProtocol(int requiredVersion, PrivateKey privateKey, Protocol<T> inner)
	{
		super(0, null, null, null, privateKey, inner);
		checkVersionAvailability(ReceivingAsymmetricKeyCipherProtocol.class, requiredVersion);
	}

	@Override
	public PrivateKey getKey()
	{
		return (PrivateKey) super.getKey();
	}

	@Override
	public void send(DataOutput out, T t) throws IOException
	{
		throw new UnsupportedOperationException();
	}

}
