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
package aletheia.gui.icons;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconManager
{
	public final List<Image> aletheiaIconList;

	private static List<Image> loadIcons()
	{
		String[] files =
		{ "aletheia_plain_16.png", "aletheia_plain_32.png", "aletheia_plain_64.png", "aletheia_plain_128.png" };
		List<Image> icons = new ArrayList<Image>();
		for (String f : files)
		{
			InputStream is = null;
			try
			{
				is = ClassLoader.getSystemResourceAsStream("aletheia/gui/icons/" + f);
				if (is != null)
					icons.add(ImageIO.read(is));
			}
			catch (IOException e)
			{
			}
			finally
			{
				if (is != null)
					try
					{
						is.close();
					}
					catch (IOException e)
					{
					}
			}
		}
		return icons;
	}

	@SuppressWarnings("unused")
	private static Icon loadBinaryFolderIcon()
	{
		InputStream is = null;
		try
		{
			is = ClassLoader.getSystemResourceAsStream("aletheia/gui/icons/folder_binary.png");
			try
			{
				return new ImageIcon(ImageIO.read(is));
			}
			catch (IOException e)
			{
				return null;
			}
		}
		finally
		{
			if (is != null)
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					return null;
				}
		}
	}

	private IconManager()
	{
		super();
		aletheiaIconList = Collections.unmodifiableList(loadIcons());
	}

	public static final IconManager instance = new IconManager();

}
