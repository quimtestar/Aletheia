/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import aletheia.utilities.collections.CombinedCollection;

class NodeQueueEntry extends QueueEntry
{

	private final QueueEntry parent;
	private final Proof.SolvedCandidate solvedCandidate;
	private final int age;

	public NodeQueueEntry(QueueEntry parent, QueueSubEntry queueSubEntry, Proof.SolvedCandidate solvedCandidate) throws UnsolvableQueueEntryException
	{
		super(parent.getCandidateFinder(), parent.getProof(), new CombinedCollection<>(parent.getOtherSubEntries(), solvedCandidate.descendants()));
		this.parent = parent;
		this.solvedCandidate = solvedCandidate;
		this.age = parent.getAge() + 1;
		if (getProof().putSolvedCandidate(queueSubEntry, solvedCandidate) != null)
			throw new RuntimeException();
	}

	public QueueEntry getParent()
	{
		return parent;
	}

	public Proof.SolvedCandidate getSolvedCandidate()
	{
		return solvedCandidate;
	}

	@Override
	public int getAge()
	{
		return age;
	}

}
