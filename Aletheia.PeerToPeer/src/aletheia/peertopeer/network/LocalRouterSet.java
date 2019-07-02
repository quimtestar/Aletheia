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
package aletheia.peertopeer.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.network.RemoteRouterSet.RemoteRouter;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.SlotRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.TargetRouteableSubMessage;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.FilteredSet;
import aletheia.utilities.collections.NotNullFilter;

public class LocalRouterSet implements RouterSet
{
	private final UUID nodeUuid;
	private final Random random;

	private static class NeighbourEntry
	{
		private static final Bijection<NeighbourEntry, NetworkPhase> bijection = new Bijection<>()
		{

			@Override
			public NetworkPhase forward(NeighbourEntry e)
			{
				return e.neighbour;
			}

			@Override
			public NeighbourEntry backward(NetworkPhase output)
			{
				throw new UnsupportedOperationException();
			}
		};

		private boolean booked;
		private NetworkPhase neighbour;

	}

	private final ArrayList<NeighbourEntry> neighbours;

	public class LocalRouter implements Router
	{
		private final Set<NetworkPhase> neighbours;
		private final int distance;
		private final Set<UUID> spindle;

		private LocalRouter(Set<NetworkPhase> neighbours, int distance, Set<UUID> spindle)
		{
			this.neighbours = neighbours;
			this.distance = distance;
			this.spindle = spindle;
		}

		@Override
		public int getDistance()
		{
			return distance;
		}

		@Override
		public Set<UUID> getSpindle()
		{
			return Collections.unmodifiableSet(spindle);
		}

		public Set<NetworkPhase> getNeighbours()
		{
			return Collections.unmodifiableSet(neighbours);
		}

		public Set<NetworkPhase> openNeighbours()
		{
			return new FilteredSet<>(new Filter<NetworkPhase>()
			{

				@Override
				public boolean filter(NetworkPhase neighbour)
				{
					return neighbour.isOpen();
				}
			}, neighbours);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + distance;
			result = prime * result + ((neighbours == null) ? 0 : neighbours.hashCode());
			result = prime * result + ((spindle == null) ? 0 : spindle.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LocalRouter other = (LocalRouter) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (distance != other.distance)
				return false;
			if (neighbours == null)
			{
				if (other.neighbours != null)
					return false;
			}
			else if (!neighbours.equals(other.neighbours))
				return false;
			if (spindle == null)
			{
				if (other.spindle != null)
					return false;
			}
			else if (!spindle.equals(other.spindle))
				return false;
			return true;
		}

		private LocalRouterSet getOuterType()
		{
			return LocalRouterSet.this;
		}

		public NetworkPhase randomNeighbour()
		{
			NetworkPhase[] a = openNeighbours().toArray(new NetworkPhase[0]);
			if (a.length <= 0)
				return null;
			return a[random.nextInt(a.length)];
		}

		public boolean isEmpty()
		{
			return openNeighbours().isEmpty();
		}

	}

	private final ArrayList<LocalRouter> routers;

	public interface Listener
	{
		public void changed();
	}

	private final Set<Listener> listeners;

	public LocalRouterSet(UUID nodeUuid)
	{
		this.nodeUuid = nodeUuid;
		this.random = new Random(nodeUuid.getLeastSignificantBits());
		this.neighbours = new ArrayList<>();
		this.routers = new ArrayList<>();
		this.listeners = new HashSet<>();
	}

	public UUID getNodeUuid()
	{
		return nodeUuid;
	}

	public synchronized List<NetworkPhase> getNeighbours()
	{
		return Collections.unmodifiableList(new BijectionList<>(NeighbourEntry.bijection, neighbours));
	}

	@Override
	public synchronized List<LocalRouter> getRouters()
	{
		return Collections.<LocalRouter> unmodifiableList(routers);
	}

	public synchronized void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	private int firstDifferentBit(UUID uuid)
	{
		return MiscUtilities.firstDifferentBit(getNodeUuid(), uuid);
	}

	public int neighbourPosition(NetworkPhase neighbour)
	{
		return firstDifferentBit(neighbour.getPeerNodeUuid());
	}

	public synchronized NetworkPhase getNeighbour(int i)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= neighbours.size())
			return null;
		return neighbours.get(i).neighbour;
	}

	public synchronized boolean isBooked(int i)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= neighbours.size())
			return false;
		return neighbours.get(i).booked;
	}

	public synchronized void waitForUnbook(int i) throws InterruptedException
	{
		try
		{
			waitForUnbook(i, 0);
		}
		catch (LocalRouterSetBookTimeoutException e)
		{
			throw new Error(e);
		}
	}

	public class LocalRouterSetBookTimeoutException extends LocalRouterSetException
	{
		private static final long serialVersionUID = -3983595603043972073L;
	}

	public synchronized void waitForUnbook(int i, long timeout) throws InterruptedException, LocalRouterSetBookTimeoutException
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= neighbours.size())
			return;
		long t0 = timeout > 0 ? System.nanoTime() / 1000 / 1000 : 0;
		long t1 = t0;
		while (i < neighbours.size() && neighbours.get(i).booked && (timeout == 0 || (t1 - t0 < timeout)))
		{
			wait(timeout > 0 ? timeout - (t1 - t0) : 0);
			t1 = timeout > 0 ? System.nanoTime() / 1000 / 1000 : 0;
		}
		if (i < neighbours.size() && neighbours.get(i).booked)
			throw new LocalRouterSetBookTimeoutException();
	}

	public abstract class LocalRouterSetException extends Exception
	{
		private static final long serialVersionUID = 4574756528495575419L;

		public LocalRouterSetException()
		{
			super();
		}

		public LocalRouterSetException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public LocalRouterSetException(String message)
		{
			super(message);
		}

		public LocalRouterSetException(Throwable cause)
		{
			super(cause);
		}

	}

	public class NeighbourCollisionException extends LocalRouterSetException
	{
		private static final long serialVersionUID = -268934140464624173L;
	}

	public class BookedNeighbourPositionException extends LocalRouterSetException
	{
		private static final long serialVersionUID = -517255545318501387L;
	}

	private synchronized void setNeighbour(int i, NetworkPhase neighbour) throws NeighbourCollisionException, BookedNeighbourPositionException
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (neighbour == null)
			throw new IllegalArgumentException();
		if (i >= neighbours.size())
		{
			neighbours.ensureCapacity(i + 1);
			while (i >= neighbours.size())
				neighbours.add(new NeighbourEntry());
		}
		if (neighbours.get(i).neighbour != null)
			throw new NeighbourCollisionException();
		if (neighbours.get(i).booked)
			throw new BookedNeighbourPositionException();
		neighbours.get(i).neighbour = neighbour;
		updateRoutes();
	}

	public synchronized void bookNeighbour(int i) throws NeighbourCollisionException, BookedNeighbourPositionException
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= neighbours.size())
		{
			neighbours.ensureCapacity(i + 1);
			while (i >= neighbours.size())
				neighbours.add(new NeighbourEntry());
		}
		if (neighbours.get(i).neighbour != null)
			throw new NeighbourCollisionException();
		if (neighbours.get(i).booked)
			throw new BookedNeighbourPositionException();
		neighbours.get(i).booked = true;
	}

	public synchronized void bookNeighbourWait(int i) throws NeighbourCollisionException, InterruptedException
	{
		try
		{
			bookNeighbourWait(i, 0);
		}
		catch (LocalRouterSetBookTimeoutException e)
		{
			throw new Error(e);
		}
	}

	public synchronized void bookNeighbourWait(int i, long timeout) throws NeighbourCollisionException, InterruptedException, LocalRouterSetBookTimeoutException
	{
		waitForUnbook(i, timeout);
		try
		{
			bookNeighbour(i);
		}
		catch (BookedNeighbourPositionException e)
		{
			throw new Error(e);
		}
	}

	public synchronized void unbookNeighbour(int i)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i < neighbours.size())
		{
			neighbours.get(i).booked = false;
			notifyAll();
		}
	}

	public synchronized boolean dropNeighbour(int i)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i < neighbours.size())
		{
			NetworkPhase old = neighbours.get(i).neighbour;
			neighbours.get(i).neighbour = null;
			if (old != null)
			{
				updateRoutes(Collections.singleton(old.getPeerNodeUuid()));
				return true;
			}
		}
		return false;
	}

	public synchronized int putNeighbour(NetworkPhase neighbour) throws NeighbourCollisionException, BookedNeighbourPositionException
	{
		int i = neighbourPosition(neighbour);
		setNeighbour(i, neighbour);
		return i;
	}

	public synchronized NetworkPhase neighbourSlot(NetworkPhase neighbour)
	{
		return getNeighbour(neighbourPosition(neighbour));
	}

	public synchronized NetworkPhase neighbourSlot(UUID uuid)
	{
		return getNeighbour(firstDifferentBit(uuid));
	}

	@Deprecated
	public synchronized NetworkPhase putNeighbourIfEmpty(NetworkPhase neighbour)
	{
		int i = neighbourPosition(neighbour);
		NetworkPhase neighbour_ = getNeighbour(i);
		boolean booked = isBooked(i);
		if (neighbour_ == null && !booked)
		{
			try
			{
				setNeighbour(i, neighbour);
			}
			catch (NeighbourCollisionException | BookedNeighbourPositionException e)
			{
				throw new Error(e);
			}
		}
		return neighbour_;
	}

	public synchronized int dropNeighbour(NetworkPhase neighbour)
	{
		int i = neighbourIndex(neighbour);
		if (i < 0)
			return -1;
		dropNeighbour(i);
		return i;
	}

	public synchronized int dropNeighbour(UUID uuid)
	{
		int i = firstDifferentBit(uuid);
		if (i < 0)
			return -1;
		dropNeighbour(i);
		return i;
	}

	public synchronized Collection<NetworkPhase> neighbourCollection(int i)
	{
		if (i >= 0 && i < neighbours.size())
			return new FilteredCollection<>(new NotNullFilter<NetworkPhase>(),
					new BijectionCollection<>(NeighbourEntry.bijection, neighbours.subList(i, neighbours.size())));
		else
			return Collections.emptyList();
	}

	public synchronized Collection<NetworkPhase> neighbourCollection()
	{
		return neighbourCollection(0);
	}

	@Override
	public synchronized LocalRouter getRouter(int i)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= routers.size())
			return null;
		return routers.get(i);
	}

	private synchronized LocalRouter setRouter(int i, LocalRouter router)
	{
		if (i < 0)
			throw new IllegalArgumentException();
		if (i >= routers.size())
		{
			routers.ensureCapacity(i + 1);
			while (i >= routers.size())
				routers.add(null);
		}
		return routers.set(i, router);
	}

	private synchronized boolean changeRouter(int i, LocalRouter router)
	{
		LocalRouter router_ = getRouter(i);
		if ((router == null && router_ == null) || (router != null && router.equals(router_)))
			return false;
		else
		{
			setRouter(i, router);
			return true;
		}
	}

	public synchronized void updateRoutes()
	{
		updateRoutes(Collections.<UUID> emptySet());
	}

	public synchronized void updateRoutes(Collection<UUID> clearing)
	{
		boolean change = false;
		for (int i = 0; i < neighbours.size(); i++)
		{
			if (getNeighbour(i) != null)
			{
				if (changeRouter(i, new LocalRouter(Collections.singleton(getNeighbour(i)), 1, Collections.singleton(getNeighbour(i).getPeerNodeUuid()))))
					change = true;
			}
			else
			{
				int minDistance = Integer.MAX_VALUE;
				Set<NetworkPhase> rNeighbours = null;
				Set<UUID> rSpindle = null;
				for (NetworkPhase neighbour : neighbourCollection(i + 1))
				{
					RemoteRouterSet routerSet = neighbour.getRemoteRouterSet();
					if (routerSet != null)
					{
						RemoteRouter router = routerSet.getRouter(i);
						if (router != null && !router.spindleIntersects(clearing) && !router.getSpindle().contains(nodeUuid))
						{
							int distance = router.getDistance() + 1;
							if (distance == minDistance)
							{
								rNeighbours.add(neighbour);
								rSpindle.add(neighbour.getPeerNodeUuid());
								rSpindle.addAll(router.getSpindle());
							}
							else if (distance < minDistance)
							{
								rNeighbours = new HashSet<>(Arrays.asList(neighbour));
								minDistance = distance;
								rSpindle = new HashSet<>(Arrays.asList(neighbour.getPeerNodeUuid()));
								rSpindle.addAll(router.getSpindle());
							}
						}
					}
				}
				if (rNeighbours != null)
				{
					if (changeRouter(i, new LocalRouter(rNeighbours, minDistance, rSpindle)))
						change = true;
				}
				else
				{
					if (changeRouter(i, null))
						change = true;
				}
			}
		}
		if (change)
		{
			for (NetworkPhase neighbour : neighbourCollection())
				neighbour.routerSet(clearing);
			for (Listener listener : listeners)
				listener.changed();
		}

	}

	private synchronized LocalRouter extremeRouter(int i, boolean extreme)
	{
		for (int j = i + 1; j < MiscUtilities.uuidBitLength; j++)
		{
			if (MiscUtilities.bitAt(getNodeUuid(), j) != extreme)
			{
				LocalRouter router = getRouter(j);
				if (router != null && !router.openNeighbours().isEmpty())
					return router;
			}
		}
		return null;
	}

	public synchronized LocalRouter pathStepRouter(UUID uuid)
	{
		if (getNodeUuid().equals(uuid))
			return null;
		int i = firstDifferentBit(uuid);
		LocalRouter router = getRouter(i);
		if (router == null || router.openNeighbours().isEmpty())
			router = extremeRouter(i, MiscUtilities.bitAt(uuid, i));
		return router;
	}

	public synchronized NetworkPhase pathStep(UUID uuid)
	{
		LocalRouter router = pathStepRouter(uuid);
		if (router == null)
			return null;
		return router.randomNeighbour();
	}

	public synchronized boolean isClosest(UUID uuid)
	{
		LocalRouter router = pathStepRouter(uuid);
		if (router == null)
			return true;
		return router.openNeighbours().isEmpty();
	}

	public synchronized LocalRouter pathStepRouter(int slot)
	{
		return getRouter(slot);
	}

	public synchronized NetworkPhase pathStep(int slot)
	{
		LocalRouter router = pathStepRouter(slot);
		if (router == null)
			return null;
		return router.randomNeighbour();
	}

	public synchronized Set<NetworkPhase> pathStepMultiple(UUID uuid)
	{
		LocalRouter router = pathStepRouter(uuid);
		if (router == null)
			return Collections.emptySet();
		return router.openNeighbours();
	}

	public synchronized Set<NetworkPhase> pathStepMultiple(int slot)
	{
		LocalRouter router = pathStepRouter(slot);
		if (router == null)
			return Collections.emptySet();
		return router.openNeighbours();
	}

	public synchronized NetworkPhase pathStep(RouteableSubMessage routeableSubMessage)
	{
		if (routeableSubMessage instanceof TargetRouteableSubMessage)
			return pathStep(((TargetRouteableSubMessage) routeableSubMessage).getTarget());
		else if (routeableSubMessage instanceof SlotRouteableSubMessage)
		{
			SlotRouteableSubMessage slotRouteableSubMessage = (SlotRouteableSubMessage) routeableSubMessage;
			return pathStep(slotRouteableSubMessage.getSlot());
		}
		else
			throw new Error();
	}

	public synchronized Set<NetworkPhase> pathStepMultiple(RouteableSubMessage routeableSubMessage)
	{
		if (routeableSubMessage instanceof TargetRouteableSubMessage)
			return pathStepMultiple(((TargetRouteableSubMessage) routeableSubMessage).getTarget());
		else if (routeableSubMessage instanceof SlotRouteableSubMessage)
		{
			SlotRouteableSubMessage slotRouteableSubMessage = (SlotRouteableSubMessage) routeableSubMessage;
			return pathStepMultiple(slotRouteableSubMessage.getSlot());
		}
		else
			throw new Error();
	}

	public synchronized int neighbourIndex(NetworkPhase neighbour)
	{
		int i = neighbourPosition(neighbour);
		if (neighbour.equals(getNeighbour(i)))
			return i;
		else
			return -1;
	}

	public synchronized boolean containsNeighbour(NetworkPhase neighbour)
	{
		return neighbourIndex(neighbour) >= 0;
	}

	public synchronized NetworkPhase lastNeighbour()
	{
		int i = lastNeighbourIndex();
		if (i < 0)
			return null;
		return getNeighbour(i);
	}

	public synchronized int lastNeighbourIndex()
	{
		for (int i = neighbours.size() - 1; i >= 0; i--)
			if (getNeighbour(i) != null)
				return i;
		return -1;
	}

	public synchronized Collection<Integer> freeNeighbourSlots()
	{
		Collection<Integer> c = new ArrayList<>();
		for (int i = 0; i < routers.size(); i++)
			if ((getRouter(i) != null) && (getNeighbour(i) == null))
				c.add(i);
		return c;
	}

	public synchronized double networkSizeEstimation()
	{
		double k = 0;
		int n = 0;
		for (ListIterator<LocalRouter> iterator = getRouters().listIterator(); iterator.hasNext();)
		{
			int i = iterator.nextIndex();
			LocalRouter router = iterator.next();
			if (router == null || router.isEmpty())
				k += 1 / (double) (1 << (i + 1));
			n++;
		}
		k += 1 / (double) (1 << n);
		return Math.exp(-k) / (1 - Math.exp(-k));
	}

}
