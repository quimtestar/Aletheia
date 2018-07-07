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
package aletheia.parser.parameteridentification;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.Term.ParameterIdentification;

public class ParameterWithType
{
	private final Identifier parameter;
	private final ParameterIdentification parameterType;

	public ParameterWithType(Identifier parameter, ParameterIdentification parameterType)
	{
		super();
		this.parameter = parameter;
		this.parameterType = parameterType;
	}

	public Identifier getParameter()
	{
		return parameter;
	}

	public ParameterIdentification getParameterType()
	{
		return parameterType;
	}

}