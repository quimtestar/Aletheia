/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

public class MemoryUsageMonitor
{
	private final static NotificationEmitter emitter;

	static
	{
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		if (mbean instanceof NotificationEmitter)
			emitter = (NotificationEmitter) mbean;
		else
			emitter = null;
	}

	public static class MemoryUsageMonitorException extends Exception
	{

		private static final long serialVersionUID = -1054630689326576624L;

		private MemoryUsageMonitorException()
		{
			super();
		}

		private MemoryUsageMonitorException(String message)
		{
			super(message);
		}

	}

	private final float threshold;

	public interface Listener
	{
		public void thresholdReached(float usage);
	}

	private Set<Listener> listeners;

	private final class MyNotificationListener implements NotificationListener
	{

		@Override
		public void handleNotification(Notification notification, Object handback)
		{
			float used = 0;
			float max = 0;
			for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans())
			{
				if (pool.getType() == MemoryType.HEAP && pool.isCollectionUsageThresholdSupported())
				{
					MemoryUsage mu = pool.getCollectionUsage();
					used += mu.getUsed();
					max += mu.getMax();
				}
			}
			float usage = used / max;
			if (usage > threshold)
			{
				synchronized (MemoryUsageMonitor.this)
				{
					for (Listener l : listeners)
						l.thresholdReached(usage);
				}
			}
		}

	}

	private final MyNotificationListener myListener;

	public MemoryUsageMonitor(float threshold) throws MemoryUsageMonitorException
	{
		if (emitter == null)
			throw new MemoryUsageMonitorException("Notification emission not supported");
		this.threshold = threshold;
		this.listeners = new LinkedHashSet<Listener>();
		this.myListener = new MyNotificationListener();
		emitter.addNotificationListener(myListener, null, null);

		for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans())
		{
			if (pool.getType() == MemoryType.HEAP && pool.isCollectionUsageThresholdSupported())
			{
				MemoryUsage mu = pool.getCollectionUsage();
				pool.setCollectionUsageThreshold((long) (mu.getMax() * threshold));
			}
		}

	}

	public float getThreshold()
	{
		return threshold;
	}

	public synchronized void addListener(Listener l)
	{
		listeners.add(l);
	}

	public synchronized void removeListener(Listener l)
	{
		listeners.remove(l);
	}

	public void shutdown()
	{
		try
		{
			emitter.removeNotificationListener(myListener);
		}
		catch (ListenerNotFoundException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void finalize() throws Throwable
	{
		shutdown();
		super.finalize();
	}

}
