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

import javax.swing.JMenuBar;

import aletheia.gui.app.DesktopAletheiaJFrame;
import aletheia.gui.menu.configuration.ConfigurationMenu;
import aletheia.gui.menu.data.DataMenu;
import aletheia.gui.menu.help.HelpMenu;
import aletheia.gui.menu.security.SecurityMenu;
import aletheia.gui.menu.window.WindowMenu;

public class AletheiaJMenuBar extends JMenuBar
{
	private static final long serialVersionUID = 7019581855541691246L;

	private final DesktopAletheiaJFrame aletheiaJFrame;
	private final ConfigurationMenu configurationMenu;
	private final SecurityMenu securityMenu;
	private final DataMenu dataMenu;
	private final WindowMenu windowMenu;
	private final HelpMenu helpMenu;

	public AletheiaJMenuBar(DesktopAletheiaJFrame aletheiaJFrame)
	{
		super();
		this.aletheiaJFrame = aletheiaJFrame;
		this.configurationMenu = new ConfigurationMenu(this);
		this.add(configurationMenu);
		this.securityMenu = new SecurityMenu(this);
		this.add(securityMenu);
		this.dataMenu = new DataMenu(this);
		this.add(dataMenu);
		this.windowMenu = new WindowMenu(this);
		this.add(windowMenu);
		this.helpMenu = new HelpMenu(this);
		this.add(helpMenu);
	}

	public DesktopAletheiaJFrame getAletheiaJFrame()
	{
		return aletheiaJFrame;
	}

	public ConfigurationMenu getConfigurationMenu()
	{
		return configurationMenu;
	}

	public SecurityMenu getSecurityMenu()
	{
		return securityMenu;
	}

	public DataMenu getDataMenu()
	{
		return dataMenu;
	}

	public WindowMenu getWindowMenu()
	{
		return windowMenu;
	}

	@Override
	public HelpMenu getHelpMenu()
	{
		return helpMenu;
	}

	public void updatePersistenceManager()
	{
		securityMenu.updatePersistenceManager();
		dataMenu.updatePersistenceManager();
		windowMenu.updatePersistenceManager();
	}

}
