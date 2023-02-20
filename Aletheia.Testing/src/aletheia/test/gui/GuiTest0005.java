/*******************************************************************************
 * Copyright (c) 2023 Quim Testar.
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
package aletheia.test.gui;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import aletheia.test.Test;

public class GuiTest0005 extends Test
{

	public void saveIcon(Icon icon, File file) throws IOException
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(icon.getIconWidth(), icon.getIconHeight(), Transparency.TRANSLUCENT);
		Graphics2D g = image.createGraphics();
		icon.paintIcon(new JPanel(), g, 0, 0);
		g.dispose();
		ImageIO.write(image, "png", file);
	}

	@Override
	public void run() throws Exception
	{
		UIManager.setLookAndFeel(new MetalLookAndFeel());
		saveIcon(UIManager.getIcon("Tree.collapsedIcon"), new File("tmp/collapsed.png"));
		saveIcon(UIManager.getIcon("Tree.expandedIcon"), new File("tmp/expanded.png"));
	}

}
