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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.Term.UnprojectException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public abstract class QueueSubEntry implements Comparable<QueueSubEntry>
{
	private final CandidateFinder candidateFinder;
	private final Context context;

	private SoftReference<Collection<Candidate>> refCandidates;

	public class CandidatesInfo
	{
		public final int minUnassigned;
		public final int minAntecedentDependent;

		public CandidatesInfo(int minUnassigned, int minAntecedentDependent)
		{
			super();
			this.minUnassigned = minUnassigned;
			this.minAntecedentDependent = minAntecedentDependent;
		}

		@Override
		public String toString()
		{
			return "(" + minAntecedentDependent + "," + minUnassigned + ")";
		}
	};

	private CandidatesInfo candidatesInfo;

	private int hashCode;
	private boolean hashCoded;

	public QueueSubEntry(CandidateFinder candidateFinder, Context context)
	{
		super();
		this.candidateFinder = candidateFinder;
		this.context = context;
		this.refCandidates = null;
		this.candidatesInfo = null;
		this.hashCoded = false;
	}

	public CandidateFinder getCandidateFinder()
	{
		return candidateFinder;
	}

	public Context getContext()
	{
		return context;
	}

	public abstract Term getTerm();

	public abstract SimpleTerm getTarget();

	public abstract List<VirtualStatement> localVirtualStatements();

	public abstract List<VirtualStatement> virtualStatements();

	protected abstract Collection<Candidate> findCandidates();

	public synchronized Collection<Candidate> candidates()
	{
		Collection<Candidate> candidates;
		if ((refCandidates == null) || (refCandidates.get() == null))
		{
			candidates = findCandidates();
			refCandidates = new SoftReference<Collection<Candidate>>(candidates);
		}
		else
			candidates = refCandidates.get();
		return candidates;
	}

	public CandidatesInfo candidatesInfo()
	{
		if (candidatesInfo == null)
		{
			int minUnassigned = Integer.MAX_VALUE;
			int minAntecedentDependent = Integer.MAX_VALUE;
			for (Candidate c : candidates())
			{
				int unassigned = c.getUnassignedVarSet().size();
				if (unassigned < minUnassigned)
					minUnassigned = unassigned;
				int antecedentDependent = c.getAntecedentDependentMap().size();
				if (antecedentDependent < minAntecedentDependent)
					minAntecedentDependent = antecedentDependent;
			}
			candidatesInfo = new CandidatesInfo(minUnassigned, minAntecedentDependent);
		}
		return candidatesInfo;
	}

	public Collection<Proof.SolvedCandidate> solvedCandidates()
	{
		Collection<Proof.SolvedCandidate> solvedCandidates = new ArrayList<Proof.SolvedCandidate>();
		for (Candidate candidate : candidates())
		{
			if (candidate.getAntecedentDependentMap().isEmpty())
			{
				Proof.PureSolvedCandidate psc = new Proof.PureSolvedCandidate(candidate);
				for (VariableTerm var : candidate.getUnassignedVarSet())
					try
				{
						psc.putDescendant(var, new NodeQueueSubEntry(this, candidate, var));
				}
				catch (UnprojectException e)
				{
					throw new RuntimeException();
				}
				solvedCandidates.add(psc);
			}
			else
				solvedCandidates.add(new Proof.ImpureSolvedCandidate(candidate, new ImpureQueueSubEntry(this, candidate, candidate.getAntecedentDependentMap()
						.keySet().iterator().next())));
		}
		return solvedCandidates;
	}

	@Override
	public int compareTo(QueueSubEntry other)
	{
		CandidatesInfo ci0 = candidatesInfo();
		CandidatesInfo ci1 = other.candidatesInfo();

		int c = -Integer.compare(ci0.minAntecedentDependent, ci1.minAntecedentDependent);
		if (c == 0)
			c = -Integer.compare(ci0.minUnassigned, ci1.minUnassigned);
		return c;

	}

	public PureQueueSubEntry getPureQueueSubEntry()
	{
		QueueSubEntry qse = this;
		while (qse instanceof ImpureQueueSubEntry)
			qse = ((ImpureQueueSubEntry) qse).getParent();
		return (PureQueueSubEntry) qse;
	}

	public String toString(Transaction transaction)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[QueueSubEntry: " + getTarget().toString(getContext().variableToIdentifier(transaction)) + ": " + candidatesInfo().toString() + "]");
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
		return getContext().getPersistenceManager();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof QueueSubEntry))
			return false;
		QueueSubEntry other = (QueueSubEntry) obj;
		if (hashCoded && other.hashCoded && hashCode != other.hashCode)
			return false;
		if (!context.equals(other.context))
			return false;
		if (virtualStatements().size() != other.virtualStatements().size())
			return false;
		class SubEntry
		{
			public final VariableTerm v0;
			public final VariableTerm v1;

			public SubEntry(VariableTerm v0, VariableTerm v1)
			{
				super();
				this.v0 = v0;
				this.v1 = v1;
			}
		}
		List<SubEntry> subs = new ArrayList<SubEntry>();
		for (int i = 0; i < virtualStatements().size(); i++)
		{
			VariableTerm v0 = virtualStatements().get(i).getVariable();
			VariableTerm v1 = other.virtualStatements().get(i).getVariable();
			VariableTerm v_ = v0;
			for (SubEntry se : subs)
				try
			{
					v_ = (VariableTerm) v_.replace(se.v0, se.v1);
			}
			catch (ReplaceTypeException e)
			{
				throw new Error(e);
			}
			if (!v1.getType().equals(v_.getType()))
				return false;
			subs.add(new SubEntry(v0, v1));
		}
		Term target_ = getTarget();
		for (SubEntry se : subs)
		{
			try
			{
				target_ = target_.replace(se.v0, se.v1);
			}
			catch (ReplaceTypeException e)
			{
				throw new Error(e);
			}
		}
		if (!target_.equals(other.getTarget()))
			return false;
		return true;
	}

	protected int calcHashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + context.hashCode();
		final int primevar = 93563;
		int hashvalue = 1;
		class SubEntry
		{
			public final VariableTerm v0;
			public final VariableTerm v1;

			public SubEntry(VariableTerm v0, VariableTerm v1)
			{
				super();
				this.v0 = v0;
				this.v1 = v1;
			}
		}
		List<SubEntry> subs = new ArrayList<SubEntry>();
		for (VirtualStatement va : virtualStatements())
		{
			VariableTerm v0 = va.getVariable();
			VariableTerm v = v0;
			for (SubEntry se : subs)
				try
			{
					v = (VariableTerm) v.replace(se.v0, se.v1);
			}
			catch (ReplaceTypeException e)
			{
				throw new Error(e);
			}
			final int hashCode = (hashvalue *= primevar);
			VariableTerm vhash = new ParameterVariableTerm(v.getType())
			{
				private static final long serialVersionUID = 2765761103803061198L;

				@Override
				public int hashCode(int hasher)
				{
					return hashCode;
				}
			};
			subs.add(new SubEntry(v0, vhash));
			result = prime * result + vhash.getType().hashCode();

		}
		Term target_ = getTarget();
		for (SubEntry se : subs)
			try
		{
				target_ = target_.replace(se.v0, se.v1);
		}
		catch (ReplaceTypeException e)
		{
			throw new Error(e);
		}
		result = prime * result + target_.hashCode();
		return result;
	}

	@Override
	final public int hashCode()
	{
		if (!hashCoded)
			hashCode = calcHashCode();
		hashCoded = true;
		return hashCode;
	}

}
