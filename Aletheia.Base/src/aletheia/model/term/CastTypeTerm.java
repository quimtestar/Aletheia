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

public abstract class CastTypeTerm extends AtomicTerm
{
	private static final long serialVersionUID = 8780254850341380288L;
	private final static int hashPrime = 2963777;

	private final Term term;

	public static class CastTypeException extends TypeException
	{

		private static final long serialVersionUID = -2663065237021489227L;

		protected CastTypeException()
		{
			super();
		}

		protected CastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected CastTypeException(String message)
		{
			super(message);
		}

		protected CastTypeException(Throwable cause)
		{
			super(cause);
		}
	}

	public CastTypeTerm(Term type, Term term)
	{
		super(type);
		this.term = term;
	}

	public Term getTerm()
	{
		return term;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		ret = ret * hashPrime + term.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof CastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		CastTypeTerm castTypeTerm = (CastTypeTerm) obj;
		if (!term.equals(castTypeTerm.term))
			return false;
		return true;
	}

	protected abstract Term castToType(Term term) throws CastTypeException;

	public static class NotCasteableException extends CastTypeException
	{
		private static final long serialVersionUID = 311089198157566761L;

		protected NotCasteableException()
		{
			super("Not casteable");
		}

	}

	public static Term castToType(Term term, Term targetType) throws CastTypeException
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
						Term castedPar = castToType(targetParameter, parType);
						Term composedPar = term.compose(castedPar);
						Term castedBody = castToType(composedPar, targetBody);
						Term casted = new FunctionTerm(targetParameter, castedBody);
						return casted;
					}
					catch (ComposeTypeException e)
					{
						throw new CastTypeException(e);
					}

				}
				else if (targetType instanceof ProjectionTerm)
					return castToType(term, ((ProjectionTerm) targetType).getFunction()).castToProjectedType();
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
						Term castedBody = castToType(composedPar, targetBody);
						Term casted = new FunctionTerm(targetParameter, castedBody);
						return casted;
					}
					catch (ComposeTypeException e)
					{
						throw new CastTypeException(e);
					}
				}
				else if (targetType instanceof ProjectionTerm)
					return castToType(term.castToUnprojectedType(), ((ProjectionTerm) targetType).getFunction()).castToProjectedType();
				else
					throw new NotCasteableException();
			}
			else
				throw new NotCasteableException();
		}

	}

	@Override
	public int size()
	{
		return term.size();
	}

	@Override
	protected void freeVariables(Set<VariableTerm> freeVars, Set<VariableTerm> localVars)
	{
		term.freeVariables(freeVars, localVars);
	}

	@Override
	public boolean isFreeVariable(VariableTerm variable)
	{
		return term.isFreeVariable(variable);
	}

	@Override
	protected Term replace(Deque<Replace> replaces, Set<VariableTerm> exclude) throws ReplaceTypeException
	{
		try
		{
			return castToType(term.replace(replaces, exclude));
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
			return castToType(term.replace(replaces));
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
		term.stringAppend(stringAppend, variableToIdentifier, parameterNumerator, parameterIdentification);
		stringAppend.append(symbolClose());
	}

	@Override
	public boolean castFree()
	{
		return false;
	}

}
