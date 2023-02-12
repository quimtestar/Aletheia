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
