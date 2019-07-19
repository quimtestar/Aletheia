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
package aletheia.peertopeer.network;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;

public class Belt
{
	private final UUID nodeUuid;
	private final Comparator<NetworkPhase> networkPhaseComparator;
	private NetworkPhase left;
	private NetworkPhase right;

	public Belt(UUID nodeUuid)
	{
		this.nodeUuid = nodeUuid;
		this.networkPhaseComparator = new RelativeNetworkPhaseComparator(nodeUuid);

		this.left = null;
		this.right = null;
	}

	public UUID getNodeUuid()
	{
		return nodeUuid;
	}

	public Comparator<NetworkPhase> getNetworkPhaseComparator()
	{
		return networkPhaseComparator;
	}

	public synchronized NetworkPhase getLeft()
	{
		return left;
	}

	public synchronized NetworkPhase setLeft(NetworkPhase left)
	{
		NetworkPhase old = this.left;
		this.left = left;
		notifyAll();
		return old;
	}

	public synchronized NetworkPhase getRight()
	{
		return right;
	}

	public synchronized NetworkPhase setRight(NetworkPhase right)
	{
		NetworkPhase old = this.right;
		this.right = right;
		notifyAll();
		return old;
	}

	public synchronized NetworkPhase getSide(Side side)
	{
		switch (side)
		{
		case Left:
			return getLeft();
		case Right:
			return getRight();
		default:
			throw new Error();
		}
	}

	public synchronized boolean containsNeighbour(NetworkPhase neighbour)
	{
		return neighbour.equals(left) || neighbour.equals(right);
	}

	public synchronized boolean dropNeighbour(NetworkPhase networkPhase, Set<Side> sides)
	{
		boolean drop = false;
		if (sides.contains(Side.Left) && networkPhase.equals(left))
			if (setLeft(null) != null)
				drop = true;
		if (sides.contains(Side.Right) && networkPhase.equals(right))
			if (setRight(null) != null)
				drop = true;
		return drop;
	}

	public synchronized boolean dropNeighbour(NetworkPhase networkPhase)
	{
		return dropNeighbour(networkPhase, EnumSet.allOf(Side.class));
	}

	public synchronized Collection<NetworkPhase> neighbourCollection()
	{
		return new FilteredCollection<>(new NotNullFilter<NetworkPhase>(), Arrays.asList(left, right));
	}

	public synchronized boolean isComplete()
	{
		return left != null && right != null;
	}

	public synchronized void waitForCompletionStatus(boolean complete) throws InterruptedException
	{
		while (isComplete() != complete)
			wait();
	}

	public synchronized void waitForCompletionStatus(boolean complete, long timeout) throws InterruptedException
	{
		if (timeout <= 0)
			waitForCompletionStatus(complete);
		else
		{
			long t0 = System.nanoTime() / 1000 / 1000;
			long t1 = t0;
			while (isComplete() != complete && (t1 - t0 < timeout))
			{
				wait(timeout - (t1 - t0));
				t1 = System.nanoTime() / 1000 / 1000;
			}
		}
	}

	private int firstDifferentBit(UUID uuid)
	{
		return MiscUtilities.firstDifferentBit(getNodeUuid(), uuid);
	}

	public synchronized NetworkPhase closestNeighbour()
	{
		int iLeft = left != null ? firstDifferentBit(left.getPeerNodeUuid()) : -1;
		int iRight = right != null ? firstDifferentBit(right.getPeerNodeUuid()) : -1;
		return iLeft > iRight ? left : right;
	}

}
