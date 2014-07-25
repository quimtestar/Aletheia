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

import aletheia.gui.menu.AletheiaMenuAction;

public class ClearPassphraseAction extends AletheiaMenuAction
{

	private static final long serialVersionUID = -4393437974843669180L;

	public ClearPassphraseAction(SecurityMenu securityMenu)
	{
		super(securityMenu, "Clear passphrase", KeyEvent.VK_C);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		getAletheiaJFrame().getPersistenceManager().getSecretKeyManager().clearPassphrase();
	}

}
