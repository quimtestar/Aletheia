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

import java.util.Map;

public class UnprojectedCastTypeTerm extends ProjectionCastTypeTerm
{
	private static final long serialVersionUID = 8127173784394913752L;
	private final static int hashPrime = 2963951;

	public static class UnprojectedCastTypeException extends CastTypeException
	{
		private static final long serialVersionUID = -5354349922979085668L;

		protected UnprojectedCastTypeException()
		{
			super();
		}

		protected UnprojectedCastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected UnprojectedCastTypeException(String message)
		{
			super(message);
		}

		protected UnprojectedCastTypeException(Throwable cause)
		{
			super(cause);
		}

	}

	private static Term computeType(Term term) throws UnprojectedCastTypeException
	{
		if (term instanceof ProjectedCastTypeTerm)
			throw new UnprojectedCastTypeException("Can't cast to unprojected a casted to projected term");
		Term type = term.getType();
		if (!(type instanceof ProjectionTerm))
			throw new UnprojectedCastTypeException("Type is not a projection");
		return ((ProjectionTerm) type).getFunction();
	}

	public UnprojectedCastTypeTerm(Term term) throws UnprojectedCastTypeException
	{
		super(computeType(term), term);
	}

	@Override
	protected int hashCode(int hasher)
	{
		int ret = super.hashCode(hasher *= hashPrime);
		return ret;
	}

	@Override
	protected boolean equals(Term term, Map<ParameterVariableTerm, ParameterVariableTerm> parameterMap)
	{
		if (!super.equals(term, parameterMap) || !(term instanceof UnprojectedCastTypeTerm))
			return false;
		return true;
	}

	@Override
	protected Term castToType(Term term) throws UnprojectedCastTypeException
	{
		return term.castToUnprojectedType();
	}

	@Override
	protected String symbolOpen()
	{
		return "{";
	}

	@Override
	protected String symbolClose()
	{
		return "}";
	}

	@Override
	public Term castToProjectedType()
	{
		return getTerm();
	}

}
