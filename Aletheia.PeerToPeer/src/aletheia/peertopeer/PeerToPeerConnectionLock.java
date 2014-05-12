/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 * 
 * This file is part of the Aletheia Proof Assistant.
 * 
 * The Aletheia Proof Assistant is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * The Aletheia Proof Assistant is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer;

public class PeerToPeerConnectionLock
{
	public class LockTimeoutException extends Exception
	{
		private static final long serialVersionUID = 4436840171945607563L;
	}

	private PeerToPeerConnection lockedConnection;

	public PeerToPeerConnectionLock()
	{
		this.lockedConnection = null;
	}

	public synchronized void lock(PeerToPeerConnection connection) throws InterruptedException
	{
		while (lockedConnection != null && lockedConnection != connection)
			wait();
		lockedConnection = connection;
	}

	public synchronized void lock(PeerToPeerConnection connection, long timeout) throws InterruptedException, LockTimeoutException
	{
		long t0 = System.currentTimeMillis();
		while (lockedConnection != null && lockedConnection != connection)
		{
			long t = t0 + timeout - System.currentTimeMillis();
			if (t <= 0)
				throw new LockTimeoutException();
			wait(t);
		}
		lockedConnection = connection;
	}

	public synchronized void unlock(PeerToPeerConnection connection)
	{
		if (lockedConnection == connection)
			lockedConnection = null;
		notifyAll();
	}

	public synchronized PeerToPeerConnection getLockedConnection()
	{
		return lockedConnection;
	}

	public synchronized void waitForUnlock(PeerToPeerConnection connection) throws InterruptedException
	{
		while (lockedConnection != null && lockedConnection != connection)
			wait();
	}

	public synchronized void waitForUnlock(PeerToPeerConnection connection, long timeout) throws InterruptedException, LockTimeoutException
	{
		long t0 = System.currentTimeMillis();
		while (lockedConnection != null && lockedConnection != connection)
		{
			long t = t0 + timeout - System.currentTimeMillis();
			if (t <= 0)
				throw new LockTimeoutException();
			wait(t);
		}
	}

}
