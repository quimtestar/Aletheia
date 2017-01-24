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
package aletheia.utilities.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A {@link NonBlockingSocketChannelStream} {@link OutputStream}.
 *
 * @author Quim Testar
 */
public class NonBlockingSocketChannelOutputStream extends OutputStream implements NonBlockingSocketChannelStream
{
	private final SocketChannel socketChannel;
	private final Selector selector;
	private final SelectionKey selectionKey;
	private long timeout;
	private boolean interrupted;

	public NonBlockingSocketChannelOutputStream(SocketChannel socketChannel, long timeout) throws IOException
	{
		this.socketChannel = socketChannel;
		this.selector = Selector.open();
		this.selectionKey = this.socketChannel.register(selector, SelectionKey.OP_WRITE);
		this.timeout = timeout;
		this.interrupted = false;
	}

	public NonBlockingSocketChannelOutputStream(SocketChannel socketChannel) throws IOException
	{
		this(socketChannel, 0);
	}

	@Override
	public synchronized void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	@Override
	public long getTimeout()
	{
		return timeout;
	}

	@Override
	public void write(int b) throws IOException, TimeoutException, InterruptedException
	{
		write(new byte[]
		{ (byte) b });
	}

	@Override
	public void write(byte[] b) throws IOException, TimeoutException, InterruptedException
	{
		write(b, 0, b.length);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException, TimeoutException, InterruptedException
	{
		synchronized (selector)
		{
			if (interrupted)
				throw new InterruptedException();
			if (!selector.isOpen() || !socketChannel.isOpen())
				throw new ClosedStreamException();
			long time0 = 0;
			if (timeout > 0)
				time0 = System.nanoTime() / 1000 / 1000;
			long time1 = time0 + timeout;
			int i = 0;
			while (i < len)
			{
				while (true)
				{
					long remaining = time1 - time0;
					if (timeout > 0 && remaining <= 0)
						throw new TimeoutException();
					selector.select(remaining);
					if (interrupted)
						throw new InterruptedException();
					if (Thread.interrupted())
						throw new InterruptedException();
					if (selector.selectedKeys().contains(selectionKey))
						break;
					if (timeout > 0)
						time0 = System.nanoTime() / 1000 / 1000;
				}
				ByteBuffer byteBuffer = ByteBuffer.wrap(b, off + i, len - i);
				i += socketChannel.write(byteBuffer);
				if (timeout > 0)
					time0 = System.nanoTime() / 1000 / 1000;
			}
		}
	}

	@Override
	public void close() throws IOException
	{
		selector.close();
	}

	@Override
	public void interrupt()
	{
		interrupted = true;
		selector.wakeup();
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}

}
