/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceConfiguration;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager.EntityStoreVersionException;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager.MustAllowCreateException;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager.UnsupportedEntityStoreVersionException;
import aletheia.persistence.berkeleydb.preferences.BerkeleyDBPersistenceAletheiaPreferences;
import aletheia.persistence.gui.PersistenceGUIFactory;
import aletheia.preferences.AletheiaPreferences;
import aletheia.utilities.MiscUtilities;

public class BerkeleyDBPersistenceGUIFactory extends PersistenceGUIFactory
{
	private final BerkeleyDBPersistenceAletheiaPreferences preferences;

	public BerkeleyDBPersistenceGUIFactory(AletheiaPreferences preferencesParent)
	{
		this.preferences = new BerkeleyDBPersistenceAletheiaPreferences(preferencesParent);
	}

	@Override
	public String getName()
	{
		return "BerkeleyDB";
	}

	@Override
	public BerkeleyDBPersistenceAletheiaPreferences getPreferences()
	{
		return preferences;
	}

	@Override
	public BerkeleyDBPersistencePreferencesJPanel createPreferencesJPanel()
	{
		return new BerkeleyDBPersistencePreferencesJPanel(getPreferences());
	}

	private BerkeleyDBPersistenceConfiguration makePersistenceManagerConfiguration(PersistenceManager.StartupProgressListener progressListener,
			boolean allowCreate, boolean allowUpgrade)
	{
		BerkeleyDBPersistenceConfiguration configuration = new BerkeleyDBPersistenceConfiguration();
		configuration.setStartupProgressListener(progressListener);
		configuration.setDbFile(getPreferences().getDbFile());
		configuration.setAllowCreate(allowCreate);
		configuration.setReadOnly(getPreferences().isReadOnly());
		configuration.setAllowUpgrade(allowUpgrade);
		configuration.setCachePercent(getPreferences().getCachePercent());
		return configuration;
	}

	@Override
	public BerkeleyDBPersistenceManager createPersistenceManager(Component parent, PersistenceManager.StartupProgressListener progressListener)
			throws RedialogCreatePersistenceManagerException, EncapsulatedCreatePersistenceManagerException
	{
		if (getPreferences().getDbFile() == null)
			return null;
		try
		{
			try
			{
				return new BerkeleyDBPersistenceManager(makePersistenceManagerConfiguration(progressListener, false, false));
			}
			catch (UnsupportedEntityStoreVersionException e)
			{
				JOptionPane.showMessageDialog(parent, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
				throw new RedialogCreatePersistenceManagerException();
			}
			catch (EntityStoreVersionException e)
			{
				if (!getPreferences().isReadOnly())
				{
					JOptionPane.showMessageDialog(parent, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
					int r = JOptionPane.showConfirmDialog(parent, "Try to upgrade environment in " + getPreferences().getDbFile().getAbsolutePath()
							+ " from version " + e.getStoreVersion() + " to version " + e.getCodeStoreVersion() + "?", "", JOptionPane.YES_NO_OPTION);
					if (r != JOptionPane.YES_OPTION)
						throw new RedialogCreatePersistenceManagerException();
					return new BerkeleyDBPersistenceManager(makePersistenceManagerConfiguration(progressListener, false, true));
				}
				else
					throw new EncapsulatedCreatePersistenceManagerException(e);
			}
		}
		catch (MustAllowCreateException e)
		{
			int r = JOptionPane.showConfirmDialog(parent, "Environment not found in " + getPreferences().getDbFile().getAbsolutePath() + ". Create?", "",
					JOptionPane.YES_NO_OPTION);
			if (r != JOptionPane.YES_OPTION)
				throw new RedialogCreatePersistenceManagerException();
			return new BerkeleyDBPersistenceManager(makePersistenceManagerConfiguration(progressListener, true, false));
		}
	}

}
