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
package aletheia.gui.cli.command.gui;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.statement.NonOperationalCommand;
import aletheia.log4j.LoggerManager;
import aletheia.parser.TermParserException;

public class TraceException extends NonOperationalCommand
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final String message;
	private final Exception exception;

	public TraceException(CommandSource from, String message, Exception exception)
	{
		super(from);
		this.message = message;
		this.exception = exception;
	}

	public TraceException(CommandSource from, Exception exception)
	{
		this(from, null, exception);
	}

	protected String getMessage()
	{
		return message;
	}

	protected Exception getException()
	{
		return exception;
	}

	@Override
	public void run()
	{
		if (message != null)
		{
			getErr().println(message);
			if (exception != null)
				exception.printStackTrace(getErr());
		}
		else if (exception != null)
		{
			if (exception.getMessage() != null)
			{
				getErr().println(exception.getMessage());
			}
			else
			{
				logger.error("Exception " + exception.getClass() + " has no defined message; dumping stack trace to the CLI", exception);
				exception.printStackTrace(getErr());
			}
			if (exception instanceof CommandParseTermParserException)
			{
				TermParserException tpe = ((CommandParseTermParserException) exception).getCause();
				getErr().print(tpe.getInput().substring(0, tpe.getStartLocation().column - 1));
				getErrB().print("\u00bb");
				getErrB().print(tpe.getInput().substring(tpe.getStartLocation().column - 1, tpe.getStopLocation().column - 1));
				getErrB().print("\u00ab");
				getErr().println(tpe.getInput().substring(tpe.getStopLocation().column - 1));
			}

		}
		getErr().flush();
	}

}
