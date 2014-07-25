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
package aletheia.persistence.gui;

import java.awt.Component;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.preferences.PersistenceAletheiaPreferences;

public abstract class PersistenceGUIFactory
{
	public abstract String getName();

	public abstract PersistenceAletheiaPreferences getPreferences();

	public abstract PersistencePreferencesJPanel createPreferencesJPanel();

	public abstract class CreatePersistenceManagerException extends Exception
	{

		private static final long serialVersionUID = 9154491670899506027L;

		protected CreatePersistenceManagerException()
		{
			super();
		}

		protected CreatePersistenceManagerException(Throwable cause)
		{
			super(cause);
		}

	}

	public class EncapsulatedCreatePersistenceManagerException extends CreatePersistenceManagerException
	{
		private static final long serialVersionUID = -3127690007278040724L;

		public EncapsulatedCreatePersistenceManagerException(Exception exception)
		{
			super(exception);
		}

		@Override
		public synchronized Exception getCause()
		{
			return (Exception) super.getCause();
		}
	}

	public class RedialogCreatePersistenceManagerException extends CreatePersistenceManagerException
	{
		private static final long serialVersionUID = 3162811324818639267L;

	}

	public abstract PersistenceManager createPersistenceManager(Component parent, PersistenceManager.StartupProgressListener startupProgressListener)
			throws CreatePersistenceManagerException;

	public PersistenceManager createPersistenceManager(Component parent) throws CreatePersistenceManagerException
	{
		return createPersistenceManager(parent, PersistenceManager.StartupProgressListener.silent);
	}

}
