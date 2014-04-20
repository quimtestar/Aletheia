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

public interface NonBlockingSocketChannelStream
{

	public void setTimeout(long timeout);

	public long getTimeout();

	public void interrupt();

	public class StreamException extends IOException
	{
		private static final long serialVersionUID = 2426901446844555488L;

	}

	public class TimeoutException extends StreamException
	{
		private static final long serialVersionUID = -4024446616688877784L;

	}

	public class InterruptedException extends StreamException
	{
		private static final long serialVersionUID = 8342416457669067112L;
	}

	public class ClosedStreamException extends StreamException
	{
		private static final long serialVersionUID = 1085642608030134349L;
	}
}
