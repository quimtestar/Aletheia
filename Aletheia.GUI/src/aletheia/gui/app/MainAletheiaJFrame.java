/*******************************************************************************
 * Copyright (c) 2018, 2019 Quim Testar
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
package aletheia.gui.app;

import java.util.Map;

import javax.swing.JOptionPane;

import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.CommandLineArguments.Switch;
import aletheia.utilities.MiscUtilities;

public abstract class MainAletheiaJFrame extends AletheiaJFrame
{
	private static final long serialVersionUID = 3568497758961499431L;

	private class MyThrowableProcessor implements AletheiaEventQueue.ThrowableProcessor<DesktopAletheiaJFrame, PersistenceLockTimeoutException>
	{

		@Override
		public boolean processThrowable(DesktopAletheiaJFrame aletheiaJFrame, PersistenceLockTimeoutException e)
		{
			aletheiaJFrame.lock(e.getOwners());
			return true;
		}

	}

	private final FontManager fontManager;
	private final AletheiaGUI aletheiaGUI;

	public MainAletheiaJFrame(FontManager fontManager, AletheiaGUI aletheiaGUI)
	{
		super();
		this.fontManager = fontManager;
		this.aletheiaGUI = aletheiaGUI;
		this.aletheiaGUI.getAletheiaEventQueue().addThrowableProcessor(this, PersistenceLockTimeoutException.class, new MyThrowableProcessor());
	}

	public FontManager getFontManager()
	{
		return fontManager;
	}

	public AletheiaGUI getAletheiaGUI()
	{
		return aletheiaGUI;
	}

	public Map<String, Switch> getGlobalSwitches()
	{
		return aletheiaGUI.getGlobalSwitches();
	}

	public AletheiaEventQueue getAletheiaEventQueue()
	{
		return aletheiaGUI.getAletheiaEventQueue();
	}

	public void fatalError(Throwable e)
	{
		JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
		exit();
	}

	public abstract PeerToPeerNode getPeerToPeerNode();

	public abstract void restart();

	public abstract AletheiaJFrame openExtraFrame(String extraTitle);

}
