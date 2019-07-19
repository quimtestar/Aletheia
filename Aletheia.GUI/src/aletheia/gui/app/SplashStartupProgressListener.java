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
package aletheia.gui.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.SplashScreen;

import aletheia.persistence.PersistenceManager.StartupProgressListener;
import aletheia.version.VersionManager;

public class SplashStartupProgressListener implements StartupProgressListener
{
	private final SplashScreen splashScreen;
	private final Graphics2D graphics;
	private final Dimension size;

	private SplashStartupProgressListener(SplashScreen splashScreen) throws IllegalStateException
	{
		if (splashScreen != null)
		{
			this.splashScreen = splashScreen;
			this.graphics = splashScreen.createGraphics();
			this.size = splashScreen.getSize();
			graphics.setBackground(Color.white);
			graphics.setColor(new Color(0x000054));
			graphics.drawString("Version " + VersionManager.getVersion(), 12, 492);
			splashScreen.update();
		}
		else
		{
			this.splashScreen = null;
			this.graphics = null;
			this.size = null;
		}
	}

	private static SplashScreen getSplashScreen()
	{
		try
		{
			return SplashScreen.getSplashScreen();
		}
		catch (HeadlessException e)
		{
			return null;
		}
	}

	public SplashStartupProgressListener()
	{
		this(getSplashScreen());
	}

	@Override
	public void updateProgress(float progress)
	{
		if (splashScreen != null && splashScreen.isVisible())
		{
			int position = (int) (progress * (size.width - 4));
			graphics.fillRect(2, 466, position, 6);
			graphics.clearRect(position, 466, size.width - 4, 6);
			splashScreen.update();
		}
	}

	public void close()
	{
		if (splashScreen != null && splashScreen.isVisible())
			splashScreen.close();
	}

}
