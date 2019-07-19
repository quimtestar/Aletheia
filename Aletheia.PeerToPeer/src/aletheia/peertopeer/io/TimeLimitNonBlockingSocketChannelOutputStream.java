/*******************************************************************************
 * Copyright (c) 2014, 2017 Quim Testar.
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
package aletheia.peertopeer.io;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import aletheia.utilities.io.NonBlockingSocketChannelOutputStream;

public class TimeLimitNonBlockingSocketChannelOutputStream extends NonBlockingSocketChannelOutputStream
{
	private final long extraTime;

	private long timeLimit;

	public TimeLimitNonBlockingSocketChannelOutputStream(SocketChannel socketChannel, long extraTime) throws IOException
	{
		super(socketChannel);
		this.extraTime = extraTime;
	}

	public TimeLimitNonBlockingSocketChannelOutputStream(SocketChannel socketChannel) throws IOException
	{
		this(socketChannel, 0);
	}

	public long getExtraTime()
	{
		return extraTime;
	}

	public void setRemainingTime(long remainingTime)
	{
		if (remainingTime > 0)
			this.timeLimit = System.nanoTime() / 1000 / 1000 + remainingTime;
		else
			this.timeLimit = 0;
	}

	public long getRemainingTime()
	{
		if (timeLimit > 0)
			return timeLimit - System.nanoTime() / 1000 / 1000;
		else
			return 0;
	}

	public boolean expiredRemainingTime()
	{
		if (timeLimit > 0)
			return timeLimit < System.nanoTime() / 1000 / 1000;
		else
			return false;
	}

	public long extendRemainingTime(long extend) throws TimeoutException
	{
		if (expiredRemainingTime())
			throw new TimeoutException();
		long remainingTime = getRemainingTime();
		if (getRemainingTime() > 0)
		{
			remainingTime += extend;
			setRemainingTime(remainingTime);
		}
		return remainingTime;
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException, TimeoutException, InterruptedException
	{
		if (expiredRemainingTime())
		{
			if (extraTime > 0)
				setTimeout(extraTime);
			else
				throw new TimeoutException();
		}
		else
		{
			if (timeLimit > 0)
				setTimeout(getRemainingTime() + extraTime);
			else
				setTimeout(0);
		}
		super.write(b, off, len);
	}

}
