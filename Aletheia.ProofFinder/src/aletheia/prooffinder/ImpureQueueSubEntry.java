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

import java.util.Collection;
import java.util.List;

import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.utilities.collections.AdaptedCollection;

public class ImpureQueueSubEntry extends QueueSubEntry
{
	private final QueueSubEntry parent;
	private final Candidate candidate;
	private final VariableTerm variable;

	public ImpureQueueSubEntry(QueueSubEntry parent, Candidate candidate, VariableTerm variable)
	{
		super(parent.getCandidateFinder(), parent.getContext());
		this.parent = parent;
		this.candidate = candidate;
		this.variable = variable;
	}

	public QueueSubEntry getParent()
	{
		return parent;
	}

	public Candidate getCandidate()
	{
		return candidate;
	}

	public VariableTerm getVariable()
	{
		return variable;
	}

	@Override
	public Term getTerm()
	{
		return parent.getTerm();
	}

	@Override
	public SimpleTerm getTarget()
	{
		return parent.getTarget();
	}

	@Override
	public List<VirtualStatement> localVirtualStatements()
	{
		return parent.localVirtualStatements();
	}

	@Override
	public List<VirtualStatement> virtualStatements()
	{
		return parent.virtualStatements();
	}

	@Override
	protected Collection<Candidate> findCandidates()
	{
		return new AdaptedCollection<Candidate>(
				parent.getCandidateFinder().impureCandidatesFor(parent.getContext(), parent.virtualStatements(), candidate, variable));
	}

	@Override
	protected int calcHashCode()
	{
		final int prime = 31;
		int result = super.calcHashCode();
		result = prime * result + ((candidate == null) ? 0 : candidate.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ImpureQueueSubEntry))
			return false;
		ImpureQueueSubEntry other = (ImpureQueueSubEntry) obj;
		if (candidate == null)
		{
			if (other.candidate != null)
				return false;
		}
		else if (!candidate.equals(other.candidate))
			return false;
		if (variable == null)
		{
			if (other.variable != null)
				return false;
		}
		else if (!variable.equals(other.variable))
			return false;
		return true;
	}

}
