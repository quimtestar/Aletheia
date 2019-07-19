/*******************************************************************************
 * Copyright (c) 2019 Quim Testar.
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
 *******************************************************************************/
package aletheia.test.unsorted;

import java.lang.reflect.Field;
import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.app.DesktopAletheiaGUI;
import aletheia.gui.app.DesktopAletheiaJFrame;
import aletheia.test.Test;

public class Test0030 extends Test
{

	@Override
	public void run() throws Exception
	{
		DesktopAletheiaGUI aletheiaGUI = new DesktopAletheiaGUI();
		DesktopAletheiaJFrame aletheiaJFrame = new DesktopAletheiaJFrame(aletheiaGUI);
		aletheiaJFrame.pack();
		aletheiaJFrame.setVisible(true);

		Field aletheiaContentPaneField = aletheiaJFrame.getClass().getDeclaredField("aletheiaContentPane");
		aletheiaContentPaneField.setAccessible(true);
		AletheiaJPanel aletheiaJPanel = (AletheiaJPanel) aletheiaContentPaneField.get(aletheiaJFrame);

		String s = "\u2190 \u00bbpoctlla\u00ab \u2192";

		aletheiaJPanel.getCliJPanel().getErrB().println(s);

	}

}
