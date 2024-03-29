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
package aletheia.model.term;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.CompositionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
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

		protected CompositionTypeException()
		{
			super();
		}

		protected CompositionTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected CompositionTypeException(String message)
		{
			super(message);
		}

		protected CompositionTypeException(Throwable cause)
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

	@Override
	public int size()
	{
		return head.size() + tail.size();
	}

	/**
	 * A term is replaced simply composing the replacement of the parts (head
	 * and tail).
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
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		try
		{
			return head.replace(replaces).compose(tail.replace(replaces));
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

	/**
	 * The textual representation of a composition is just the concatenation of
	 * the terms which compose it.
	 *
	 * <p>
	 * If the tail is a composition, it is represented inside a couple of
	 * parentheses since it the composition is interpreted to be
	 * left-associative.
	 * </p>
	 *
	 */
	@Override
	protected void stringAppend(StringAppender stringAppender, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		CompositionParameterIdentification headParameterIdentification = null;
		ParameterIdentification tailParameterIdentification = null;
		if (parameterIdentification instanceof CompositionParameterIdentification)
		{
			headParameterIdentification = ((CompositionParameterIdentification) parameterIdentification).getHead();
			tailParameterIdentification = ((CompositionParameterIdentification) parameterIdentification).getTail();
		}
		head.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, headParameterIdentification);
		stringAppender.append(" ");
		stringAppender.openSub();
		if (tail instanceof CompositionTerm)
			stringAppender.append("(");
		tail.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, tailParameterIdentification);
		if (tail instanceof CompositionTerm)
			stringAppender.append(")");
		stringAppender.closeSub();
	}

	/**
	 * Both the head and the tail must be equal.
	 */
	@Override
	protected boolean equals(Term term, Map<ParameterVariableTerm, ParameterVariableTerm> parameterMap)
	{
		if (!(term instanceof CompositionTerm))
			return false;
		CompositionTerm compositionTerm = (CompositionTerm) term;
		if (!head.equals(compositionTerm.head, parameterMap) || !tail.equals(compositionTerm.tail, parameterMap))
			return false;
		return true;
	}

	@Override
	protected int hashCode(int hasher)
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

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return head.isFreeVariable(variable) || tail.isFreeVariable(variable);
	}

	public class DiffInfoComposition extends DiffInfoNotEqual
	{
		public final DiffInfo diffHead;
		public final DiffInfo diffTail;

		protected DiffInfoComposition(CompositionTerm other, DiffInfo diffHead, DiffInfo diffTail)
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

	@Override
	public boolean castFree()
	{
		return getHead().castFree() && getTail().castFree();
	}

	@Override
	protected AtomicTerm atom()
	{
		return getHead().atom();
	}

	@Override
	public CompositionParameterIdentification makeParameterIdentification(Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
	{
		ParameterIdentification preHeadParameterIdentification = getHead().makeParameterIdentification(parameterIdentifiers);
		CompositionParameterIdentification headParameterIdentification;
		if (preHeadParameterIdentification instanceof CompositionParameterIdentification)
			headParameterIdentification = (CompositionParameterIdentification) preHeadParameterIdentification;
		else
			headParameterIdentification = CompositionParameterIdentification.make(null, preHeadParameterIdentification);
		ParameterIdentification tailParameterIdentification = getTail().makeParameterIdentification(parameterIdentifiers);
		return CompositionParameterIdentification.make(headParameterIdentification, tailParameterIdentification);
	}

	@Override
	protected void populateDomainParameterIdentificationMap(ParameterIdentification parameterIdentification,
			Map<ParameterVariableTerm, DomainParameterIdentification> domainParameterIdentificationMap)
	{
		if (parameterIdentification instanceof CompositionParameterIdentification)
		{
			CompositionParameterIdentification compositionParameterIdentification = (CompositionParameterIdentification) parameterIdentification;
			getTail().populateDomainParameterIdentificationMap(compositionParameterIdentification.getTail(), domainParameterIdentificationMap);
			getHead().populateDomainParameterIdentificationMap(compositionParameterIdentification.getHead(), domainParameterIdentificationMap);
		}
	}

	public class SearchInfoComposition extends SearchInfo
	{
		public final SearchInfo searchHead;
		public final SearchInfo searchTail;

		protected SearchInfoComposition(SearchInfo searchHead, SearchInfo searchTail)
		{
			this.searchHead = searchHead;
			this.searchTail = searchTail;
		}

		@Override
		public String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			if (tail instanceof CompositionTerm)
				return searchHead.toString(variableToIdentifier, parameterNumerator) + " (" + searchTail.toString(variableToIdentifier, parameterNumerator)
						+ ")";
			else
				return searchHead.toString(variableToIdentifier, parameterNumerator) + " " + searchTail.toString(variableToIdentifier, parameterNumerator);
		}

	}

	@Override
	public SearchInfo search(Term sub)
	{
		SearchInfo si = super.search(sub);
		if (si instanceof SearchInfoFound)
			return si;

		SearchInfo siHead = getHead().search(sub);
		SearchInfo siTail = getTail().search(sub);
		if ((siHead instanceof SearchInfoNotFound) && (siTail instanceof SearchInfoNotFound))
			return new SearchInfoNotFound();

		return new SearchInfoComposition(siHead, siTail);
	}

}
