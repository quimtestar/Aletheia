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
import aletheia.model.parameteridentification.ParameterIdentification;

public class ParameterWithDomain
{
	private final Identifier parameter;
	private final ParameterIdentification domain;

	public ParameterWithDomain(Identifier parameter, ParameterIdentification domain)
	{
		super();
		this.parameter = parameter;
		this.domain = domain;
	}

	public Identifier getParameter()
	{
		return parameter;
	}

	public ParameterIdentification getDomain()
	{
		return domain;
	}

}
