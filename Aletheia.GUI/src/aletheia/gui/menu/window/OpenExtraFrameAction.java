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
package aletheia.gui.menu.window;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.apache.logging.log4j.Logger;

import aletheia.gui.menu.AletheiaMenuAction;
import aletheia.log4j.LoggerManager;

public class OpenExtraFrameAction extends AletheiaMenuAction
{
	private static final Logger logger = LoggerManager.instance.logger();

	private static final long serialVersionUID = 2818746913208642479L;

	public OpenExtraFrameAction(WindowMenu windowMenu)
	{
		super(windowMenu, "Open extra frame", KeyEvent.VK_O);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			getAletheiaJFrame().openExtraFrame();
		}
		catch (InterruptedException e1)
		{
			logger.error("Exception caught", e);
		}
	}

}
