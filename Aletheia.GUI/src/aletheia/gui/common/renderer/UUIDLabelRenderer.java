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

import java.util.UUID;

import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaTheme;

public class UUIDLabelRenderer extends AbstractRenderer
{

	private static final long serialVersionUID = 3107760025659849180L;

	public UUIDLabelRenderer(FontManager fontManager, UUID uuid, AletheiaTheme.Key colorKey)
	{
		super(fontManager);
		addUUIDLabel(uuid, colorKey);
	}

	public UUIDLabelRenderer(FontManager fontManager, UUID uuid)
	{
		super(fontManager);
		addUUIDLabel(uuid, AletheiaTheme.Key.default_);
	}

}
