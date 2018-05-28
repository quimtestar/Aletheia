/*******************************************************************************
 * Copyright (c) 2018 Quim Testar.
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

import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.tokens.Location;

public class TokenProcessorException extends Exception
{
	private static final long serialVersionUID = 5888776553497775726L;

	private final Location startLocation;
	private final Location stopLocation;

	public TokenProcessorException(String message, Location startLocation, Location stopLocation)
	{
		super(message);
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;
	}

	public TokenProcessorException(ParserBaseException e)
	{
		super(e.getMessage(), e);
		this.startLocation = e.getStartLocation();
		this.stopLocation = e.getStopLocation();
	}

	public TokenProcessorException(Exception e, Location startLocation, Location stopLocation)
	{
		super(e.getMessage(), e);
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;
	}

	public Location getStartLocation()
	{
		return startLocation;
	}

	public Location getStopLocation()
	{
		return stopLocation;
	}

	public String position()
	{
		return startLocation.position();
	}

}
