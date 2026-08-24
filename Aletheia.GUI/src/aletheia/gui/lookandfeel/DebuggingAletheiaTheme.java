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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;

/**
 * This class is just for debugging purposes.
 */
@Deprecated
public class DebuggingAletheiaTheme extends AletheiaTheme
{
	private static final Logger logger = LoggerManager.instance.logger();
	@Deprecated
	static DebuggingAletheiaTheme instance = new DebuggingAletheiaTheme();

	private final Map<String, ColorUIResource> colors;

	@Deprecated
	public DebuggingAletheiaTheme()
	{
		super();
		this.colors = new HashMap<>();
	}

	private String callingMethodName()
	{
		return StackWalker.getInstance()
				.walk(s -> s.map(StackWalker.StackFrame::getMethodName).filter(m -> !List.of("callingMethodName", "color").contains(m)).findFirst())
				.orElse(null);
	}

	private static ColorUIResource hashToColor(int h)
	{
		h = Integer.remainderUnsigned(h, 1 << (3 * 3)) + 1;
		return new ColorUIResource((h & 0b111000000) >> 1, (h & 0b111000) << 2, (h & 0b111) << 5);
	}

	private synchronized ColorUIResource color()
	{
		String method = callingMethodName();
		ColorUIResource color = Optional.ofNullable(colors).map(m -> m.get(method)).orElse(null);
		if (color == null)
		{
			color = Optional.of(callingMethodName()).map(String::hashCode).map(DebuggingAletheiaTheme::hashToColor).orElse(black);
			if (colors != null)
				colors.put(method, color);
			logger.info("{} -> {}", method, String.format("%06x", color.getRGB() & 0xffffff));
		}
		return color;
	}

	@Deprecated
	@Override
	public void addCustomEntriesToTable(UIDefaults table)
	{
		super.addCustomEntriesToTable(table);
		table.keySet().stream().filter(String.class::isInstance).sorted().forEach(k -> {
			try
			{
				Color c = (Color) table.get(k);
				logger.info("{}: {}", k, String.format("%06x", c.getRGB() & 0xffffff));
			}
			catch (Exception e)
			{
			}
		});
	}

	@Deprecated
	@Override
	protected ColorUIResource getPrimary1()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getPrimary2()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getPrimary3()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getSecondary1()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getSecondary2()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getSecondary3()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getBlack()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getDesktopColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getInactiveControlTextColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControlTextColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getMenuDisabledForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	protected ColorUIResource getWhite()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getFocusColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControl()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControlShadow()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControlDarkShadow()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControlInfo()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControlHighlight()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getControlDisabled()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getPrimaryControl()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getPrimaryControlShadow()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getPrimaryControlDarkShadow()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getPrimaryControlInfo()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getPrimaryControlHighlight()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getSystemTextColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getInactiveSystemTextColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getTextHighlightColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getHighlightedTextColor()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getWindowTitleBackground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getWindowTitleForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getWindowTitleInactiveBackground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getWindowTitleInactiveForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getMenuBackground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getMenuForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getMenuSelectedBackground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getMenuSelectedForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getSeparatorBackground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getSeparatorForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getAcceleratorForeground()
	{
		return color();
	}

	@Deprecated
	@Override
	public ColorUIResource getAcceleratorSelectedForeground()
	{
		return color();
	}

}
