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

import aletheia.model.term.ProjectionTerm.ProjectionTypeException;

public class ProjectedCastTypeTerm extends ProjectionCastTypeTerm
{
	private static final long serialVersionUID = -36509166031613827L;
	private final static int hashPrime = 2963923;

	public static class ProjectedCastTypeException extends CastTypeException
	{

		private static final long serialVersionUID = -5957505545584755810L;

		protected ProjectedCastTypeException()
		{
			super();
		}

		protected ProjectedCastTypeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected ProjectedCastTypeException(String message)
		{
			super(message);
		}

		protected ProjectedCastTypeException(Throwable cause)
		{
			super(cause);
		}

	}

	private static Term computeType(Term term) throws ProjectedCastTypeException
	{
		if (term instanceof UnprojectedCastTypeTerm)
			throw new ProjectedCastTypeException("Can't cast to projected a casted to unprojected term");
		Term type = term.getType();
		if (!(type instanceof FunctionTerm))
			throw new ProjectedCastTypeException("Type is not a function");
		try
		{
			return new ProjectionTerm((FunctionTerm) type);
		}
		catch (ProjectionTypeException e)
		{
			throw new ProjectedCastTypeException(e);
		}
	}

	public ProjectedCastTypeTerm(Term term) throws ProjectedCastTypeException
	{
		super(computeType(term), term);
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
		if (!(obj instanceof ProjectedCastTypeTerm))
			return false;
		if (!super.equals(obj))
			return false;
		return true;
	}

	@Override
	protected Term castToType(Term term) throws ProjectedCastTypeException
	{
		return term.castToProjectedType();
	}

	@Override
	protected String symbolOpen()
	{
		return "[";
	}

	@Override
	protected String symbolClose()
	{
		return "]";
	}

	@Override
	public Term castToUnprojectedType()
	{
		return getTerm();
	}

}
