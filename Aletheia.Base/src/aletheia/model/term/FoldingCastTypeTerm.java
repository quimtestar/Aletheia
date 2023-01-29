/*******************************************************************************
 * Copyright (c) 2018, 2019 Quim Testar
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
import java.util.Map;
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.ParameterIdentification;

public class FoldingCastTypeTerm extends CastTypeTerm
{
	private static final long serialVersionUID = -9215582833035602649L;
	private final static int hashPrime = 2964509;

	private final IdentifiableVariableTerm variable;
	private final Term value;

	public static class FoldingCastTypeException extends CastTypeException
	{
		private static final long serialVersionUID = 6533505214214035548L;

		protected FoldingCastTypeException()
		{
			super();
		}

		protected FoldingCastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected FoldingCastTypeException(String message)
		{
			super(message);
		}

		protected FoldingCastTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	public FoldingCastTypeTerm(Term term, Term type, IdentifiableVariableTerm variable, Term value) throws FoldingCastTypeException
	{
		super(type, term);
		this.variable = variable;
		this.value = value;
		try
		{
			if (!type.replace(variable, value).equals(term.getType()))
				throw new FoldingCastTypeException("Folding type does not match");
		}
		catch (ReplaceTypeException e)
		{
			throw new FoldingCastTypeException(e);
		}
	}

	public IdentifiableVariableTerm getVariable()
	{
		return variable;
	}

	public Term getValue()
	{
		return value;
	}

	@Override
	protected int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + variable.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + value.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	protected boolean equals(Term term, Map<ParameterVariableTerm, ParameterVariableTerm> parameterMap)
	{
		if (this == term)
			return true;
		if (!(term instanceof FoldingCastTypeTerm) || !super.equals(term, parameterMap))
			return false;
		FoldingCastTypeTerm castTypeTerm = (FoldingCastTypeTerm) term;
		if (!variable.equals(castTypeTerm.variable) || !value.equals(castTypeTerm.value))
			return false;
		return true;
	}

	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return new FoldingCastTypeTerm(getTerm().replace(replaces, exclude), getType().replace(replaces, exclude), variable, value);
		}
		catch (FoldingCastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		try
		{
			return new FoldingCastTypeTerm(getTerm().replace(replaces), getType().replace(replaces), variable, value);
		}
		catch (FoldingCastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}

	}

	@Override
	protected void stringAppend(StringAppender stringAppender, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		stringAppender.append("(");
		stringAppender.openSub();
		getTerm().stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.closeSub();
		stringAppender.openSub();
		stringAppender.append(":");
		getType().stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.closeSub();
		stringAppender.append(" | ");
		stringAppender.openSub();
		value.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.closeSub();
		stringAppender.openSub();
		stringAppender.append(" <- ");
		variable.stringAppend(stringAppender, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppender.closeSub();
		stringAppender.append(")");
	}

	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		super.freeVariables(freeVars, localVars);
		if (!localVars.contains(variable))
			freeVars.add(variable);

	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return super.isFreeVariable(variable) || this.variable.equals(variable);
	}

	public class DiffInfoFoldingCastType extends DiffInfoCastType
	{
		public final DiffInfo diffType;
		public final DiffInfo diffVariable;
		public final DiffInfo diffValue;

		protected DiffInfoFoldingCastType(FoldingCastTypeTerm other, DiffInfo diffTerm, DiffInfo diffType, DiffInfo diffVariable, DiffInfo diffValue)
		{
			super(other, diffTerm);
			this.diffType = diffType;
			this.diffVariable = diffVariable;
			this.diffValue = diffValue;
		}

		@Override
		public String toStringLeft(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return "(" + diffTerm.toStringLeft(variableToIdentifier, parameterNumerator) + ":" + diffType.toStringLeft(variableToIdentifier, parameterNumerator)
					+ " | " + diffVariable.toStringLeft(variableToIdentifier, parameterNumerator) + " <- "
					+ diffValue.toStringLeft(variableToIdentifier, parameterNumerator) + ")";
		}

		@Override
		public String toStringRight(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return "(" + diffTerm.toStringRight(variableToIdentifier, parameterNumerator) + ":"
					+ diffType.toStringRight(variableToIdentifier, parameterNumerator) + " | "
					+ diffVariable.toStringRight(variableToIdentifier, parameterNumerator) + " <- "
					+ diffValue.toStringRight(variableToIdentifier, parameterNumerator) + ")";
		}
	}

	@Override
	public DiffInfo diff(Term term)
	{
		DiffInfo di = super.diff(term);
		if (di != null)
			return di;
		if (!(term instanceof FoldingCastTypeTerm))
			return new DiffInfoNotEqual(term);
		FoldingCastTypeTerm cast = (FoldingCastTypeTerm) term;
		DiffInfo diffTerm = getTerm().diff(cast.getTerm());
		DiffInfo diffType = getType().diff(cast.getType());
		DiffInfo diffVariable = getVariable().diff(cast.getVariable());
		DiffInfo diffValue = getValue().diff(cast.getValue());
		if ((diffTerm instanceof DiffInfoEqual) && (diffType instanceof DiffInfoEqual) && (diffVariable instanceof DiffInfoEqual)
				&& (diffValue instanceof DiffInfoEqual))
			return new DiffInfoEqual(cast);
		return new DiffInfoFoldingCastType(cast, diffTerm, diffType, diffVariable, diffValue);
	}

	public class SearchInfoFoldingCastType extends SearchInfoCastType
	{
		public final SearchInfo searchType;
		public final SearchInfo searchVariable;
		public final SearchInfo searchValue;

		protected SearchInfoFoldingCastType(SearchInfo searchTerm, SearchInfo searchType, SearchInfo searchVariable, SearchInfo searchValue)
		{
			super(searchTerm);
			this.searchType = searchType;
			this.searchVariable = searchVariable;
			this.searchValue = searchValue;
		}

		@Override
		public String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier, ParameterNumerator parameterNumerator)
		{
			return "(" + searchTerm.toString(variableToIdentifier, parameterNumerator) + ":" + searchType.toString(variableToIdentifier, parameterNumerator)
					+ " | " + searchVariable.toString(variableToIdentifier, parameterNumerator) + " <- "
					+ searchValue.toString(variableToIdentifier, parameterNumerator) + ")";
		}
	}

	@Override
	public SearchInfo search(Term sub)
	{
		SearchInfo si = super.search(sub);
		if (si instanceof SearchInfoFound)
			return si;

		SearchInfo siTerm = getTerm().search(sub);
		SearchInfo siType = getType().search(sub);
		SearchInfo siVariable = getVariable().search(sub);
		SearchInfo siValue = getValue().search(sub);
		if ((siTerm instanceof SearchInfoNotFound) && (siType instanceof SearchInfoNotFound) && (siVariable instanceof SearchInfoNotFound)
				&& (siValue instanceof SearchInfoNotFound))
			return new SearchInfoNotFound();

		return new SearchInfoFoldingCastType(siTerm, siType, siVariable, siValue);
	}

}
