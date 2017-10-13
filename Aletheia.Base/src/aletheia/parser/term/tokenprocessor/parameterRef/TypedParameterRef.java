/*******************************************************************************
 * Copyright (c) 2017 Quim Testar
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
package aletheia.parser.term.tokenprocessor.parameterRef;

import aletheia.model.term.ParameterVariableTerm;

public class TypedParameterRef
{
	private final ParameterRef parameterRef;
	private final ParameterVariableTerm parameter;

	public TypedParameterRef(ParameterRef parameterRef, ParameterVariableTerm parameter)
	{
		super();
		this.parameterRef = parameterRef;
		this.parameter = parameter;
	}

	public ParameterRef getParameterRef()
	{
		return parameterRef;
	}

	public ParameterVariableTerm getParameter()
	{
		return parameter;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
		result = prime * result + ((parameterRef == null) ? 0 : parameterRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedParameterRef other = (TypedParameterRef) obj;
		if (parameter == null)
		{
			if (other.parameter != null)
				return false;
		}
		else if (!parameter.equals(other.parameter))
			return false;
		if (parameterRef == null)
		{
			if (other.parameterRef != null)
				return false;
		}
		else if (!parameterRef.equals(other.parameterRef))
			return false;
		return true;
	}

}
