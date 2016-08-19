/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
package aletheia.gui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

public class FocusBorderManager
{
	private class Listener implements FocusListener
	{

		@Override
		public void focusGained(FocusEvent e)
		{
			setBorder(true);
		}

		@Override
		public void focusLost(FocusEvent e)
		{
			setBorder(false);
		}
	}

	private final Listener listener = new Listener();

	private final JComponent borderable;
	private final Component[] focusables;
	private final Border oldBorder;
	private final Border notFocusedBorder;
	private final Border focusedBorder;

	public FocusBorderManager(JComponent borderable, Component... focusables)
	{
		this.borderable = borderable;
		this.focusables = focusables;
		this.oldBorder = borderable.getBorder();
		if (oldBorder != null)
		{
			this.notFocusedBorder = oldBorder;
			Insets insets = oldBorder.getBorderInsets(borderable);
			this.focusedBorder = BorderFactory.createMatteBorder(insets.top, insets.left, insets.bottom, insets.right, Color.blue);
		}
		else
		{
			this.notFocusedBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
			this.focusedBorder = BorderFactory.createLineBorder(Color.blue, 1);
		}
		boolean focused = false;
		for (Component f : focusables)
		{
			f.addFocusListener(listener);
			if (f.hasFocus())
				focused = true;
		}
		setBorder(focused);
	}

	private void setBorder(boolean focused)
	{
		borderable.setBorder(focused ? focusedBorder : notFocusedBorder);
	}

	public void close()
	{
		borderable.setBorder(oldBorder);
		for (Component f : focusables)
			f.removeFocusListener(listener);
	}

}
