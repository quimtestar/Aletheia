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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CombinedSet;

public abstract class QueueEntry implements Comparable<QueueEntry>
{
	private final CandidateFinder candidateFinder;
	private final Proof proof;
	private final QueueSubEntry firstSubEntry;
	private final Set<QueueSubEntry> otherSubEntries;
	private final int sumMinAntecedentDependent;
	private final int sumMinUnassigned;
	private double priority;

	public class UnsolvableQueueEntryException extends Exception
	{
		private static final long serialVersionUID = -3932396378873632821L;

	}

	public QueueEntry(CandidateFinder candidateFinder, QueueSubEntry subEntry) throws UnsolvableQueueEntryException
	{
		this(candidateFinder, new Proof(subEntry), Collections.singleton(subEntry));
	}

	public QueueEntry(CandidateFinder candidateFinder, Proof proof, Collection<QueueSubEntry> subEntries) throws UnsolvableQueueEntryException
	{
		super();
		this.candidateFinder = candidateFinder;
		this.proof = new Proof(proof);
		this.otherSubEntries = new HashSet<>();
		QueueSubEntry first = null;
		int sumMinUnassigned = 0;
		int sumMinAntecedentDependent = 0;
		for (QueueSubEntry qse : subEntries)
		{
			if (qse.candidates().isEmpty())
				throw new UnsolvableQueueEntryException();
			if (!proof.containsQueueSubEntry(qse))
			{
				if (first == null || (!qse.equals(first) && !this.otherSubEntries.contains(qse)))
				{
					sumMinUnassigned += qse.candidatesInfo().minUnassigned;
					sumMinAntecedentDependent += qse.candidatesInfo().minAntecedentDependent;
					if (first == null || qse.compareTo(first) < 0)
					{
						if (first != null)
							this.otherSubEntries.add(first);
						first = qse;
					}
					else
						this.otherSubEntries.add(qse);
				}
			}
		}
		this.firstSubEntry = first;
		this.sumMinUnassigned = sumMinUnassigned;
		this.sumMinAntecedentDependent = sumMinAntecedentDependent;
		this.priority = Double.NaN;
	}

	public CandidateFinder getCandidateFinder()
	{
		return candidateFinder;
	}

	public Proof getProof()
	{
		return proof;
	}

	public QueueSubEntry getFirstSubEntry()
	{
		return firstSubEntry;
	}

	public Set<QueueSubEntry> getOtherSubEntries()
	{
		return Collections.unmodifiableSet(otherSubEntries);
	}

	public Set<QueueSubEntry> subEntries()
	{
		if (firstSubEntry == null)
			return Collections.emptySet();
		else
			return new CombinedSet<>(Collections.singleton(firstSubEntry), otherSubEntries);
	}

	public Collection<QueueEntry> offspring()
	{
		Collection<QueueEntry> offspring = new ArrayList<>();
		for (Proof.SolvedCandidate sc : firstSubEntry.solvedCandidates())
			try
			{
				boolean cycle = false;
				for (QueueSubEntry qse : sc.descendants())
					if (proof.existsPath(qse, firstSubEntry))
					{
						cycle = true;
						break;
					}
				if (!cycle)
					offspring.add(new NodeQueueEntry(this, firstSubEntry, sc));
			}
			catch (UnsolvableQueueEntryException e)
			{
			}
		return offspring;
	}

	public abstract int getAge();

	public boolean solved()
	{
		return firstSubEntry == null;
	}

	public double getPriority()
	{
		if (Double.isNaN(priority))
			priority = getAge() * (Math.pow(sumMinAntecedentDependent, 2) + Math.pow(sumMinUnassigned, 0.25));
		return priority;

	}

	@Override
	public int compareTo(QueueEntry other)
	{
		if (Math.abs(getPriority() - other.getPriority()) > 1e-10)
			return Double.compare(getPriority(), other.getPriority());
		return Integer.compare(otherSubEntries.size(), other.otherSubEntries.size());
	}

	public String priorityString()
	{
		return "(" + getAge() + "," + sumMinAntecedentDependent + "," + sumMinUnassigned + " -> " + getPriority() + ")";
	}

	public String toString(Transaction transaction)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[QueueEntry: " + priorityString() + " ");
		for (QueueSubEntry qse : subEntries())
			sb.append("," + qse.toString(transaction));
		sb.append("]");
		return sb.toString();
	}

	@Override
	public String toString()
	{
		Transaction transaction = getPersistenceManager().beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	private PersistenceManager getPersistenceManager()
	{
		return getCandidateFinder().getPersistenceManager();
	}

	boolean subsumes(QueueEntry other)
	{
		return other.subEntries().containsAll(subEntries());
	}

}
