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

import java.util.Set;

public abstract class CastTypeTerm extends AtomicTerm
{
	private static final long serialVersionUID = -1639688625397660727L;
	private final static int hashPrime = 2964383;

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

	public static class NotCasteableException extends CastTypeException
	{
		private static final long serialVersionUID = 311089198157566761L;

		protected NotCasteableException()
		{
			super("Not casteable");
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
	public boolean castFree()
	{
		return false;
	}

}
