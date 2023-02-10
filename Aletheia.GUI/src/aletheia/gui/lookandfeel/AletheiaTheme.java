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

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.OceanTheme;

public abstract class AletheiaTheme extends OceanTheme
{
	protected static final ColorUIResource darkPurple = new ColorUIResource(0x800080);
	protected static final ColorUIResource lightPurple = new ColorUIResource(0xff40ff);
	protected static final ColorUIResource darkGreen = new ColorUIResource(Color.green.darker().darker());
	protected static final ColorUIResource lightGreen = new ColorUIResource(128, 255, 128);

	public abstract ColorUIResource getSelectedBackground();

	public abstract ColorUIResource getActiveContext();

	public abstract ColorUIResource getGroupSorter();

	public abstract ColorUIResource getProvenLabel();

}
