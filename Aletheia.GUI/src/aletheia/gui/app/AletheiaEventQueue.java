/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.gui.app;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

public class AletheiaEventQueue extends EventQueue
{
	public interface ThrowableProcessor<C extends Component, T extends Throwable>
	{
		public boolean processThrowable(C component, T e);
	}

	private final WeakHashMap<Component, Map<Class<Throwable>, Set<ThrowableProcessor<Component, Throwable>>>> processorMap;

	public AletheiaEventQueue()
	{
		this.processorMap = new WeakHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public synchronized <C extends Component, T extends Throwable> boolean addThrowableProcessor(C component, Class<? extends T> throwableClass,
			ThrowableProcessor<? extends C, ? extends T> processor)
	{
		Map<Class<Throwable>, Set<ThrowableProcessor<Component, Throwable>>> throwableMap = processorMap.get(component);
		if (throwableMap == null)
		{
			throwableMap = new HashMap<>();
			processorMap.put(component, throwableMap);
		}
		Set<ThrowableProcessor<Component, Throwable>> processors = throwableMap.get(throwableClass);
		if (processors == null)
		{
			processors = new HashSet<>();
			throwableMap.put((Class<Throwable>) throwableClass, processors);
		}
		return processors.add((ThrowableProcessor<Component, Throwable>) processor);
	}

	public synchronized <C extends Component, T extends Throwable> boolean removeThrowableProcessor(C component, Class<? extends T> throwableClass,
			ThrowableProcessor<? extends C, ? extends T> processor)
	{
		Map<Class<Throwable>, Set<ThrowableProcessor<Component, Throwable>>> throwableMap = processorMap.get(component);
		if (throwableMap == null)
			return false;
		Set<ThrowableProcessor<Component, Throwable>> processors = throwableMap.get(throwableClass);
		if (processors == null)
			return false;
		try
		{
			return processors.remove(processor);
		}
		finally
		{
			if (processors.isEmpty())
			{
				throwableMap.remove(throwableClass);
				if (throwableMap.isEmpty())
					processorMap.remove(component);
			}
		}
	}

	@Override
	protected void dispatchEvent(AWTEvent event)
	{
		try
		{
			super.dispatchEvent(event);
		}
		catch (Throwable t)
		{
			synchronized (this)
			{
				Object source = event.getSource();
				if (source instanceof Component)
				{
					Component component = (Component) source;
					while (component != null)
					{
						Map<Class<Throwable>, Set<ThrowableProcessor<Component, Throwable>>> throwableMap = processorMap.get(component);
						if (throwableMap != null)
						{
							for (Entry<Class<Throwable>, Set<ThrowableProcessor<Component, Throwable>>> e : throwableMap.entrySet())
							{
								if (e.getKey().isInstance(t))
								{
									for (ThrowableProcessor<Component, Throwable> processor : e.getValue())
									{
										if (processor.processThrowable(component, t))
											return;
									}
								}
							}
						}
						component = component.getParent();
					}
				}
				throw t;
			}
		}
	}

}
