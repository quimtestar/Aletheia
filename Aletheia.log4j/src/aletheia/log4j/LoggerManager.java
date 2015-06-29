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
package aletheia.log4j;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * Statically configures the Log4J library with a properties file named
 * "aletheia.log4j.properties" on the working directory.
 *
 * @see <a href="http://logging.apache.org/log4j/1.2/manual.html">http://logging
 *      .apache.org/log4j/1.2/manual.html</a>
 *
 * @author Quim Testar
 */
public class LoggerManager
{
	private final static String confFileSysProp = "log4j.configurationFile";
	private final static String defaultConfFile = "aletheia.log4j2.xml";

	private final LoggerContext loggerContext;

	private LoggerManager()
	{
		Properties sysProps = System.getProperties();
		if (sysProps.get(confFileSysProp) == null)
			sysProps.put(confFileSysProp, defaultConfFile);
		loggerContext = new LoggerContext("aletheia");
		loggerContext.start();
	}

	public static LoggerManager instance = new LoggerManager();

	/**
	 * Creates a new {@link Logger} object named after the class from which this
	 * method is called. Meant to be called statically once for each class where
	 * logging is needed.
	 */
	public Logger logger()
	{
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		return loggerContext.getLogger(className);
	}

	private final static Logger logger = instance.logger();

	/**
	 * Configures an {@link UncaughtExceptionHandler} that logs a fatal error
	 * with the exception and then prints the stack trace to the standard error.
	 *
	 * Meant to be used once at the application level.
	 *
	 * @see Logger#fatal(Object, Throwable)
	 * @see Exception#printStackTrace(java.io.PrintStream)
	 */
	public void setUncaughtExceptionHandler()
	{
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{

			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				String message = "Exception in thread \"" + t.getName() + "\" ";
				logger.fatal(message, e);
				System.err.println(message);
				e.printStackTrace(System.err);
			}
		});

	}

}
