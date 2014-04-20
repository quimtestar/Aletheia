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
package aletheia.utilities;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;

public class AsynchronousInvoker
{
	private final static Logger logger = LoggerManager.logger();

	private final static int maxRunningThreads = 7;

	public static AsynchronousInvoker instance = new AsynchronousInvoker();

	public interface Invokable
	{
		public void invoke();
	}

	private final Queue<Invokable> queue;
	private int maxQueueSize;

	private class MyThread extends Thread
	{
		public MyThread()
		{
			super("AsynchronousInvoker Thread");
		}

		@Override
		public void run()
		{
			logger.trace("Thread start");
			while (true)
			{
				Invokable invokable = poll();
				if (invokable == null)
					break;
				invokable.invoke();
				logger.trace("Queue size (-): " + queue.size());
			}
			logger.trace("Thread stop");
		}

	}

	private int runningThreads;

	private AsynchronousInvoker()
	{
		this.queue = new ArrayDeque<>();
		this.runningThreads = 0;
		updateMaxQueueSize();
	}

	private void updateMaxQueueSize()
	{
		if (queue.size() > maxQueueSize)
			maxQueueSize = queue.size();
	}

	public synchronized void invoke(Invokable invokable)
	{
		queue.add(invokable);
		while (runningThreads < queue.size() && runningThreads < maxRunningThreads)
		{
			MyThread thread = new MyThread();
			thread.start();
			runningThreads++;
			logger.trace("Running threads (+): " + runningThreads);
		}
		updateMaxQueueSize();
		logger.trace("Queue size (+): " + queue.size() + "  (max: " + maxQueueSize + ")");
	}

	private synchronized Invokable poll()
	{
		notifyAll();
		if (queue.isEmpty())
		{
			runningThreads--;
			logger.trace("Running threads (-): " + runningThreads);
			return null;
		}
		else
			return queue.poll();
	}

	public synchronized void waitForQueueSizeLesserThan(int n) throws InterruptedException
	{
		if (n <= 0)
			throw new IllegalArgumentException();
		while (queue.size() >= n)
			wait();
	}

}
