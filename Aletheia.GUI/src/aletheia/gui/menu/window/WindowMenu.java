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

import java.awt.event.KeyEvent;

import aletheia.gui.menu.AletheiaJMenu;
import aletheia.gui.menu.AletheiaJMenuBar;
import aletheia.gui.menu.AletheiaMenuItem;

public class WindowMenu extends AletheiaJMenu
{
	private static final long serialVersionUID = -1653459658630302457L;

	private final OpenExtraFrameAction openExtraFrameAction;
	private final CloseExtraFramesAction closeExtraFramesAction;

	public WindowMenu(AletheiaJMenuBar aletheiaJMenuBar)
	{
		super(aletheiaJMenuBar, "Window", KeyEvent.VK_W);
		this.openExtraFrameAction = new OpenExtraFrameAction(this);
		this.add(new AletheiaMenuItem(openExtraFrameAction));
		this.closeExtraFramesAction = new CloseExtraFramesAction(this);
		this.add(new AletheiaMenuItem(closeExtraFramesAction));
	}

	public OpenExtraFrameAction getOpenExtraFrameAction()
	{
		return openExtraFrameAction;
	}
}
