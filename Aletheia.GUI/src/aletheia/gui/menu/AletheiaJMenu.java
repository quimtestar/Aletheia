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
package aletheia.gui.menu;

import javax.swing.JMenu;

import aletheia.gui.app.DesktopAletheiaJFrame;

public abstract class AletheiaJMenu extends JMenu
{
	private static final long serialVersionUID = -1841244310731526360L;
	private final AletheiaJMenuBar aletheiaJMenuBar;

	public AletheiaJMenu(AletheiaJMenuBar aletheiaJMenuBar, String s, int mnemonic)
	{
		super(s);
		this.setMnemonic(mnemonic);
		this.aletheiaJMenuBar = aletheiaJMenuBar;
		this.setFont(getAletheiaJFrame().getMenuFont());
	}

	public AletheiaJMenuBar getAletheiaJMenuBar()
	{
		return aletheiaJMenuBar;
	}

	public DesktopAletheiaJFrame getAletheiaJFrame()
	{
		return getAletheiaJMenuBar().getAletheiaJFrame();
	}

}
