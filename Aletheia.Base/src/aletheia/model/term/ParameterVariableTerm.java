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

import java.util.Map;

import aletheia.model.identifier.Identifier;

/**
 * A {@link VariableTerm} that can be used as a parameter of a
 * {@link FunctionTerm}.
 */
public class ParameterVariableTerm extends VariableTerm
{
	private static final long serialVersionUID = -305846440658111516L;
	private final static int hashPrime = 2959739;

	public ParameterVariableTerm(Term type)
	{
		super(type);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		return false;
	}

	@Override
	public int hashCode(int hasher)
	{
		int ret = 0;
		ret = ret * hashPrime + System.identityHashCode(this);
		return ret;
	}

	@Override
	public String hexRef()
	{
		return "?" + String.format("%08x", hashCode());
	}

	public String numRef(int num)
	{
		return "@" + num;
	}

	public String numRef(ParameterNumerator parameterNumerator)
	{
		return numRef(parameterNumerator.parameterNumber(this));
	}

	@Override
	protected void stringConvert(StringConverter stringConverter, Map<? extends VariableTerm, Identifier> variableToIdentifier,
			ParameterNumerator parameterNumerator, ParameterIdentification parameterIdentification)
	{
		if (parameterNumerator.isNumbered(this))
			stringConverter.append(numRef(parameterNumerator));
		else
			super.stringConvert(stringConverter, variableToIdentifier, parameterNumerator, parameterIdentification);
	}

}
