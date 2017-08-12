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

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.utilities.collections.TailList;

/**
 * A composition is composed by two parts; a head, which must be an
 * {@link SimpleTerm}, and a tail which can be any kind of {@link Term}.
 *
 * <p>
 * The textual representation of a composition is just the concatenation of the
 * terms which compose it.
 * </p>
 *
 */
public class CompositionTerm extends SimpleTerm
{
	private static final long serialVersionUID = 5555587382162058915L;
	private final static int hashPrime = 2959673;

	private final SimpleTerm head;
	private final Term tail;

	public static class CompositionTypeException extends TypeException
	{
		private static final long serialVersionUID = 8530964165091126355L;

		public CompositionTypeException()
		{
			super();
		}

		public CompositionTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public CompositionTypeException(String message)
		{
			super(message);
		}

		public CompositionTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	private static Term computeType(SimpleTerm head, Term tail) throws CompositionTypeException
	{
		if (head.getType() == null)
			throw new CompositionTypeException("Composition's head has no type");
		try
		{
			return head.getType().compose(tail);
		}
		catch (ComposeTypeException e)
		{
			throw new CompositionTypeException(e.getMessage(), e);
		}
		finally
		{

		}

	}

	/**
	 * Create a new composition with the specified head and tail.
	 *
	 * The type of the composition will be the same as the type of the head.
	 *
	 * @param head
	 *            The head.
	 * @param tail
	 *            The tail.
	 * @throws CompositionTypeException
	 */
	public CompositionTerm(SimpleTerm head, Term tail) throws CompositionTypeException
	{
		super(computeType(head, tail));
		this.head = head;
		this.tail = tail;
	}

	/**
	 *
	 * @return The head.
	 */
	public SimpleTerm getHead()
	{
		return head;
	}

	/**
	 *
	 * @return The tail.
	 */
	public Term getTail()
	{
		return tail;
	}

	/**
	 * A term is replaced simply composing the replacement of the parts (head and
	 * tail).
	 */
	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		Term headRep = head.replace(replaces, exclude);
		Term tailRep = tail.replace(replaces, exclude);
		if (headRep.equals(head) && tailRep.equals(tail))
			return this;
		try
		{
			return headRep.compose(tailRep);
		}
		catch (ComposeTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	@Override
	public Term replaceSubterm(Term subterm, Term replace) throws ReplaceTypeException
	{
		Term replaced = super.replaceSubterm(subterm, replace);
		if (replaced != this)
			return replaced;

		Term headRep = head.replaceSubterm(subterm, replace);
		Term tailRep = tail.replaceSubterm(subterm, replace);
		if (headRep.equals(head) && tailRep.equals(tail))
			return this;
		try
		{
			return headRep.compose(tailRep);
		}
		catch (ComposeTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	public static class CompositionParameterIdentification extends ParameterIdentification
	{
		private final CompositionParameterIdentification head;
		private final ParameterIdentification tail;

		public CompositionParameterIdentification(CompositionParameterIdentification head, ParameterIdentification tail)
		{
			super();
			this.head = head;
			this.tail = tail;
		}

		public CompositionParameterIdentification getHead()
		{
			return head;
		}

		public ParameterIdentification getTail()
		{
			return tail;
		}

	}

	/**
	 * The textual representation of a composition is just the concatenation of the
	 * terms which compose it.
	 *
	 * <p>
	 * If the tail is a composition, it is represented inside a couple of
	 * parentheses since it the composition is interpreted to be left-associative.
	 * </p>
	 *
	 */
	@Override
	public String toString(Map<? extends VariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator,
			ParameterIdentification parameterIdentification)
	{
		CompositionParameterIdentification headParameterIdentification = null;
		ParameterIdentification tailParameterIdentification = null;
		if (parameterIdentification instanceof CompositionParameterIdentification)
		{
			headParameterIdentification = ((CompositionParameterIdentification) parameterIdentification).getHead();
			tailParameterIdentification = ((CompositionParameterIdentification) parameterIdentification).getTail();
		}
		String sHead = head.toString(variableToIdentifier, parameterNumerator, headParameterIdentification);
		String sTail_ = tail.toString(variableToIdentifier, parameterNumerator, tailParameterIdentification);
		String sTail = tail instanceof CompositionTerm ? "(" + sTail_ + ")" : sTail_;
		return sHead + " " + sTail;
	}

	/**
	 * Both the head and the tail must be equal.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof CompositionTerm))
			return false;
		if (!super.equals(obj))
			return false;
		CompositionTerm compositionTerm = (CompositionTerm) obj;
		if (!head.equals(compositionTerm.head))
			return false;
		if (!tail.equals(compositionTerm.tail))
			return false;
		return true;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + head.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + tail.hashCode(hasher *= hashPrime);
		return ret;
	}

	/**
	 * The union of the free variables in the head and the tail.
	 */
	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		head.freeVariables(freeVars, localVars);
		tail.freeVariables(freeVars, localVars);
	}

	public class DiffInfoComposition extends DiffInfoNotEqual
	{
		public final DiffInfo diffHead;
		public final DiffInfo diffTail;

		public DiffInfoComposition(CompositionTerm other, DiffInfo diffHead, DiffInfo diffTail)
		{
			super(other);
			this.diffHead = diffHead;
			this.diffTail = diffTail;
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			if (tail instanceof CompositionTerm)
				return diffHead.toStringLeft(variableToIdentifier, parameterNumerator) + " (" + diffTail.toStringLeft(variableToIdentifier, parameterNumerator)
						+ ")";
			else
				return diffHead.toStringLeft(variableToIdentifier, parameterNumerator) + " " + diffTail.toStringLeft(variableToIdentifier, parameterNumerator);
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			if (((CompositionTerm) other).getTail() instanceof CompositionTerm)
				return diffHead.toStringRight(variableToIdentifier, parameterNumerator) + " ("
						+ diffTail.toStringRight(variableToIdentifier, parameterNumerator) + ")";
			else
				return diffHead.toStringRight(variableToIdentifier, parameterNumerator) + " "
						+ diffTail.toStringRight(variableToIdentifier, parameterNumerator);
		}
	}

	@Override
	public DiffInfo diff(Term term)
	{
		DiffInfo di = super.diff(term);
		if (di != null)
			return di;
		if (!(term instanceof CompositionTerm))
			return new DiffInfoNotEqual(term);
		CompositionTerm comp = (CompositionTerm) term;
		DiffInfo diffHead = getHead().diff(comp.getHead());
		DiffInfo diffTail = getTail().diff(comp.getTail());
		if ((diffHead instanceof DiffInfoEqual) && (diffTail instanceof DiffInfoEqual))
			return new DiffInfoEqual(comp);
		return new DiffInfoComposition(comp, diffHead, diffTail);
	}

	/**
	 * The length of a composition term is the sum of the length of the parts.
	 */
	@Override
	public int length()
	{
		return head.length() + tail.consequent().length();
	}

	/**
	 * The unprojection of a composition is the composition of the unprojected
	 * parts.
	 */
	@Override
	public Term unproject() throws UnprojectTypeException
	{
		Term headp = head.unproject();
		try
		{
			return headp.compose(tail);
		}
		catch (ComposeTypeException e)
		{
			throw new UnprojectTypeException(e);
		}
		finally
		{
		}
	}

	@Override
	public SimpleTerm head()
	{
		return head;
	}

	@Override
	public List<Term> components()
	{
		return new TailList<>(head.components(), tail);
	}

	@Override
	public List<Term> aggregateComponents()
	{
		return new TailList<>(head.aggregateComponents(), this);
	}

}
