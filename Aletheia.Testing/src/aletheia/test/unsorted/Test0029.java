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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import aletheia.gui.app.FontManager;
import aletheia.test.Test;

public class Test0029 extends Test
{

	@Override
	public void run() throws Exception
	{
		FontManager fontManager = new FontManager(20);
		JFrame frame = new JFrame("Test0029");
		StyledDocument document = new DefaultStyledDocument();
		JTextPane textPane = new JTextPane(document);
		//textPane.setText("\u2190 \u00bbpoctlla\u00ab \u2192");
		textPane.setFont(fontManager.defaultFont());
		textPane.setPreferredSize(new Dimension(800, 600));
		frame.setContentPane(textPane);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		AttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground((MutableAttributeSet) attributeSet, Color.red);
		StyleConstants.setBold((MutableAttributeSet) attributeSet, true);
		StyleConstants.setUnderline((MutableAttributeSet) attributeSet, true);

		document.insertString(0, "\u2190 \u00bbpoctlla\u00ab \u2192", attributeSet);
	}

}
