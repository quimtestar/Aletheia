/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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

public abstract class ProjectionCastTypeTerm extends CastTypeTerm
{
	private static final long serialVersionUID = 8780254850341380288L;
	private final static int hashPrime = 2963777;

	public ProjectionCastTypeTerm(Term type, Term term)
	{
		super(type, term);
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof ProjectionCastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	protected abstract Term castToType(Term term) throws CastTypeException;

	public static Term castToTargetType(Term term, Term targetType) throws CastTypeException
	{
		Term type = term.getType();
		if (type.equals(targetType))
			return term;
		else
		{
			if (type instanceof FunctionTerm)
			{
				if (targetType instanceof FunctionTerm)
				{
					ParameterVariableTerm targetParameter = ((FunctionTerm) targetType).getParameter();
					Term targetBody = ((FunctionTerm) targetType).getBody();
					try
					{
						Term parType = ((FunctionTerm) type).getParameter().getType();
						Term castedPar = castToTargetType(targetParameter, parType);
						Term composedPar = term.compose(castedPar);
						Term castedBody = castToTargetType(composedPar, targetBody);
						Term casted = new FunctionTerm(targetParameter, castedBody);
						return casted;
					}
					catch (ComposeTypeException e)
					{
						throw new CastTypeException(e);
					}

				}
				else if (targetType instanceof ProjectionTerm)
					return castToTargetType(term, ((ProjectionTerm) targetType).getFunction()).castToProjectedType();
				else
					throw new NotCasteableException();
			}
			else if (type instanceof ProjectionTerm)
			{
				if (targetType instanceof FunctionTerm)
				{
					ParameterVariableTerm targetParameter = ((FunctionTerm) targetType).getParameter();
					Term targetBody = ((FunctionTerm) targetType).getBody();
					try
					{
						Term unprojected = term.castToUnprojectedType();
						Term composedPar = unprojected.compose(targetParameter);
						Term castedBody = castToTargetType(composedPar, targetBody);
						Term casted = new FunctionTerm(targetParameter, castedBody);
						return casted;
					}
					catch (ComposeTypeException e)
					{
						throw new CastTypeException(e);
					}
				}
				else if (targetType instanceof ProjectionTerm)
					return castToTargetType(term.castToUnprojectedType(), ((ProjectionTerm) targetType).getFunction()).castToProjectedType();
				else
					throw new NotCasteableException();
			}
			else
				throw new NotCasteableException();
		}

	}

	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return castToType(getTerm().replace(replaces, exclude));
		}
		catch (CastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	@Override
	public Term replace(Map<VariableTerm, Term> replaces) throws ReplaceTypeException
	{
		try
		{
			return castToType(getTerm().replace(replaces));
		}
		catch (CastTypeException e)
		{
			throw new ReplaceTypeException(e);
		}
	}

	protected abstract String symbolOpen();

	protected abstract String symbolClose();

	@Override
	protected void stringAppend(StringAppender stringAppend, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		stringAppend.append(symbolOpen());
		getTerm().stringAppend(stringAppend, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppend.append(symbolClose());
	}

	@Override
	public boolean castFree()
	{
		return false;
	}

}
