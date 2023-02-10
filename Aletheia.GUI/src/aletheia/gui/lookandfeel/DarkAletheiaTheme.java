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
package aletheia.gui.lookandfeel;

import javax.swing.plaf.ColorUIResource;

class DarkAletheiaTheme extends AletheiaTheme
{
	private static final ColorUIResource background = new ColorUIResource(64, 64, 64);
	private static final ColorUIResource text = new ColorUIResource(224, 224, 224);
	private static final ColorUIResource selectedBackground = new ColorUIResource(0, 0, 0);
	private static final ColorUIResource activeContext = lightPurple;
	private static final ColorUIResource groupSorter = new ColorUIResource(128, 128, 255);
	private static final ColorUIResource provenLabel = lightGreen;

	@Override
	public ColorUIResource getWindowBackground()
	{
		return background;
	}

	@Override
	public ColorUIResource getUserTextColor()
	{
		return text;
	}

	@Override
	public ColorUIResource getSelectedBackground()
	{
		return selectedBackground;
	}

	@Override
	public ColorUIResource getActiveContext()
	{
		return activeContext;
	}

	@Override
	public ColorUIResource getGroupSorter()
	{
		return groupSorter;
	}

	@Override
	public ColorUIResource getProvenLabel()
	{
		return provenLabel;
	}
}
