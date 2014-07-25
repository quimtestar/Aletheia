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

import aletheia.persistence.PersistenceManager;
import aletheia.utilities.aborter.Aborter;
import aletheia.utilities.aborter.Aborter.AbortException;

public abstract class PeerToPeerFollower
{
	private final PersistenceManager persistenceManager;

	public interface Listener
	{

	}

	private final Listener listener;

	public PeerToPeerFollower(PersistenceManager persistenceManager, Listener listener)
	{
		this.persistenceManager = persistenceManager;
		this.listener = listener;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public Listener getListener()
	{
		return listener;
	}

	public abstract void follow(Aborter aborter) throws AbortException;

	public abstract void unfollow();

}
