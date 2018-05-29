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
package aletheia.parser;

import aletheia.parsergenerator.LocationInterval;
import aletheia.parsergenerator.ParserBaseException;

//TODO not necessary anymore. Delete this
@Deprecated
public class AletheiaParserException extends Exception
{
	private static final long serialVersionUID = 313143433656661210L;

	private final LocationInterval locationInterval;

	public AletheiaParserException(ParserBaseException e)
	{
		super(e.getMessage(), e);
		this.locationInterval = e.getLocationInterval();
	}

	public LocationInterval getLocationInterval()
	{
		return locationInterval;
	}

	public String position()
	{
		return locationInterval.position();
	}

	@Override
	public String getMessage()
	{
		if (getCause() != null)
			return getCause().getMessage();
		else
			return super.getMessage();
	}

}
