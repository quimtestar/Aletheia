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

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;

/**
 * Singleton class that keeps a queue of tasks to be executed sequentially on
 * several threads.
 *
 * Designed basically to avoid dead-lock problems when the result of a
 * potentially locking task is not needed to keep going.
 *
 * The tasks to be executed are objects implementing the {@link Invokable}
 * interface.
 *
 * The amount of {@link #maxRunningThreads} tasks will be executed on one each
 * parallel thread (hard-coded, non-configurable). Those threads will be kept
 * alive as long there are pending tasks to execute.
 *
 * @author Quim Testar
 *
 */
public class AsynchronousInvoker
{
	private final static Logger logger = LoggerManager.instance.logger();

	/**
	 * Number of parallel tasks that might be executed in parallel.
	 */
	private final static int maxRunningThreads = 7;

	/**
	 * The singleton instance.
	 */
	public static AsynchronousInvoker instance = new AsynchronousInvoker();

	/**
	 * Tasks to be executed must implement this interface.
	 */
	public interface Invokable
	{
		public void invoke();
	}

	/**
	 * The queue.
	 */
	private final Queue<Invokable> queue;

	/**
	 * Maximum size the queue have reached so far. Only for debugging purposes.
	 */
	private int maxQueueSize;

	/**
	 * Task-executing {@link Thread} class.
	 */
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

	/**
	 * Number of running threads.
	 */
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

	/**
	 * Adds a new task to the queue.
	 */
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

	/**
	 * Polls a task from the queue.
	 */
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

	/**
	 * Puts the invoker in a wait state until the number of pending tasks is
	 * lower than <b>n</b>.
	 *
	 * @param n
	 *            Minimum size that will keep the method waiting.
	 */
	public synchronized void waitForQueueSizeLesserThan(int n) throws InterruptedException
	{
		if (n <= 0)
			throw new IllegalArgumentException();
		while (queue.size() >= n)
			wait();
	}

}
