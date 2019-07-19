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
package aletheia.gui.person;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import aletheia.gui.app.DesktopAletheiaJFrame;
import aletheia.gui.app.FontManager;
import aletheia.persistence.PersistenceManager;

public class PersonsDialog extends JDialog
{
	private static final long serialVersionUID = -9088016958228981761L;
	private final DesktopAletheiaJFrame aletheiaJFrame;
	private final PersonJTable personJTable;
	private final PrivatePersonJTable privatePersonJTable;
	private final JTabbedPane tabbedPane;

	public PersonsDialog(DesktopAletheiaJFrame aletheiaJFrame)
	{
		super(aletheiaJFrame, "Persons", false);
		this.aletheiaJFrame = aletheiaJFrame;
		this.personJTable = new PersonJTable(this);
		this.privatePersonJTable = new PrivatePersonJTable(this);
		this.tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Private", new JScrollPane(privatePersonJTable));
		tabbedPane.addTab("All", new JScrollPane(personJTable));
		tabbedPane.addKeyListener(new KeyListener()
		{

			private void forwardEvent(KeyEvent e)
			{
				int i = tabbedPane.getSelectedIndex();
				switch (i)
				{
				case 0:
					personJTable.dispatchEvent(e);
					break;
				case 1:
					privatePersonJTable.dispatchEvent(e);
					break;
				}
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				forwardEvent(e);
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				forwardEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				forwardEvent(e);
			}
		});
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.addWindowListener(new WindowListener()
		{

			@Override
			public void windowOpened(WindowEvent e)
			{
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				personJTable.cancelEditing();
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
			}

			@Override
			public void windowIconified(WindowEvent e)
			{
			}

			@Override
			public void windowDeiconified(WindowEvent e)
			{
			}

			@Override
			public void windowActivated(WindowEvent e)
			{
			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{
			}

		});
		this.setContentPane(tabbedPane);
		this.setResizable(true);
		this.setPreferredSize(new Dimension(800, 600));
		this.setLocationRelativeTo(aletheiaJFrame);
		this.pack();
	}

	protected DesktopAletheiaJFrame getAletheiaJFrame()
	{
		return aletheiaJFrame;
	}

	public FontManager getFontManager()
	{
		return getAletheiaJFrame().getFontManager();
	}

	public PersistenceManager getPersistenceManager()
	{
		return getAletheiaJFrame().getPersistenceManager();
	}

	public void close()
	{
		dispose();
		personJTable.close();
		privatePersonJTable.close();
	}

	public void updateFontSize()
	{
		personJTable.updateFontSize();
		privatePersonJTable.updateFontSize();
	}

}
