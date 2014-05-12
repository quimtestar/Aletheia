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
package aletheia.prooffinder;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Context;
import aletheia.persistence.PersistenceManager;
import aletheia.prooffinder.QueueEntry.UnsolvableQueueEntryException;

public class ProofFinder
{
	private final static Logger logger = LoggerManager.logger();

	private final PersistenceManager persistenceManager;
	private final ContextWatcher contextWatcher;
	private final CandidateFinder candidateFinder;

	private class ContextQueue
	{
		public final Context context;
		public final SubsumptionTable subsumptionTable;
		public final Queue<QueueEntry> queue;

		public ContextQueue(Context context)
		{
			super();
			this.context = context;
			this.subsumptionTable = new SubsumptionTable();
			this.queue = new PriorityQueue<QueueEntry>();
			try
			{
				QueueEntry e = new RootQueueEntry(candidateFinder, context);
				queue.add(e);
				subsumptionTable.add(e);

			}
			catch (UnsolvableQueueEntryException e)
			{
			}
		}

		public boolean iterate()
		{
			QueueEntry qe = queue.poll();
			if (qe == null)
			{
				contextDiscarded(context);
				return true;
			}
			if (qe.solved())
			{
				contextProved(context, qe.getProof());
				return true;
			}
			for (QueueEntry qe_ : qe.offspring())
			{
				if (!subsumptionTable.isSubsumed(qe_))
				{
					queue.add(qe_);
					subsumptionTable.add(qe_);
				}
			}
			return false;
		}

		public int size()
		{
			return queue.size();
		}
	}

	private final Map<Context, ContextQueue> contextQueueMap;
	private int totalSize;

	public interface Listener
	{
		public void contextProved(Context context, Proof proof);

		public void contextDiscarded(Context context);
	}

	private final Set<Listener> listeners;
	private ProofFinderThread proofFinderThread;

	private float throttle;
	private int maxTotalSize;

	public ProofFinder(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.contextWatcher = new ContextWatcher();
		this.candidateFinder = new CandidateFinder(persistenceManager, contextWatcher);
		this.contextQueueMap = new HashMap<Context, ContextQueue>();
		this.totalSize = 0;
		this.listeners = Collections.synchronizedSet(new HashSet<Listener>());
		this.proofFinderThread = null;
		this.throttle = 1.0f;
		this.maxTotalSize = 5000;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public float getThrottle()
	{
		return throttle;
	}

	public void setThrottle(float throttle)
	{
		this.throttle = throttle;
	}

	public int getMaxTotalSize()
	{
		return maxTotalSize;
	}

	public void setMaxTotalSize(int maxTotalSize)
	{
		this.maxTotalSize = maxTotalSize;
	}

	public void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	private void contextProved(Context context, Proof proof)
	{
		synchronized (listeners)
		{
			for (Listener l : listeners)
				l.contextProved(context, proof);
		}
	}

	private void contextDiscarded(Context context)
	{
		synchronized (listeners)
		{
			for (Listener l : listeners)
				l.contextDiscarded(context);
		}
	}

	public synchronized boolean addToProvingPool(Context context)
	{
		if (!contextQueueMap.containsKey(context))
		{
			ContextQueue cq = new ContextQueue(context);
			contextQueueMap.put(context, cq);
			if (proofFinderThread == null)
			{
				proofFinderThread = new ProofFinderThread();
				proofFinderThread.start();
			}
			totalSize += cq.size();
			notify();
			return true;
		}
		return false;
	}

	public synchronized void waitForFinish() throws InterruptedException
	{
		while (!contextQueueMap.isEmpty())
			wait();
	}

	private synchronized void iterate() throws InterruptedException
	{
		while (contextQueueMap.isEmpty())
			wait();
		int totalSize_ = 0;
		int maxSize = Integer.MIN_VALUE;
		Map.Entry<Context, ContextQueue> maxEntry = null;
		Iterator<Map.Entry<Context, ContextQueue>> iterator = contextQueueMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<Context, ContextQueue> e = iterator.next();
			if (e.getValue().iterate())
			{
				iterator.remove();
				notify();
			}
			else
			{
				int s = e.getValue().size();
				totalSize_ += s;
				if (s > maxSize)
				{
					maxSize = s;
					maxEntry = e;
				}
			}

		}

		totalSize = totalSize_;

		if (totalSize > maxTotalSize)
		{
			contextQueueMap.remove(maxEntry.getKey());
			notify();
			contextDiscarded(maxEntry.getKey());
			totalSize -= maxSize;
		}
		logger.debug("elems: " + contextQueueMap.size() + "   totalSize: " + totalSize);
	}

	private synchronized void discardContextQueue(ContextQueue contextQueue)
	{
		notify();
		contextDiscarded(contextQueue.context);
		totalSize -= contextQueue.size();
	}

	public synchronized void discardAll()
	{
		Iterator<ContextQueue> iterator = contextQueueMap.values().iterator();
		while (iterator.hasNext())
		{
			discardContextQueue(iterator.next());
			iterator.remove();
		}
	}

	private synchronized boolean endLoop()
	{
		if (contextQueueMap.isEmpty())
		{
			proofFinderThread = null;
			return true;
		}
		else
			return false;
	}

	private class ProofFinderThread extends Thread
	{
		private boolean halt;

		private ProofFinderThread()
		{
			super("ProofFinderThread");
			this.halt = false;
		}

		public void shutdown() throws InterruptedException
		{
			this.halt = true;
			interrupt();
			join();
		}

		@Override
		public void run()
		{
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			long lastRealTime = System.nanoTime();
			long lastCpuTime = threadMXBean.getCurrentThreadCpuTime();
			while (!halt && !endLoop())
			{
				try
				{
					iterate();
					long realTime = System.nanoTime();
					long cpuTime = threadMXBean.getCurrentThreadCpuTime();
					long wait = (long) ((cpuTime - lastCpuTime) / throttle) - (realTime - lastRealTime);
					if (wait > 0)
						Thread.sleep(wait / 1000000, (int) (wait % 1000000));
					lastRealTime = realTime;
					lastCpuTime = cpuTime;
				}
				catch (InterruptedException e)
				{
				}
			}

		}

	}

	private synchronized void shutdownProofFinderThread() throws InterruptedException
	{
		if (proofFinderThread != null)
			proofFinderThread.shutdown();
		proofFinderThread = null;
	}

	public void shutdown() throws InterruptedException
	{
		shutdownProofFinderThread();
		contextWatcher.shutdown();
		candidateFinder.shutdown();
	}

	public void clearCache()
	{
		candidateFinder.clearCache();
	}

}
