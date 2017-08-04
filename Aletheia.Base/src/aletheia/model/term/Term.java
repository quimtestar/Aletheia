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
package aletheia.model.term;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.model.term.ProjectionTerm.ProjectionTypeException;
import aletheia.persistence.Transaction;
import aletheia.protocol.Exportable;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.ReverseList;

/**
 * <p>
 * Abstract immutable representation of a mathematical expression (in the sense
 * of <a>http://en.wikipedia.org/wiki/Mathematical_expression</a>). The main
 * property of a term is its type, which is itself another term. You can get the
 * type of any term by calling the method {@link #type}.
 * </p>
 * <p>
 * The type relation between terms is acyclic, and some terms have no defined
 * type. (e.g. the primitive type or any function to the primitve type).
 * </p>
 *
 */
public abstract class Term implements Serializable, Exportable
{
	private static final long serialVersionUID = -8894330621996769111L;
	private final static int hashPrime = 2959609;

	private final Term type;

	private transient int hashCode;
	private transient boolean hashCoded = false;

	public Term(Term type)
	{
		super();
		this.type = type;
	}

	/**
	 * Base class for all the type-related exceptions for terms.
	 *
	 */
	public abstract static class TypeException extends Exception
	{
		private static final long serialVersionUID = 2182608565749310425L;

		public TypeException()
		{
			super();
		}

		public TypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public TypeException(String message)
		{
			super(message);
		}

		public TypeException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Computes the type of this term (another term).
	 *
	 * @return The type.
	 */
	public Term getType()
	{
		return type;
	}

	/**
	 *
	 * Exception related with the non-matching types of the replacing terms.
	 *
	 */
	public class ReplaceTypeException extends TypeException
	{
		private static final long serialVersionUID = 8300442083665283294L;

		public ReplaceTypeException()
		{
			super();
		}

		public ReplaceTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ReplaceTypeException(String message)
		{
			super(message);
		}

		public ReplaceTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Replace all occurrences of the <i>variable</i> in this term with the
	 * <i>term</i>.
	 *
	 * @param variable
	 *            The variable to be replaced.
	 * @param term
	 *            The value to be replaced with.
	 * @return The replaced term.
	 * @throws ReplaceTypeException
	 *             The term and the variable type doesn't match.
	 */
	public final Term replace(VariableTerm variable, Term term) throws ReplaceTypeException
	{
		return replace(Collections.singletonList(new Replace(variable, term)));
	}

	public final Term replace(Collection<Replace> replaces) throws ReplaceTypeException
	{
		return replace(new LinkedList<>(replaces), new HashSet<VariableTerm>());
	}

	/**
	 * Replace all occurrences of the <i>oldTerm</i> sub-term in this term with
	 * the <i>newTerm</i>.
	 *
	 * @param subterm
	 *            The sub-term to be replaced.
	 * @param replace
	 *            The value to be replaced with.
	 * @return The replaced term.
	 * @throws ReplaceTypeException
	 *             The <i>oldTerm</i> and the <i>newTerm</i> types don't match.
	 */
	// Might be unified with 'replace' methods above. Not sure about side effects.
	public Term replaceSubterm(Term subterm, Term replace) throws ReplaceTypeException
	{
		if (!subterm.getType().equals(replace.getType()))
			throw new ReplaceTypeException();
		if (equals(subterm))
			return replace;
		else
			return this;
	}

	/**
	 * Auxiliary class for performing replacements.
	 */
	public static class Replace
	{
		public final VariableTerm variable;
		public final Term term;

		public Replace(VariableTerm variable, Term term)
		{
			super();
			this.variable = variable;
			this.term = term;
		}

	}

	/**
	 * Perform a series of replacements on this term (auxiliary-internal
	 * method).
	 */
	protected abstract Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException;

	/**
	 * Two terms are equal by default.
	 *
	 * Must be overridden by subclasses.
	 *
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof Term))
			return false;
		return true;
	}

	@Override
	public final int hashCode()
	{
		if (!hashCoded)
		{
			hashCode = hashCode(hashPrime);
			hashCoded = true;
		}
		return hashCode;
	}

	public int hashCode(int hasher)
	{
		int ret = hasher * hashPrime;
		return ret;
	}

	/**
	 * Adds the free variables in this term to <i>freeVars</i>, considering
	 * <i>localVars</i> as bounded (allegedly, they are the parameter variables
	 * of function terms which this term is embedded in the body of).
	 *
	 * @param freeVars
	 *            The set of free variables.
	 * @param localVars
	 *            The set of already known local variables.
	 */
	protected abstract void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars);

	/**
	 * Returns the set of free variables in this term. A variable is free when
	 * it doesn't occur in the body of a function which the variable is the
	 * parameter of.
	 *
	 * @return The set of free variables.
	 */
	public Set<VariableTerm> freeVariables()
	{
		Set<VariableTerm> freeVars = new HashSet<>();
		freeVariables(freeVars, new HashSet<VariableTerm>());
		return freeVars;
	}

	/**
	 * The set of free variables as {@link IdentifiableVariableTerm}s. Set
	 * operations might throw a {@link ClassCastException} if the term has any
	 * free {@link VariableTerm} that is not an {@link IdentifiableVariableTerm}
	 * .
	 *
	 * @return The set of free {@link IdentifiableVariableTerm}s
	 */
	public Set<IdentifiableVariableTerm> freeIdentifiableVariables()
	{
		return new BijectionSet<>(new CastBijection<VariableTerm, IdentifiableVariableTerm>(), freeVariables());
	}

	public abstract String toString(Map<? extends VariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator);

	/**
	 * Converts term to {@link String} using the specified correspondence
	 * between variables and identifiers.
	 *
	 * @param variableToIdentifier
	 *            Mapping from variables to identifiers to use for the
	 *            conversion.
	 * @return this term converted to a String.
	 */
	public String toString(Map<? extends VariableTerm, Identifier> variableToIdentifier)
	{
		return toString(variableToIdentifier, parameterNumerator());
	}

	public String toString(Transaction transaction, Context context)
	{
		return toString(transaction, context, parameterNumerator());
	}

	public String toString(Transaction transaction, Context context, ParameterNumerator parameterNumerator)
	{
		return toString(context != null ? context.variableToIdentifier(transaction) : null, parameterNumerator);
	}

	/**
	 * Equivalent to {@link #toString(Map)} with an empty map.
	 *
	 * @see #toString(Map)
	 */
	@Override
	public String toString()
	{
		return toString(null);
	}

	/**
	 *
	 * Information about the result of a comparison of two given terms.
	 *
	 * @see Term#diff(Term)
	 *
	 */
	public abstract class DiffInfo
	{
		/**
		 * Initial textual mark of a difference.
		 */
		public static final String beginMark = "\u00bb";

		/**
		 * Final textual mark of a difference.
		 */
		public static final String endMark = "\u00ab";

		/**
		 * The other term of the comparison.
		 */
		protected final Term other;

		/**
		 * Constructs a {@link DiffInfo} for a comparison within <i>this</i> and
		 * <i>other</i>.
		 *
		 * @param other
		 *            The right part of the comparison.
		 */
		public DiffInfo(Term other)
		{
			super();
			this.other = other;
		}

		/**
		 * Same as {@link #toStringLeft(Map)} with an empty map.
		 */
		@Override
		public String toString()
		{
			return toStringLeft(Collections.<IdentifiableVariableTerm, Identifier> emptyMap());
		}

		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier)
		{
			return toStringLeft(variableToIdentifier, parameterNumerator());
		}

		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier)
		{
			return toStringRight(variableToIdentifier, parameterNumerator());
		}

		/**
		 * Shows the left part of the comparison as a {@link String}. Marks the
		 * differences to the other part using {@link #beginMark} and
		 * {@link #endMark}.
		 *
		 * @param variableToIdentifier
		 *            Mapping from variables to identifiers to use for the
		 *            conversion.
		 * @return The left part of the comparison as a {@link String}.
		 */
		public abstract String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator);

		/**
		 * Shows the right part of the comparison as a {@link String}. Marks the
		 * differences to the other part using {@link #beginMark} and
		 * {@link #endMark}.
		 *
		 * @param variableToIdentifier
		 *            Mapping from variables to identifiers to use for the
		 *            conversion.
		 * @return The right part of the comparison as a {@link String}.
		 */
		public abstract String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator);
	}

	/**
	 *
	 * {@link DiffInfo} for identical terms.
	 *
	 */
	public class DiffInfoEqual extends DiffInfo
	{

		public DiffInfoEqual(Term other)
		{
			super(other);
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return Term.this.toString(variableToIdentifier, parameterNumerator);
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return other.toString(variableToIdentifier, parameterNumerator);
		}

	}

	/**
	 * {@link DiffInfo} for absolutely different terms.
	 *
	 */
	public class DiffInfoNotEqual extends DiffInfo
	{
		public DiffInfoNotEqual(Term other)
		{
			super(other);
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return beginMark + Term.this.toString(variableToIdentifier, parameterNumerator) + endMark;
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return beginMark + other.toString(variableToIdentifier, parameterNumerator) + endMark;
		}
	}

	/**
	 * Computes the {@link DiffInfo} for the comparison between this term and
	 * the parameter.
	 *
	 * @param term
	 *            The term to compare with.
	 * @return The result of the comparison.
	 *
	 * @see DiffInfo
	 */
	public DiffInfo diff(Term term)
	{
		if (this == term)
			return new DiffInfoEqual(term);
		return null;
	}

	/**
	 * Type error when composing
	 *
	 * @see Term#compose(Term)
	 *
	 */
	public class ComposeTypeException extends TypeException
	{
		private static final long serialVersionUID = 5283308545833724895L;

		private final Term head;
		private final Term tail;

		public ComposeTypeException(Term head, Term tail)
		{
			super();
			this.head = head;
			this.tail = tail;
		}

		public ComposeTypeException(String message, Throwable cause, Term head, Term tail)
		{
			super(message, cause);
			this.head = head;
			this.tail = tail;
		}

		public ComposeTypeException(String message, Term head, Term tail)
		{
			super(message);
			this.head = head;
			this.tail = tail;
		}

		public ComposeTypeException(Throwable cause, Term head, Term tail)
		{
			super(cause);
			this.head = head;
			this.tail = tail;
		}

		public Term getHead()
		{
			return head;
		}

		public Term getTail()
		{
			return tail;
		}

	}

	/**
	 * Composes this term with another term.
	 *
	 *
	 * @param term
	 *            The term to compose with.
	 * @return The composed term.
	 * @throws ComposeTypeException
	 *             Type error when composing.
	 */
	public abstract Term compose(Term term) throws ComposeTypeException;

	public abstract SimpleTerm consequent(Collection<ParameterVariableTerm> parameters);

	/**
	 * The consequent of a term is defined to be itself if it's a simple term or
	 * the consequent of the body if it's a function
	 *
	 *
	 * @return The consequent of this term.
	 */
	public SimpleTerm consequent()
	{
		return consequent(null);
	}

	/**
	 * The list of parameter variables of this function.
	 */
	protected abstract void parameters(Collection<ParameterVariableTerm> parameters);

	public List<ParameterVariableTerm> parameters()
	{
		List<ParameterVariableTerm> parameters = new ArrayList<>();
		parameters(parameters);
		return parameters;

	}

	/**
	 * If a {@link FunctionTerm}, takes out every parameter which is independent
	 * from the body. If not, returns the same object unchanged.
	 */
	public Term dropIndependentParameters()
	{
		Term term = this;
		List<ParameterVariableTerm> parameters = new LinkedList<>();
		while (term instanceof FunctionTerm)
		{
			FunctionTerm function = (FunctionTerm) term;
			parameters.add(function.getParameter());
			term = function.getBody();
		}
		parameters.retainAll(term.freeVariables());
		for (ParameterVariableTerm param : new ReverseList<>(parameters))
			try
			{
				term = new FunctionTerm(param, term);
			}
			catch (NullParameterTypeException e)
			{
				throw new RuntimeException(e);
			}
		return term;
	}

	/**
	 * Exception thrown when <i>unprojecting</i> a term.
	 *
	 * @see Term#unproject()
	 */
	public class UnprojectTypeException extends TypeException
	{
		private static final long serialVersionUID = 6702315755038199240L;

		public UnprojectTypeException()
		{
			super();
		}

		public UnprojectTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UnprojectTypeException(String message)
		{
			super(message);
		}

		public UnprojectTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Computes the <i>unprojection</i> of this term. This is, converts all the
	 * projected functions of this terms to regular functions (and, if needed,
	 * composes them with the terms which they are composed with, since a
	 * projected function can be the head of a composition term and a regular
	 * function can't).
	 *
	 * @return This term unprojected.
	 * @throws UnprojectTypeException
	 */
	public abstract Term unproject() throws UnprojectTypeException;

	public abstract ProjectionTerm project() throws ProjectionTypeException;

	public class ParameterNumerator
	{
		private final Stack<ParameterVariableTerm> parameterStack;
		private final Map<ParameterVariableTerm, Stack<Integer>> parameterToNumberStackMap;

		protected ParameterNumerator()
		{
			this.parameterStack = new Stack<>();
			this.parameterToNumberStackMap = new HashMap<>();
		}

		public int numberParameter(ParameterVariableTerm parameter)
		{
			int number = parameterStack.size();
			parameterStack.push(parameter);
			Stack<Integer> numberStack = parameterToNumberStackMap.get(parameter);
			if (numberStack == null)
			{
				numberStack = new Stack<>();
				parameterToNumberStackMap.put(parameter, numberStack);
			}
			numberStack.push(number);
			return number;
		}

		public boolean isEmpty()
		{
			return parameterStack.isEmpty();
		}

		public ParameterVariableTerm unNumberParameter()
		{
			ParameterVariableTerm parameter = parameterStack.pop();
			Stack<Integer> numberStack = parameterToNumberStackMap.get(parameter);
			numberStack.pop();
			if (numberStack.isEmpty())
				parameterToNumberStackMap.remove(parameter);
			return parameter;
		}

		public class NotNumberedException extends RuntimeException
		{
			private static final long serialVersionUID = 486154559108015069L;

		}

		public int parameterNumber(ParameterVariableTerm parameter) throws NotNumberedException
		{
			Stack<Integer> numberStack = parameterToNumberStackMap.get(parameter);
			if (numberStack == null)
				throw new NotNumberedException();
			return numberStack.peek();
		}

		public boolean isNumbered(ParameterVariableTerm parameter)
		{
			Stack<Integer> numberStack = parameterToNumberStackMap.get(parameter);
			if (numberStack == null)
				return false;
			return true;
		}
	}

	public ParameterNumerator parameterNumerator()
	{
		return new ParameterNumerator();
	}

	public static class Match
	{
		private final Map<VariableTerm, Term> assignMapLeft;
		private final Map<VariableTerm, Term> assignMapRight;

		private Match(Map<VariableTerm, Term> assignMapLeft, Map<VariableTerm, Term> assignMapRight)
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

	public Match match(Collection<? extends VariableTerm> assignableVars, Term term)
	{
		return match(assignableVars, term, Collections.<VariableTerm> emptySet());
	}

	public Match match(Collection<? extends VariableTerm> assignableVarsLeft, Term termRight, Collection<? extends VariableTerm> assignableVarsRight)
	{
		Term termLeft = this;
		Map<VariableTerm, Term> assignMapLeft = new HashMap<>();
		Map<VariableTerm, Term> assignMapRight = new HashMap<>();

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

		List<Replace> replaceLeft = new ArrayList<>();
		List<Replace> replaceRight = new ArrayList<>();

		Stack<StackEntry> stack = new Stack<>();
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
								if (e.termRight instanceof TauTerm)
									return null;
								try
								{
									if (!var.getType().replace(replaceLeft).equals(e.termRight.getType()))
										return null;
								}
								catch (ReplaceTypeException e1)
								{
									throw new Error(e1);
								}
								assignMapLeft.put(var, e.termRight);
								replaceLeft.add(new Term.Replace(var, e.termRight));
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
								if (e.termLeft instanceof TauTerm)
									return null;
								try
								{
									if (!var.getType().replace(replaceRight).equals(e.termLeft.getType()))
										return null;
								}
								catch (ReplaceTypeException e1)
								{
									throw new Error(e1);
								}
								assignMapRight.put(var, e.termLeft);
								replaceRight.add(new Term.Replace(var, e.termLeft));
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
					Term termLeftR;
					try
					{
						termLeftR = e.termLeft.replace(replaceLeft);
					}
					catch (ReplaceTypeException e1)
					{
						termLeftR = e.termLeft;
					}
					Term termRightR;
					try
					{
						termRightR = e.termRight.replace(replaceRight);
					}
					catch (ReplaceTypeException e1)
					{
						termRightR = e.termRight;
					}
					if (!termLeftR.equals(termRightR))
					{
						if (termLeftR instanceof FunctionTerm)
						{
							if (!(termRightR instanceof FunctionTerm))
								return null;
							FunctionTerm fl = (FunctionTerm) termLeftR;
							FunctionTerm fr = (FunctionTerm) termRightR;
							stack.push(new StackEntry(e.parameterCorrespondence, fl.getParameter().getType(), fr.getParameter().getType()));
							stack.push(new StackEntry(new ParameterCorrespondence(e.parameterCorrespondence, fl.getParameter(), fr.getParameter()),
									fl.getBody(), fr.getBody()));
						}
						else if (termLeftR instanceof CompositionTerm)
						{
							if (!(termRightR instanceof CompositionTerm))
								return null;
							CompositionTerm cl = (CompositionTerm) termLeftR;
							CompositionTerm cr = (CompositionTerm) termRightR;
							stack.push(new StackEntry(e.parameterCorrespondence, cl.getTail(), cr.getTail()));
							stack.push(new StackEntry(e.parameterCorrespondence, cl.getHead(), cr.getHead()));
						}
						else if (termLeftR instanceof ProjectionTerm)
						{
							if (!(termRightR instanceof ProjectionTerm))
								return null;
							FunctionTerm fl = ((ProjectionTerm) termLeftR).getFunction();
							FunctionTerm fr = ((ProjectionTerm) termRightR).getFunction();
							stack.push(new StackEntry(e.parameterCorrespondence, fl, fr));
						}
						else
							return null;
					}
				}
			}
		}

		return new Match(assignMapLeft, assignMapRight);

	}

}
