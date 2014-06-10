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
package aletheia.gui.menu;

import javax.swing.AbstractAction;

import aletheia.gui.app.AletheiaJFrame;

public abstract class AletheiaMenuAction extends AbstractAction
{
	private static final long serialVersionUID = -8269480514632884323L;

	private final AletheiaJMenu aletheiaJMenu;

	public AletheiaMenuAction(AletheiaJMenu aletheiaJMenu, String name, int mnemonic)
	{
		this.aletheiaJMenu = aletheiaJMenu;
		this.putValue(NAME, name);
		this.putValue(MNEMONIC_KEY, mnemonic);
	}

	public AletheiaJMenu getAletheiaJMenu()
	{
		return aletheiaJMenu;
	}

	public AletheiaJMenuBar getAletheiaJMenuBar()
	{
		return getAletheiaJMenu().getAletheiaJMenuBar();
	}

	public AletheiaJFrame getAletheiaJFrame()
	{
		return getAletheiaJMenuBar().getAletheiaJFrame();
	}

}
