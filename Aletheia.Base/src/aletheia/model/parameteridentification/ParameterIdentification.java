/*******************************************************************************
 * Copyright (c) 2018, 2023 Quim Testar
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

import java.io.StringReader;

import aletheia.model.identifier.Identifier;
import aletheia.parser.parameteridentification.ParameterIdentificationParser;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.protocol.Exportable;

public abstract class ParameterIdentification implements Exportable
{
	public static ParameterIdentification parse(String input) throws ParserBaseException
	{
		return ParameterIdentificationParser.parseParameterIdentification(new StringReader(input));
	}

	@Override
	public int hashCode()
	{
		return 1;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		return true;
	}

	protected abstract ParameterIdentification clearIdentifier(Identifier identifier);

}
