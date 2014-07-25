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
package aletheia.utilities.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

/**
 * The streams ({@link InputStream input} or {@link OutputStream output})
 * implementing this interface will send/receive the data through a non-blocking
 * {@link SocketChannel}.
 *
 * @author Quim Testar
 */
public interface NonBlockingSocketChannelStream
{

	/**
	 * Sets the timeout for read/write operations. If 0 operations will wait
	 * indefinitelly.
	 *
	 * @param timeout
	 *            in milliseconds. If 0 will wait indefinitelly.
	 */
	public void setTimeout(long timeout);

	/**
	 * Gets the configured timeout for read/write operations.
	 */
	public long getTimeout();

	/**
	 * Interrupts the read/write operation to programatically unblock it if it's
	 * on a wait state. The operation will throw a {@link InterruptedException}.
	 */
	public void interrupt();

	/**
	 * Read/write operations might throw this specific exceptions for kind of
	 * streams.
	 */
	public class StreamException extends IOException
	{
		private static final long serialVersionUID = 2426901446844555488L;

	}

	/**
	 * Operation timeout has expired.
	 */
	public class TimeoutException extends StreamException
	{
		private static final long serialVersionUID = -4024446616688877784L;

	}

	/**
	 * Operation has been interrupted.
	 *
	 * @see #interrupt()
	 */
	public class InterruptedException extends StreamException
	{
		private static final long serialVersionUID = 8342416457669067112L;
	}

	/**
	 * Stream has been closed.
	 */
	public class ClosedStreamException extends StreamException
	{
		private static final long serialVersionUID = 1085642608030134349L;
	}
}
