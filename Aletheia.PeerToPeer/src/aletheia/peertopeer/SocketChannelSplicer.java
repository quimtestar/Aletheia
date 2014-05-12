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
package aletheia.peertopeer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;

public class SocketChannelSplicer extends Thread
{
	private final static Logger logger = LoggerManager.logger();

	private final int bufferCapacity;
	private final Selector selector;

	private class Splice
	{
		private final SelectionKey selectionKey;
		private final ByteBuffer byteBuffer;
		private final SocketChannel output;

		private boolean reading;

		private Splice(SocketChannel input, SocketChannel output) throws ClosedChannelException
		{
			super();
			this.selectionKey = input.register(selector, SelectionKey.OP_READ);
			this.byteBuffer = ByteBuffer.allocateDirect(bufferCapacity);
			this.output = output;
			this.reading = true;
		}

		private boolean emptyBuffer()
		{
			return byteBuffer.position() <= 0;
		}
	}

	private final Map<SocketChannel, Splice> spliceMap;

	private boolean shutdown;

	public SocketChannelSplicer(String name, int bufferCapacity) throws IOException
	{
		super(name);
		this.bufferCapacity = bufferCapacity;
		this.selector = Selector.open();
		this.spliceMap = new HashMap<SocketChannel, Splice>();

		this.shutdown = false;
	}

	public SocketChannelSplicer(int bufferCapacity) throws IOException
	{
		this("SocketChannelSplicer", bufferCapacity);
	}

	private synchronized void semiAddSplice(SocketChannel input, SocketChannel output) throws ClosedChannelException
	{
		selector.wakeup();
		spliceMap.put(input, new Splice(input, output));
		logger.trace("semiAddSplice: input: " + input + "   output: " + output);
	}

	public synchronized void addSplice(SocketChannel channelA, SocketChannel channelB) throws ClosedChannelException
	{
		semiAddSplice(channelA, channelB);
		semiAddSplice(channelB, channelA);
	}

	private synchronized void splices() throws IOException
	{
		Set<Splice> modified = new HashSet<Splice>();
		for (SelectionKey key : selector.selectedKeys())
		{
			if (key.isValid())
			{
				SocketChannel sc = (SocketChannel) key.channel();
				Splice spliceRead = spliceMap.get(sc);
				Splice spliceWrite = spliceMap.get(spliceRead.output);
				if (spliceRead.reading && key.isValid() && (key.readyOps() & SelectionKey.OP_READ) != 0)
				{
					try
					{
						int r = sc.read(spliceRead.byteBuffer);
						if (r < 0)
							throw new EOFException();
						else if (r > 0)
						{
							modified.add(spliceRead);
							modified.add(spliceWrite);
						}
					}
					catch (IOException e)
					{
						if (!(e instanceof EOFException))
							logger.warn("Exception caught while reading from: " + sc, e);
						spliceRead.reading = false;
						modified.add(spliceRead);
						if (spliceRead.emptyBuffer() && spliceRead.output.isOpen())
							spliceRead.output.shutdownOutput();
					}
				}
				if (key.isValid() && (key.readyOps() & SelectionKey.OP_WRITE) != 0)
				{
					if (!spliceWrite.emptyBuffer())
					{
						spliceWrite.byteBuffer.flip();
						try
						{
							int w = sc.write(spliceWrite.byteBuffer);
							spliceWrite.byteBuffer.compact();
							if (w > 0)
							{
								modified.add(spliceRead);
								modified.add(spliceWrite);
							}
						}
						catch (IOException e)
						{
							logger.warn("Exception caught while writing to: " + sc, e);
							if (spliceRead.output.isOpen())
								spliceRead.output.shutdownInput();
							spliceWrite.reading = false;
							modified.add(spliceRead);
						}
					}
					if (!spliceWrite.reading && spliceWrite.emptyBuffer())
						sc.shutdownOutput();
				}
			}
		}
		for (Splice splice : modified)
		{
			if (splice.selectionKey.isValid())
			{
				int ops = 0;
				if (splice.reading && splice.byteBuffer.hasRemaining())
					ops |= SelectionKey.OP_READ;
				Splice spliceWrite = spliceMap.get(splice.output);
				if (spliceWrite.byteBuffer.position() > 0)
					ops |= SelectionKey.OP_WRITE;
				if (!splice.reading && splice.emptyBuffer() && !spliceWrite.reading && spliceWrite.emptyBuffer())
					closeSplice((SocketChannel) splice.selectionKey.channel());
				else
					splice.selectionKey.interestOps(ops);
			}
		}

	}

	private synchronized void closeSplices() throws IOException
	{
		for (SocketChannel sc : spliceMap.keySet())
			semiCloseSplice(sc);
		spliceMap.clear();
	}

	private synchronized void closeSplice(SocketChannel sc) throws IOException
	{
		semiCloseSplice(sc);
		Splice splice = spliceMap.remove(sc);
		semiCloseSplice(splice.output);
		spliceMap.remove(splice.output);
	}

	private synchronized void semiCloseSplice(SocketChannel sc) throws IOException
	{
		logger.trace("semiCloseSplice: " + sc);
		sc.close();
		Splice splice = spliceMap.get(sc);
		splice.selectionKey.cancel();
	}

	@Override
	public void run()
	{
		try
		{
			try
			{
				while (!shutdown)
				{
					selector.select();
					if (shutdown)
						break;
					splices();
				}
			}
			finally
			{
				closeSplices();
				selector.close();
			}
		}
		catch (IOException e)
		{
			logger.fatal("Exception caught", e);
			throw new RuntimeException(e);
		}
	}

	public synchronized void shutdown() throws InterruptedException
	{
		this.shutdown = true;
		selector.wakeup();
		join();
	}

	public synchronized void closeAllSocketChannels()
	{
		for (SocketChannel socketChannel : spliceMap.keySet())
		{
			try
			{
				socketChannel.close();
			}
			catch (IOException e)
			{
				logger.warn("Exception caught", e);
			}
		}
	}

}
