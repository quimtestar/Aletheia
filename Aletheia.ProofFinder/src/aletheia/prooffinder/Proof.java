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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedCollection;

public class Proof
{
	private final QueueSubEntry subEntry;

	static abstract class SolvedCandidate
	{
		private final Candidate candidate;

		SolvedCandidate(Candidate candidate)
		{
			super();
			this.candidate = candidate;
		}

		Candidate getCandidate()
		{
			return candidate;
		}

		abstract Collection<QueueSubEntry> descendants();

	}

	static class PureSolvedCandidate extends SolvedCandidate
	{
		private final Map<VariableTerm, PureQueueSubEntry> descendants;

		PureSolvedCandidate(Candidate candidate)
		{
			super(candidate);
			this.descendants = new HashMap<>();
		}

		void putDescendant(VariableTerm var, PureQueueSubEntry se)
		{
			descendants.put(var, se);
		}

		PureQueueSubEntry getDescendant(VariableTerm var)
		{
			return descendants.get(var);
		}

		@Override
		Collection<QueueSubEntry> descendants()
		{
			return new AdaptedCollection<>(descendants.values());
		}

	}

	static class ImpureSolvedCandidate extends SolvedCandidate
	{
		private final ImpureQueueSubEntry descendant;

		public ImpureSolvedCandidate(Candidate candidate, ImpureQueueSubEntry descendant)
		{
			super(candidate);
			this.descendant = descendant;
		}

		ImpureQueueSubEntry getDescendant()
		{
			return descendant;
		}

		@Override
		Collection<QueueSubEntry> descendants()
		{
			return Collections.<QueueSubEntry> singleton(descendant);
		}

	}

	private final Map<QueueSubEntry, SolvedCandidate> solvedCandidates;

	Proof(QueueSubEntry subEntry)
	{
		this.subEntry = subEntry;
		this.solvedCandidates = new HashMap<>();
	}

	Proof(Proof proof)
	{
		this.subEntry = proof.subEntry;
		this.solvedCandidates = new HashMap<>(proof.solvedCandidates);
	}

	boolean containsQueueSubEntry(QueueSubEntry se)
	{
		return solvedCandidates.containsKey(se);
	}

	SolvedCandidate getSolvedCandidate(QueueSubEntry se)
	{
		return solvedCandidates.get(se);
	}

	PureSolvedCandidate getPureSolvedCandidate(QueueSubEntry se)
	{
		SolvedCandidate sc = getSolvedCandidate(se);
		while (sc instanceof ImpureSolvedCandidate)
			sc = getSolvedCandidate(((ImpureSolvedCandidate) sc).getDescendant());
		return (PureSolvedCandidate) sc;
	}

	SolvedCandidate putSolvedCandidate(QueueSubEntry se, SolvedCandidate sole)
	{
		return solvedCandidates.put(se, sole);
	}

	public class ExecutionException extends Exception
	{
		private static final long serialVersionUID = -9095996133405676249L;

		public ExecutionException()
		{
			super();
		}

		public ExecutionException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ExecutionException(String message)
		{
			super(message);
		}

		public ExecutionException(Throwable cause)
		{
			super(cause);
		}

	}

	public void execute(Transaction transaction) throws ExecutionException
	{
		class StackEntry
		{
			final public Context ctx;
			final public QueueSubEntry se;

			public StackEntry(Context ctx, QueueSubEntry se)
			{
				super();
				this.ctx = ctx;
				this.se = se;
			}
		}

		Map<VirtualStatement, Assumption> assumptionMap = new HashMap<>();

		Stack<StackEntry> stack = new Stack<>();
		stack.push(new StackEntry(subEntry.getContext(), subEntry));

		while (!stack.isEmpty())
		{
			try
			{
				StackEntry e = stack.pop();
				for (VirtualStatement vs : e.se.localVirtualStatements())
				{
					Assumption a = e.ctx.assumptions(transaction).get(vs.getOrder());
					assumptionMap.put(vs, a);
				}
				PureSolvedCandidate psc = getPureSolvedCandidate(e.se);
				Candidate c = psc.getCandidate();
				Statement st;
				PureCandidate pc = c.getPureCandidate();
				if (pc instanceof StatementCandidate)
					st = ((StatementCandidate) pc).getStatement();
				else if (pc instanceof VirtualStatementCandidate)
				{
					VirtualStatement vs = ((VirtualStatementCandidate) pc).getVirtualStatement();
					st = assumptionMap.get(vs);
				}
				else
					throw new Error();

				for (VariableTerm var : c.getVarList())
				{
					Term t_ = c.getAssignMap().get(var);
					if (t_ == null)
					{
						Term term = c.getAntecedentMap().get(var);
						Set<VariableTerm> fv = term.freeVariables();
						for (VirtualStatement vs : e.se.virtualStatements())
						{
							if (fv.contains(vs.getVariable()))
								term = term.replace(vs.getVariable(), assumptionMap.get(vs).getVariable());

						}

						Statement solver = null;
						for (Statement stsol : e.ctx.statementsByTerm(transaction).get(term).toArray(new Statement[0]))
						{
							if (stsol.isProved())
							{
								solver = stsol;
								break;
							}
						}
						if (solver == null)
						{
							Context ctx_ = e.ctx.openSubContext(transaction, term);
							stack.push(new StackEntry(ctx_, psc.getDescendant(var)));
							solver = ctx_;
						}

						t_ = solver.getVariable();
					}
					else
					{
						Set<VariableTerm> fv = t_.freeVariables();
						for (VirtualStatement vs : e.se.virtualStatements())
						{
							if (fv.contains(vs.getVariable()))
								t_ = t_.replace(vs.getVariable(), assumptionMap.get(vs).getVariable());

						}
					}

					Statement instanceProof = e.ctx.statements(transaction).get(t_);
					if (instanceProof == null)
						instanceProof = e.ctx.suitableForInstanceProofStatementByTerm(transaction, t_);
					if (instanceProof == null)
						throw new ExecutionException();

					st = e.ctx.specialize(transaction, st, t_, instanceProof);
				}
			}
			catch (ReplaceTypeException e)
			{
				throw new ExecutionException(e);
			}
			catch (StatementException e)
			{
				throw new ExecutionException(e);
			}
			finally
			{
			}
		}

	}

	public boolean existsPath(QueueSubEntry qse0, QueueSubEntry qse1)
	{
		Set<QueueSubEntry> visited = new HashSet<>();
		Queue<QueueSubEntry> queue = new LinkedList<>();
		queue.add(qse0);
		while (!queue.isEmpty())
		{
			QueueSubEntry qse = queue.poll();
			if (!visited.contains(qse))
			{
				visited.add(qse);
				if (qse.equals(qse1))
					return true;
				SolvedCandidate sc = solvedCandidates.get(qse);
				if (sc != null)
					queue.addAll(sc.descendants());
			}
		}
		return false;
	}

}
