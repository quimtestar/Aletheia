/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconManager
{
	private Image readImage(String fn)
	{
		try (InputStream is = IconManager.class.getResourceAsStream(fn))
		{
			if (is == null)
				return null;
			else
				return ImageIO.read(is);
		}
		catch (IOException e)
		{
			return null;
		}

	}

	private ImageIcon readIcon(String fn)
	{
		return Optional.ofNullable(readImage(fn)).map(ImageIcon::new).orElse(null);
	}

	private List<Image> loadAletheiaIcons()
	{
		return Stream.of("aletheia_plain_16.png", "aletheia_plain_32.png", "aletheia_plain_64.png", "aletheia_plain_128.png").map(this::readImage)
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	public final List<Image> aletheiaIconList = loadAletheiaIcons();
	public final ImageIcon darkTreeCollapsedIcon = readIcon("darkTreeCollapsedIcon.png");
	public final ImageIcon darkTreeExpandedIcon = readIcon("darkTreeExpandedIcon.png");

	public static final IconManager instance = new IconManager();

}
