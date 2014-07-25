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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.TTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;

public class TermMatcher
{
	public static class TermMatch
	{
		private final Map<VariableTerm, Term> assignMapLeft;
		private final Map<VariableTerm, Term> assignMapRight;

		public TermMatch(Map<VariableTerm, Term> assignMapLeft, Map<VariableTerm, Term> assignMapRight)
		{
			super();
			this.assignMapLeft = assignMapLeft;
			this.assignMapRight = assignMapRight;
		}

		public Map<VariableTerm, Term> getAssignMapLeft()
		{
			return Collections.unmodifiableMap(assignMapLeft);
		}

		public Map<VariableTerm, Term> getAssignMapRight()
		{
			return Collections.unmodifiableMap(assignMapRight);
		}
	}

	public static TermMatch match(Term termLeft, Set<VariableTerm> assignableVarsLeft, Term termRight, Set<VariableTerm> assignableVarsRight)
	{
		Map<VariableTerm, Term> assignMapLeft = new HashMap<VariableTerm, Term>();
		Map<VariableTerm, Term> assignMapRight = new HashMap<VariableTerm, Term>();

		class ParameterCorrespondence
		{
			public final ParameterCorrespondence parent;
			public final VariableTerm varLeft;
			public final VariableTerm varRight;

			public ParameterCorrespondence(ParameterCorrespondence parent, VariableTerm varLeft, VariableTerm varRight)
			{
				super();
				this.parent = parent;
				this.varLeft = varLeft;
				this.varRight = varRight;
			}
		}
		;

		class StackEntry
		{
			public final Term termLeft;
			public final Term termRight;
			public final ParameterCorrespondence parameterCorrespondence;

			public StackEntry(ParameterCorrespondence parameterCorrespondence, Term termLeft, Term termRight)
			{
				super();
				this.parameterCorrespondence = parameterCorrespondence;
				this.termLeft = termLeft;
				this.termRight = termRight;
			}

			public StackEntry(Term termLeft, Term termRight)
			{
				this(null, termLeft, termRight);
			}
		}
		;

		Stack<StackEntry> stack = new Stack<StackEntry>();
		stack.push(new StackEntry(termLeft, termRight));

		while (!stack.isEmpty())
		{
			StackEntry e = stack.pop();
			if (!e.termLeft.equals(e.termRight))
			{
				boolean processed = false;
				if (!processed && e.termLeft instanceof VariableTerm)
				{
					processed = true;
					VariableTerm var = (VariableTerm) e.termLeft;
					ParameterCorrespondence pc = e.parameterCorrespondence;
					while (pc != null)
					{
						if (pc.varLeft.equals(var))
							break;
						pc = pc.parent;
					}
					if ((pc == null) || (!pc.varRight.equals(e.termRight)))
					{
						if (assignableVarsLeft.contains(var))
						{
							Term t = assignMapLeft.get(var);
							if (t == null)
							{
								if ((e.termRight instanceof TTerm) || !var.getType().equals(e.termRight.getType()))
									return null;
								assignMapLeft.put(var, e.termRight);
							}
							else
								stack.push(new StackEntry(e.parameterCorrespondence, t, e.termRight));
						}
						else
							processed = false;
					}
				}
				if (!processed && (e.termRight instanceof VariableTerm))
				{
					processed = true;
					VariableTerm var = (VariableTerm) e.termRight;
					ParameterCorrespondence pc = e.parameterCorrespondence;
					while (pc != null)
					{
						if (pc.varRight.equals(var))
							break;
						pc = pc.parent;
					}
					if ((pc == null) || (!pc.varLeft.equals(e.termLeft)))
					{
						if (assignableVarsRight.contains(var))
						{
							Term t = assignMapRight.get(var);
							if (t == null)
							{
								if ((e.termLeft instanceof TTerm) || (!var.getType().equals(e.termLeft.getType())))
									return null;
								assignMapRight.put(var, e.termLeft);
							}
							else
								stack.push(new StackEntry(e.parameterCorrespondence, e.termLeft, t));
						}
						else
							processed = false;
					}
				}
				if (!processed)
				{
					if (e.termLeft instanceof FunctionTerm)
					{
						if (!(e.termRight instanceof FunctionTerm))
							return null;
						FunctionTerm fl = (FunctionTerm) e.termLeft;
						FunctionTerm fr = (FunctionTerm) e.termRight;
						stack.push(new StackEntry(e.parameterCorrespondence, fl.getParameter().getType(), fr.getParameter().getType()));
						stack.push(new StackEntry(new ParameterCorrespondence(e.parameterCorrespondence, fl.getParameter(), fr.getParameter()), fl.getBody(),
								fr.getBody()));
					}
					else if (e.termLeft instanceof CompositionTerm)
					{
						if (!(e.termRight instanceof CompositionTerm))
							return null;
						CompositionTerm cl = (CompositionTerm) e.termLeft;
						CompositionTerm cr = (CompositionTerm) e.termRight;
						stack.push(new StackEntry(e.parameterCorrespondence, cl.getHead(), cr.getHead()));
						stack.push(new StackEntry(e.parameterCorrespondence, cl.getTail(), cr.getTail()));
					}
					else
						return null;
				}
			}
		}

		return new TermMatch(assignMapLeft, assignMapRight);

	}

}
