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

public abstract class ParserBaseException extends Exception
{
	private static final long serialVersionUID = -1338891993034444711L;

	private final LocationInterval locationInterval;

	public ParserBaseException(LocationInterval locationInterval)
	{
		super();
		this.locationInterval = locationInterval;
	}

	public ParserBaseException(LocationInterval locationInterval, String message, Throwable cause)
	{
		super(message, cause);
		this.locationInterval = locationInterval;
	}

	public ParserBaseException(LocationInterval locationInterval, String message)
	{
		super(message);
		this.locationInterval = locationInterval;
	}

	public ParserBaseException(LocationInterval locationInterval, Throwable cause)
	{
		super(cause.getMessage(), cause);
		this.locationInterval = locationInterval;
	}

	public LocationInterval getLocationInterval()
	{
		return locationInterval;
	}

	public String position()
	{
		return locationInterval.position();
	}

	public String getGenericMessage()
	{
		return "Parsing error";
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
