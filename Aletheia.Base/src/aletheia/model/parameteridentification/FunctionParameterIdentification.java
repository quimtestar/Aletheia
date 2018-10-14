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
package aletheia.model.parameteridentification;

import aletheia.model.identifier.Identifier;

public class FunctionParameterIdentification extends ParameterIdentification
{
	private final Identifier parameter;
	private final ParameterIdentification domain;
	private final ParameterIdentification body;

	public FunctionParameterIdentification(Identifier parameter, ParameterIdentification domain, ParameterIdentification body)
	{
		super();
		this.parameter = parameter;
		this.domain = domain;
		this.body = body;
	}

	public Identifier getParameter()
	{
		return parameter;
	}

	public ParameterIdentification getDomain()
	{
		return domain;
	}

	public ParameterIdentification getBody()
	{
		return body;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<");
		ParameterIdentification pi = this;
		boolean first = true;
		while (pi instanceof FunctionParameterIdentification)
		{
			if (!first)
				builder.append(", ");
			first = false;
			FunctionParameterIdentification fpi = (FunctionParameterIdentification) pi;
			if (fpi.getParameter() != null)
				builder.append(fpi.getParameter().toString());
			if (fpi.getDomain() != null)
				builder.append(":" + fpi.getDomain().toString());
			pi = fpi.getBody();
		}
		if (pi != null)
			builder.append(" -> " + pi.toString());
		builder.append(">");
		return builder.toString();
	}

}
