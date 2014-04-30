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
package aletheia.utilities.aborter;

/**
 * Abort processes with a polling mechanism.
 * 
 * The abortable process should receive an instance of an object implementing
 * this interface and periodically call the {@link #checkAbort()} method, which
 * will throw an {@link AbortException} if the process needs to be aborted.
 * 
 * @author Quim Testar
 */
public interface Aborter
{
	/**
	 * Thrown when the process is to be aborted.
	 */
	public class AbortException extends Exception
	{
		private static final long serialVersionUID = 8886504954466363575L;

		public AbortException()
		{
			super();
		}

		public AbortException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public AbortException(String message)
		{
			super(message);
		}

		public AbortException(Throwable cause)
		{
			super(cause);
		}

	}

	/**
	 * An aborter that never aborts.
	 */
	public static Aborter nullAborter = new Aborter()
	{

		@Override
		public void checkAbort()
		{
		}

	};

	/**
	 * Check if the process must be aborted.
	 * 
	 * @throws AbortException
	 *             The process must be aborted.
	 */
	public void checkAbort() throws AbortException;

}
