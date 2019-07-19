/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.metal.MetalIconFactory;

import aletheia.persistence.berkeleydb.preferences.BerkeleyDBPersistenceAletheiaPreferences;
import aletheia.persistence.gui.PersistencePreferencesJPanel;

public class BerkeleyDBPersistencePreferencesJPanel extends PersistencePreferencesJPanel
{

	private static final long serialVersionUID = 7388820207543861236L;

	private final BerkeleyDBPersistenceAletheiaPreferences preferences;

	private final JTextField berkeleyDbPersistenceFolderTextField;
	private final JCheckBox berkeleyDbPersistenceReadOnlyCheckBox;
	private final JSpinner berkeleyDbPersistenceCachePercentSpinner;

	protected BerkeleyDBPersistencePreferencesJPanel(BerkeleyDBPersistenceAletheiaPreferences preferences)
	{
		super();
		this.preferences = preferences;
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel("Persistence Folder"), gbc);
		}
		this.berkeleyDbPersistenceFolderTextField = new JTextField();
		this.berkeleyDbPersistenceFolderTextField.setEditable(true);
		this.berkeleyDbPersistenceFolderTextField.setColumns(32);
		if (preferences.getDbFile() == null)
			this.berkeleyDbPersistenceFolderTextField.setText("");
		else
			this.berkeleyDbPersistenceFolderTextField.setText(preferences.getDbFile().getAbsolutePath());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(berkeleyDbPersistenceFolderTextField, gbc);
		}
		JButton choosePersistenceFolderButton = new JButton(new ChoosePersistenceFolderAction());
		choosePersistenceFolderButton.setMargin(new Insets(2, 2, 2, 2));
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(choosePersistenceFolderButton, gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel("Read only"), gbc);
		}
		this.berkeleyDbPersistenceReadOnlyCheckBox = new JCheckBox();
		this.berkeleyDbPersistenceReadOnlyCheckBox.setSelected(preferences.isReadOnly());
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(berkeleyDbPersistenceReadOnlyCheckBox, gbc);
		}
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(new JLabel("Cache percent"), gbc);
		}
		this.berkeleyDbPersistenceCachePercentSpinner = new JSpinner(
				new SpinnerNumberModel(Integer.max(1, Integer.min(preferences.getCachePercent(), 90)), 1, 90, 1));
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 2;
			gbc.insets = formFieldInsets;
			gbc.anchor = GridBagConstraints.WEST;
			add(berkeleyDbPersistenceCachePercentSpinner, gbc);
		}

	}

	private class ChoosePersistenceFolderAction extends AbstractAction
	{
		private static final long serialVersionUID = -6622348196898981545L;

		public ChoosePersistenceFolderAction()
		{
			super(null, new MetalIconFactory.FolderIcon16());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			File dbFile = new File(berkeleyDbPersistenceFolderTextField.getText().trim());
			JFileChooser fileChooser = new JFileChooser(dbFile);
			fileChooser.setDialogTitle("Choose the persistence folder");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = fileChooser.showOpenDialog(BerkeleyDBPersistencePreferencesJPanel.this);
			if (ret == JFileChooser.APPROVE_OPTION)
			{
				dbFile = fileChooser.getSelectedFile();
				berkeleyDbPersistenceFolderTextField.setText(dbFile.getAbsolutePath());
			}

		}
	}

	@Override
	public void okAction()
	{
		String dbFileName = berkeleyDbPersistenceFolderTextField.getText().trim();
		File dbFile = null;
		if (!dbFileName.isEmpty())
		{
			dbFile = new File(dbFileName);
			if (!dbFile.exists())
			{
				int r = JOptionPane.showConfirmDialog(this, "Folder " + dbFile.getAbsolutePath() + " doesn't exist. Create?", "", JOptionPane.YES_NO_OPTION);
				if (r != JOptionPane.YES_OPTION)
					return;
				dbFile.mkdirs();
			}
		}
		preferences.setDbFile(dbFile);
		preferences.setReadOnly(berkeleyDbPersistenceReadOnlyCheckBox.isSelected());
		preferences.setCachePercent((int) berkeleyDbPersistenceCachePercentSpinner.getValue());
	}

}
