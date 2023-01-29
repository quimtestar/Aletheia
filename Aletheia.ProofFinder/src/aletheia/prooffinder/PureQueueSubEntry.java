/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.utilities.collections.AdaptedCollection;

public abstract class PureQueueSubEntry extends QueueSubEntry
{
	private final Term term;
	private final List<VirtualStatement> localVirtualStatements;
	private final SimpleTerm target;

	public PureQueueSubEntry(CandidateFinder candidateFinder, Context context, Term term)
	{
		super(candidateFinder, context);
		this.term = term;
		this.localVirtualStatements = new ArrayList<>();
		int order = 0;
		while (term instanceof FunctionTerm)
		{
			FunctionTerm func = (FunctionTerm) term;
			VirtualStatement vs = new VirtualStatement(this, func.getParameter(), order);
			this.localVirtualStatements.add(vs);
			term = func.getBody();
			order++;
		}
		this.target = (SimpleTerm) term;
	}

	@Override
	public Term getTerm()
	{
		return term;
	}

	@Override
	public List<VirtualStatement> localVirtualStatements()
	{
		return Collections.unmodifiableList(localVirtualStatements);
	}

	@Override
	public SimpleTerm getTarget()
	{
		return target;
	}

	@Override
	public abstract List<VirtualStatement> virtualStatements();

	@Override
	protected Collection<Candidate> findCandidates()
	{
		return new AdaptedCollection<>(getCandidateFinder().pureCandidatesFor(getContext(), virtualStatements(), target));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || !(obj instanceof PureQueueSubEntry))
			return false;
		return true;
	}

}
