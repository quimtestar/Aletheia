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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public abstract class Candidate
{
	private final Term term;
	private final SimpleTerm target;
	private final List<VariableTerm> varList;
	private final SimpleTerm consequent;
	private final Map<VariableTerm, Term> assignMap;
	private final Set<VariableTerm> unassignedVarSet;
	private final Map<VariableTerm, Term> antecedentMap;
	private final Map<VariableTerm, Set<VariableTerm>> antecedentDependentMap;

	public Candidate(final Term term, SimpleTerm target)
	{
		this.term = term;
		this.target = target;
		this.varList = new ArrayList<>();
		Set<VariableTerm> varSet = new HashSet<>();
		Term term_ = term;
		while (term_ instanceof FunctionTerm)
		{
			FunctionTerm func = (FunctionTerm) term_;
			VariableTerm var = func.getParameter();
			varList.add(var);
			if (!varSet.add(var))
				throw new RuntimeException();
			term_ = func.getBody();
		}
		this.consequent = (SimpleTerm) term_;
		Term.Match termMatch = consequent.match(varSet, target, Collections.<VariableTerm> emptySet());
		Map<VariableTerm, Term> assignMap = null;
		Set<VariableTerm> unassignedVarSet = null;
		Map<VariableTerm, Term> antecedentMap = null;
		Map<VariableTerm, Set<VariableTerm>> antecedentDependentMap = null;
		if (termMatch != null)
		{
			try
			{
				assignMap = termMatch.getAssignMapLeft();
				unassignedVarSet = new HashSet<>(varList);
				unassignedVarSet.removeAll(assignMap.keySet());
				antecedentMap = new HashMap<>();
				Set<VariableTerm> antecedentDependentMapKeys = new HashSet<>();
				Map<VariableTerm, Set<VariableTerm>> antecedentDependentMap_ = new HashMap<>();
				for (int i = 0; i < varList.size(); i++)
				{
					VariableTerm v = varList.get(i);
					Term t = v.getType();
					Set<VariableTerm> fv = t.freeVariables();
					List<Term.Replace> replaces = new ArrayList<>();
					for (VariableTerm v_ : varList.subList(0, i))
					{
						Term t_ = assignMap.get(v_);
						if (t_ != null)
							replaces.add(new Term.Replace(v_, t_));
						else if (fv.contains(v_))
						{
							antecedentDependentMap_.get(v_).add(v);
							antecedentDependentMapKeys.add(v_);
						}
					}
					antecedentMap.put(v, t.replace(replaces));
					antecedentDependentMap_.put(v, new HashSet<VariableTerm>());
				}
				antecedentDependentMap = new HashMap<>();
				for (VariableTerm v : antecedentDependentMapKeys)
					antecedentDependentMap.put(v, Collections.unmodifiableSet(antecedentDependentMap_.get(v)));
			}
			catch (ReplaceTypeException e) // XXX
			{
				assignMap = null;
				unassignedVarSet = null;
				antecedentMap = null;
				antecedentDependentMap = null;
			}
		}
		this.assignMap = assignMap;
		this.unassignedVarSet = unassignedVarSet;
		this.antecedentMap = antecedentMap;
		this.antecedentDependentMap = antecedentDependentMap;
	}

	protected Candidate(Candidate other)
	{
		this.term = other.term;
		this.target = other.target;
		this.varList = new ArrayList<>(other.varList);
		this.consequent = other.consequent;
		this.assignMap = new HashMap<>(other.assignMap);
		this.unassignedVarSet = new HashSet<>(other.unassignedVarSet);
		this.antecedentMap = new HashMap<>(other.antecedentMap);
		this.antecedentDependentMap = new HashMap<>(other.antecedentDependentMap);
	}

	public Term getTerm()
	{
		return term;
	}

	public SimpleTerm getTarget()
	{
		return target;
	}

	public List<VariableTerm> getVarList()
	{
		return Collections.unmodifiableList(varList);
	}

	public SimpleTerm getConsequent()
	{
		return consequent;
	}

	public boolean isAssigned()
	{
		return assignMap != null;
	}

	public Map<VariableTerm, Term> getAssignMap()
	{
		return Collections.unmodifiableMap(assignMap);
	}

	public Set<VariableTerm> getUnassignedVarSet()
	{
		return Collections.unmodifiableSet(unassignedVarSet);
	}

	public Map<VariableTerm, Term> getAntecedentMap()
	{
		return Collections.unmodifiableMap(antecedentMap);
	}

	public Map<VariableTerm, Set<VariableTerm>> getAntecedentDependentMap()
	{
		return Collections.unmodifiableMap(antecedentDependentMap);
	}

	protected void assignVar(VariableTerm var, Term assign)
	{
		if (assignMap.put(var, assign) != null)
			throw new RuntimeException();
		if (!unassignedVarSet.remove(var))
			throw new RuntimeException();
		for (VariableTerm v : antecedentDependentMap.get(var))
		{
			Term t = antecedentMap.get(v);
			try
			{
				t = t.replace(var, assign);
			}
			catch (ReplaceTypeException e)
			{
				throw new Error(e);
			}
			antecedentMap.put(v, t);
		}
		antecedentDependentMap.remove(var);
	}

	public PureCandidate getPureCandidate()
	{
		Candidate c = this;
		while (c instanceof ImpureCandidate)
			c = ((ImpureCandidate) c).getParent();
		return (PureCandidate) c;
	}

	public String toString(Transaction transaction)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[Candidate: ");
		PureCandidate pc = getPureCandidate();
		Map<IdentifiableVariableTerm, Identifier> variableToIdentifier;
		if (pc instanceof StatementCandidate)
		{
			Statement st = ((StatementCandidate) pc).getStatement();
			sb.append(st.toString(transaction) + ": ");
			variableToIdentifier = st.parentVariableToIdentifier(transaction);
		}
		else if (pc instanceof VirtualStatementCandidate)
		{
			VirtualStatement vs = ((VirtualStatementCandidate) pc).getVirtualStatement();
			sb.append(vs.toString(transaction) + ": ");
			variableToIdentifier = vs.getPureQueueSubEntry().getContext().parentVariableToIdentifier(transaction);
		}
		else
			throw new Error();

		for (VariableTerm var : varList)
		{
			Term term = getAssignMap().get(var);
			if (term != null)
				sb.append(var.toString() + " <- " + term.toString(variableToIdentifier) + ", ");
		}
		sb.append("]");
		return sb.toString();
	}

	private PersistenceManager getPersistenceManager()
	{
		PureCandidate pc = getPureCandidate();
		if (pc instanceof StatementCandidate)
			return ((StatementCandidate) pc).getStatement().getPersistenceManager();
		else if (pc instanceof VirtualStatementCandidate)
			return ((VirtualStatementCandidate) pc).getVirtualStatement().getPureQueueSubEntry().getCandidateFinder().getPersistenceManager();
		else
			throw new Error();

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

}
