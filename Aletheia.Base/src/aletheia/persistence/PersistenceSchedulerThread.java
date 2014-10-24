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
package aletheia.persistence;

import java.util.Date;
import java.util.PriorityQueue;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person;

public class PersistenceSchedulerThread extends Thread
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final PersistenceManager persistenceManager;

	protected abstract class Job implements Comparable<Job>
	{
		private final int interval;
		private Date nextRun;

		public Job(int interval, boolean immediateFirstRun)
		{
			this.interval = interval;
			if (!immediateFirstRun)
				updateNextRun();
		}

		public void updateNextRun()
		{
			this.nextRun = new Date(System.currentTimeMillis() + ((long) interval) * 1000);
		}

		public long remaining()
		{
			if (nextRun == null)
				return Long.MIN_VALUE;
			else
				return nextRun.getTime() - System.currentTimeMillis();
		}

		@Override
		public int compareTo(Job o)
		{
			int c;
			c = Boolean.compare(nextRun != null, o.nextRun != null);
			if (c != 0)
				return c;
			if (nextRun == null)
				return 0;
			c = nextRun.compareTo(o.nextRun);
			if (c != 0)
				return c;
			return 0;
		}

		public abstract void run();
	}

	protected class SyncJob extends Job
	{
		private final static int interval = 5 * 60;

		public SyncJob()
		{
			super(interval, false);
		}

		@Override
		public void run()
		{
			persistenceManager.sync();
		}

	}

	protected class DeleteOldNonPrivateOrphansJob extends Job
	{
		private final static int interval = 24 * 60 * 60;

		public DeleteOldNonPrivateOrphansJob()
		{
			super(interval, true);
		}

		@Override
		public void run()
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				Person.deleteOldNonPrivateOrphans(persistenceManager, transaction);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}

	}

	private final PriorityQueue<Job> queue;

	private boolean shutdown;

	public PersistenceSchedulerThread(PersistenceManager persistenceManager)
	{
		super("PersistenceSchedulerThread");
		this.setDaemon(true);
		this.persistenceManager = persistenceManager;
		this.queue = new PriorityQueue<Job>();
		this.queue.add(new SyncJob());
		this.queue.add(new DeleteOldNonPrivateOrphansJob());
		this.shutdown = false;
	}

	@Override
	public void run()
	{
		while (!shutdown)
		{
			synchronized (this)
			{
				try
				{
					while (queue.isEmpty())
						wait();
					while (!shutdown)
					{
						Job job = queue.peek();
						long remaining = job.remaining();
						if (remaining <= 0)
							break;
						wait(remaining);
					}
					if (shutdown)
						break;
					Job job = queue.poll();
					job.run();
					job.updateNextRun();
					queue.offer(job);
				}
				catch (InterruptedException e)
				{
					logger.error("Interrupted exception caught", e);
				}
			}
		}
	}

	public void shutdown() throws InterruptedException
	{
		synchronized (this)
		{
			shutdown = true;
			notifyAll();
		}
		join();
	}

}
