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
package aletheia.gui.menu.security;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import aletheia.gui.common.PassphraseDialog;
import aletheia.gui.menu.AletheiaMenuAction;
import aletheia.utilities.MiscUtilities;

public class ChangePassphraseAction extends AletheiaMenuAction
{

	private static final long serialVersionUID = 4711802295776533144L;

	public ChangePassphraseAction(SecurityMenu securityMenu)
	{
		super(securityMenu, "Set/Change passphrase", KeyEvent.VK_S);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		PassphraseDialog dialog = new PassphraseDialog(getAletheiaJFrame(), true);
		final char[] passphrase = dialog.getPassphrase();
		if (passphrase != null)
		{
			int option = JOptionPane
					.showConfirmDialog(
							getAletheiaJFrame(),
							MiscUtilities
									.wrapText(
											"This action might take some minutes, since all the private keys must be (re)encrypted with the new passphrase.\nAre you sure you want to continue?",
											80));
			if (JOptionPane.OK_OPTION == option)
				try
				{
					getAletheiaJFrame().getPersistenceManager().getSecretKeyManager().changePassphrase(passphrase);
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(getAletheiaJFrame(), MiscUtilities.wrapText(ex.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
				}
		}
	}

}
