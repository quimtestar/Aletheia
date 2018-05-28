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
package aletheia.parsergenerator;

import aletheia.parsergenerator.tokens.Location;

public class ParserBaseException extends Exception
{
	private static final long serialVersionUID = -1338891993034444711L;

	private final Location startLocation;
	private final Location stopLocation;

	public ParserBaseException(Location startLocation, Location stopLocation)
	{
		super();
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;
	}

	public ParserBaseException(Location startLocation, Location stopLocation, String message, Throwable cause)
	{
		super(message, cause);
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;
	}

	public ParserBaseException(Location startLocation, Location stopLocation, String message)
	{
		super(message);
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;
	}

	public ParserBaseException(Location startLocation, Location stopLocation, Throwable cause)
	{
		super(cause);
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

	public String getGenericMessage()
	{
		return "Parser/Lexer error";
	}

	@Override
	public String getMessage()
	{
		if (super.getMessage() == null)
			return getGenericMessage();
		else
			return super.getMessage();
	}

}
