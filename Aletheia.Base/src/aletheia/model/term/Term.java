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
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.model.identifier.Identifier;
import aletheia.protocol.Exportable;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CastBijection;

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
		return replace(new LinkedList<Replace>(replaces), new HashSet<VariableTerm>());
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
		Set<VariableTerm> freeVars = new HashSet<VariableTerm>();
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

	/**
	 * Converts term to {@link String} using the specified correspondence
	 * between variables and identifiers.
	 *
	 * @param variableToIdentifier
	 *            Mapping from variables to identifiers to use for the
	 *            conversion.
	 * @return this term converted to a String.
	 */
	public String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier)
	{
		return toString(variableToIdentifier, parameterNumerator());
	}

	public abstract String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator);

	/**
	 * Equivalent to {@link #toString(Map)} with an empty map.
	 *
	 * @see #toString(Map)
	 */
	@Override
	public String toString()
	{
		return toString(Collections.<IdentifiableVariableTerm, Identifier> emptyMap());
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

	/**
	 * The consequent of a term is defined to be itself if it's a simple term or
	 * the consequent of the body if it's a function
	 *
	 *
	 * @return The consequent of this term.
	 */
	public abstract SimpleTerm consequent();

	/**
	 * Exception thrown when <i>unprojecting</i> a term.
	 *
	 * @see Term#unproject()
	 */
	public class UnprojectException extends TypeException
	{
		private static final long serialVersionUID = 6702315755038199240L;

		public UnprojectException()
		{
			super();
		}

		public UnprojectException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UnprojectException(String message)
		{
			super(message);
		}

		public UnprojectException(Throwable cause)
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
	 * @throws UnprojectException
	 */
	public abstract Term unproject() throws UnprojectException;

	public class ParameterNumerator
	{
		private final Stack<ParameterVariableTerm> parameterStack;
		private final Map<ParameterVariableTerm, Stack<Integer>> parameterToNumberStackMap;

		protected ParameterNumerator()
		{
			this.parameterStack = new Stack<ParameterVariableTerm>();
			this.parameterToNumberStackMap = new HashMap<ParameterVariableTerm, Stack<Integer>>();
		}

		public int numberParameter(ParameterVariableTerm parameter)
		{
			int number = parameterStack.size();
			parameterStack.push(parameter);
			Stack<Integer> numberStack = parameterToNumberStackMap.get(parameter);
			if (numberStack == null)
			{
				numberStack = new Stack<Integer>();
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

}
