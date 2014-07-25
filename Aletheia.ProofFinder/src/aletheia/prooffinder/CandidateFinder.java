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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.prooffinder.TermMatcher.TermMatch;
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.CacheWithCleanerMap;
import aletheia.utilities.collections.CombinedCollection;
import aletheia.utilities.collections.CombinedSet;
import aletheia.utilities.collections.WeakCacheWithCleanerMap;

public class CandidateFinder implements StatementCacheTree.Listener
{
	private final PersistenceManager persistenceManager;
	private final ContextWatcher contextWatcher;
	private final StatementCacheTree statementCacheTree;

	private class PureCandidatesCacheMapKey
	{
		public final Context context;
		public final SimpleTerm target;

		public PureCandidatesCacheMapKey(Context context, SimpleTerm target)
		{
			super();
			this.context = context;
			this.target = target;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((context == null) ? 0 : context.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
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
			PureCandidatesCacheMapKey other = (PureCandidatesCacheMapKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (context == null)
			{
				if (other.context != null)
					return false;
			}
			else if (!context.equals(other.context))
				return false;
			if (target == null)
			{
				if (other.target != null)
					return false;
			}
			else if (!target.equals(other.target))
				return false;
			return true;
		}

		private CandidateFinder getOuterType()
		{
			return CandidateFinder.this;
		}

	}

	private final CacheWithCleanerMap<PureCandidatesCacheMapKey, Set<StatementCandidate>> pureCandidatesCacheMap;
	private final Map<Context, Set<PureCandidatesCacheMapKey>> pureCandidatesCacheMapKeys;

	private class ImpureCandidatesCacheMapKey
	{
		public final Context context;
		public final Candidate candidate;
		public final VariableTerm variable;
		public final VariableTerm variableDependent;

		public ImpureCandidatesCacheMapKey(Context context, Candidate candidate, VariableTerm variable, VariableTerm variableDependent)
		{
			super();
			this.context = context;
			this.candidate = candidate;
			this.variable = variable;
			this.variableDependent = variableDependent;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((candidate == null) ? 0 : candidate.hashCode());
			result = prime * result + ((context == null) ? 0 : context.hashCode());
			result = prime * result + ((variable == null) ? 0 : variable.hashCode());
			result = prime * result + ((variableDependent == null) ? 0 : variableDependent.hashCode());
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
			ImpureCandidatesCacheMapKey other = (ImpureCandidatesCacheMapKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (candidate == null)
			{
				if (other.candidate != null)
					return false;
			}
			else if (!candidate.equals(other.candidate))
				return false;
			if (context == null)
			{
				if (other.context != null)
					return false;
			}
			else if (!context.equals(other.context))
				return false;
			if (variable == null)
			{
				if (other.variable != null)
					return false;
			}
			else if (!variable.equals(other.variable))
				return false;
			if (variableDependent == null)
			{
				if (other.variableDependent != null)
					return false;
			}
			else if (!variableDependent.equals(other.variableDependent))
				return false;
			return true;
		}

		private CandidateFinder getOuterType()
		{
			return CandidateFinder.this;
		}
	}

	private final CacheWithCleanerMap<ImpureCandidatesCacheMapKey, Set<ImpureCandidate>> impureCandidatesCacheMap;
	private final Map<Context, Set<ImpureCandidatesCacheMapKey>> impureCandidatesCacheMapKeys;

	public CandidateFinder(PersistenceManager persistenceManager, ContextWatcher contextWatcher)
	{
		this.persistenceManager = persistenceManager;
		this.contextWatcher = contextWatcher;
		this.statementCacheTree = new StatementCacheTree(persistenceManager, contextWatcher);
		this.pureCandidatesCacheMap = new WeakCacheWithCleanerMap<PureCandidatesCacheMapKey, Set<StatementCandidate>>();
		this.pureCandidatesCacheMapKeys = new HashMap<Context, Set<PureCandidatesCacheMapKey>>();
		this.impureCandidatesCacheMap = new WeakCacheWithCleanerMap<ImpureCandidatesCacheMapKey, Set<ImpureCandidate>>();
		this.impureCandidatesCacheMapKeys = new HashMap<Context, Set<ImpureCandidatesCacheMapKey>>();
		this.statementCacheTree.addListener(this);
		this.pureCandidatesCacheMap.addListener(new PureCandidatesCacheMapListener());
		this.impureCandidatesCacheMap.addListener(new ImpureCandidatesCacheMapListener());
	}

	PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	private synchronized Collection<StatementCandidate> localStatementCandidatesFor(Context context, SimpleTerm target)
	{
		PureCandidatesCacheMapKey key = new PureCandidatesCacheMapKey(context, target);
		Set<StatementCandidate> candidates = pureCandidatesCacheMap.get(new PureCandidatesCacheMapKey(context, target));
		if (candidates == null)
		{
			candidates = new HashSet<StatementCandidate>();
			Collection<Statement> statements = statementCacheTree.getLocalStatementCollection(context);
			synchronized (statements)
			{
				for (Statement st : statements)
				{
					StatementCandidate sc = new StatementCandidate(st, target);
					if (sc.isAssigned())
						candidates.add(sc);
				}
			}
			pureCandidatesCacheMap.put(key, candidates);
			Set<PureCandidatesCacheMapKey> keySet = pureCandidatesCacheMapKeys.get(context);
			if (keySet == null)
			{
				keySet = new HashSet<PureCandidatesCacheMapKey>();
				pureCandidatesCacheMapKeys.put(context, keySet);
			}
			keySet.add(key);
		}
		return candidates;
	}

	private Collection<VirtualStatementCandidate> virtualStatementCandidatesFor(List<VirtualStatement> virtualStatements, SimpleTerm target)
	{
		Collection<VirtualStatementCandidate> candidates = new ArrayList<VirtualStatementCandidate>();
		for (VirtualStatement vs : virtualStatements)
		{
			VirtualStatementCandidate vc = new VirtualStatementCandidate(vs, target);
			if (vc.isAssigned())
				candidates.add(vc);
		}
		return candidates;
	}

	public Collection<PureCandidate> pureCandidatesFor(Context context, List<VirtualStatement> virtualStatements, SimpleTerm target)
	{
		Stack<Context> stack = new Stack<Context>();
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			while (!(context instanceof RootContext))
			{
				stack.push(context);
				context = context.getContext(transaction);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
		Collection<StatementCandidate> statementCandidates = localStatementCandidatesFor(context, target);
		while (!stack.isEmpty())
			statementCandidates = new CombinedCollection<StatementCandidate>(localStatementCandidatesFor(stack.pop(), target), statementCandidates);

		Collection<VirtualStatementCandidate> virtualStatementCandidates = virtualStatementCandidatesFor(virtualStatements, target);

		return new CombinedCollection<PureCandidate>(new AdaptedCollection<PureCandidate>(virtualStatementCandidates), new AdaptedCollection<PureCandidate>(
				statementCandidates));
	}

	private Term assignImpure(VariableTerm variable, Term target, Term term)
	{
		List<VariableTerm> varList = new ArrayList<VariableTerm>();
		Set<VariableTerm> varSet = new HashSet<VariableTerm>();
		Term term_ = term;
		while (term_ instanceof FunctionTerm)
		{
			FunctionTerm func = (FunctionTerm) term_;
			varList.add(func.getParameter());
			varSet.add(func.getParameter());
			term_ = func.getBody();
		}
		TermMatch termMatch = TermMatcher.match(term_, varSet, target, Collections.singleton(variable));
		if (termMatch == null)
			return null;
		Term t = termMatch.getAssignMapRight().get(variable);
		if (t == null)
			return null;
		for (VariableTerm v_ : varList)
		{
			Term t_ = termMatch.getAssignMapLeft().get(v_);
			if (t_ != null)
			{
				try
				{
					t = t.replace(v_, t_);
				}
				catch (ReplaceTypeException e1)
				{
					throw new Error(e1);
				}
			}
		}
		Set<VariableTerm> fv = t.freeVariables();
		fv.retainAll(varSet);
		if (!fv.isEmpty())
			return null;
		return t;
	}

	private synchronized Set<ImpureCandidate> localImpureCandidatesFor(Context context, Candidate candidate, VariableTerm variable,
			VariableTerm variableDependent)
	{
		ImpureCandidatesCacheMapKey key = new ImpureCandidatesCacheMapKey(context, candidate, variable, variableDependent);
		Set<ImpureCandidate> impures = impureCandidatesCacheMap.get(key);
		if (impures == null)
		{
			impures = new HashSet<ImpureCandidate>();
			Term type = candidate.getAntecedentMap().get(variableDependent);
			Term target = type.consequent();
			Collection<Statement> statements = statementCacheTree.getLocalStatementCollection(context);
			synchronized (statements)
			{
				for (Statement st : statements)
				{
					Term value = assignImpure(variable, target, st.getTerm());
					if (value != null)
						impures.add(new ImpureCandidate(candidate, variable, value));
				}
			}
			impureCandidatesCacheMap.put(key, impures);
			Set<ImpureCandidatesCacheMapKey> keySet = impureCandidatesCacheMapKeys.get(context);
			if (keySet == null)
			{
				keySet = new HashSet<ImpureCandidatesCacheMapKey>();
				impureCandidatesCacheMapKeys.put(context, keySet);
			}
			keySet.add(key);
		}
		return impures;
	}

	private Set<ImpureCandidate> virtualImpureCandidatesFor(List<VirtualStatement> virtualStatements, Candidate candidate, VariableTerm variable,
			VariableTerm variableDependent)
	{
		Set<ImpureCandidate> impures = new HashSet<ImpureCandidate>();
		Term type = candidate.getAntecedentMap().get(variableDependent);
		Term target = type.consequent();
		for (VirtualStatement vs : virtualStatements)
		{
			Term value = assignImpure(variable, target, vs.getTerm());
			if (value != null)
				impures.add(new ImpureCandidate(candidate, variable, value));
		}
		return impures;
	}

	public Set<ImpureCandidate> impureCandidatesFor(Context context, List<VirtualStatement> virtualStatements, Candidate candidate, VariableTerm variable,
			VariableTerm variableDependent)
	{
		Stack<Context> stack = new Stack<Context>();
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			while (!(context instanceof RootContext))
			{
				stack.push(context);
				context = context.getContext(transaction);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
		Set<ImpureCandidate> impureCandidates = localImpureCandidatesFor(context, candidate, variable, variableDependent);
		while (!stack.isEmpty())
			impureCandidates = new CombinedSet<ImpureCandidate>(localImpureCandidatesFor(stack.pop(), candidate, variable, variableDependent), impureCandidates);
		impureCandidates = new CombinedSet<ImpureCandidate>(virtualImpureCandidatesFor(virtualStatements, candidate, variable, variableDependent),
				impureCandidates);
		return impureCandidates;
	}

	public Set<ImpureCandidate> impureCandidatesFor(Context context, List<VirtualStatement> virtualStatements, Candidate candidate, VariableTerm variable)
	{
		Set<ImpureCandidate> impures = null;
		for (VariableTerm v : candidate.getAntecedentDependentMap().get(variable))
		{
			Set<ImpureCandidate> impures_ = impureCandidatesFor(context, virtualStatements, candidate, variable, v);
			if (impures == null)
				impures = new HashSet<ImpureCandidate>(impures_);
			else
				impures.retainAll(impures_);
		}
		return impures;
	}

	public void shutdown()
	{
		statementCacheTree.shutdown();
	}

	public synchronized void clearCache()
	{
		statementCacheTree.clear();
		pureCandidatesCacheMapKeys.clear();
		pureCandidatesCacheMap.clear();
		impureCandidatesCacheMapKeys.clear();
		impureCandidatesCacheMap.clear();
	}

	@Override
	public synchronized void newProvedStatement(Context context, Statement statement)
	{
		{
			Set<PureCandidatesCacheMapKey> keySet = pureCandidatesCacheMapKeys.remove(context);
			if (keySet != null)
				for (PureCandidatesCacheMapKey key : keySet)
					pureCandidatesCacheMap.remove(key);
		}
		{
			Set<ImpureCandidatesCacheMapKey> keySet = impureCandidatesCacheMapKeys.remove(context);
			if (keySet != null)
				for (ImpureCandidatesCacheMapKey key : keySet)
					impureCandidatesCacheMap.remove(key);
		}
	}

	@Override
	public synchronized void disProvedStatement(Context context, Statement statement)
	{
		{
			Set<PureCandidatesCacheMapKey> keySet = pureCandidatesCacheMapKeys.remove(context);
			if (keySet != null)
				for (PureCandidatesCacheMapKey key : keySet)
					pureCandidatesCacheMap.remove(key);
		}
		{
			Set<ImpureCandidatesCacheMapKey> keySet = impureCandidatesCacheMapKeys.remove(context);
			if (keySet != null)
				for (ImpureCandidatesCacheMapKey key : keySet)
					impureCandidatesCacheMap.remove(key);
		}
	}

	private class PureCandidatesCacheMapListener implements CacheWithCleanerMap.Listener<PureCandidatesCacheMapKey>
	{

		@Override
		public void keyCleaned(PureCandidatesCacheMapKey key)
		{
			Set<PureCandidatesCacheMapKey> set = pureCandidatesCacheMapKeys.get(key.context);
			if (set != null)
			{
				set.remove(key);
				if (set.isEmpty())
				{
					pureCandidatesCacheMapKeys.remove(key.context);
					if (!impureCandidatesCacheMapKeys.containsKey(key.context))
						contextWatcher.unwatchContext(key.context);
				}
			}
		}

	}

	private class ImpureCandidatesCacheMapListener implements CacheWithCleanerMap.Listener<ImpureCandidatesCacheMapKey>
	{

		@Override
		public void keyCleaned(ImpureCandidatesCacheMapKey key)
		{
			Set<ImpureCandidatesCacheMapKey> set = impureCandidatesCacheMapKeys.get(key.context);
			if (set != null)
			{
				set.remove(key);
				if (set.isEmpty())
				{
					impureCandidatesCacheMapKeys.remove(key.context);
					if (!pureCandidatesCacheMapKeys.containsKey(key.context))
						contextWatcher.unwatchContext(key.context);
				}
			}
		}

	}

}
