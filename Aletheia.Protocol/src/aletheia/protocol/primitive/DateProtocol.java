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
package aletheia.protocol.primitive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

/**
 * {@link Protocol} for {@link Date} objects. The date is converted to a long
 * integer via the {@link Date#getTime()} method.
 */
@ProtocolInfo(availableVersions = 0)
public class DateProtocol extends Protocol<Date>
{

	private final LongProtocol longProtocol;

	public DateProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(DateProtocol.class, requiredVersion);
		this.longProtocol = new LongProtocol(0);
	}

	@Override
	public void send(DataOutput out, Date date) throws IOException
	{
		longProtocol.send(out, date.getTime());
	}

	@Override
	public Date recv(DataInput in) throws IOException, ProtocolException
	{
		return new Date(longProtocol.recv(in));
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		longProtocol.skip(in);
	}

}
