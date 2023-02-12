/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.gui.common.renderer;

import java.awt.Font;

import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaTheme;

public class TextLabelRenderer extends AbstractRenderer
{
	private static final long serialVersionUID = -7907428778408871348L;

	public TextLabelRenderer(FontManager fontManager, String text, AletheiaTheme.Key textColorKey)
	{
		super(fontManager);
		setActiveFont(getTextLabelFont());
		addTextLabel(text, textColorKey != null ? textColorKey : AletheiaTheme.Key.default_);
	}

	public TextLabelRenderer(FontManager fontManager, String text)
	{
		this(fontManager, text, null);
	}

	protected Font getTextLabelFont()
	{
		return getDefaultFont();
	}

}
