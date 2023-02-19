package aletheia.gui.lookandfeel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.plaf.ColorUIResource;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;

/**
 * This class is just for debugging purposes.
 */
@Deprecated
public class DebuggingMaskAletheiaTheme extends AletheiaTheme
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final Map<String, ColorUIResource> colors;

	public DebuggingMaskAletheiaTheme()
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
			color = Optional.of(callingMethodName()).map(String::hashCode).map(DebuggingMaskAletheiaTheme::hashToColor).orElse(black);
			if (colors != null)
				colors.put(method, color);
			logger.info("{} -> {}", method, String.format("%06x", color.getRGB() & 0xffffff));
		}
		return color;
	}

	@Override
	protected ColorUIResource getPrimary1()
	{
		return color();
	}

	@Override
	protected ColorUIResource getPrimary2()
	{
		return color();
	}

	@Override
	protected ColorUIResource getPrimary3()
	{
		return color();
	}

	@Override
	protected ColorUIResource getSecondary1()
	{
		return color();
	}

	@Override
	protected ColorUIResource getSecondary2()
	{
		return color();
	}

	@Override
	protected ColorUIResource getSecondary3()
	{
		return color();
	}

	@Override
	protected ColorUIResource getBlack()
	{
		return color();
	}

	@Override
	public ColorUIResource getDesktopColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getInactiveControlTextColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getControlTextColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getMenuDisabledForeground()
	{
		return color();
	}

	@Override
	protected ColorUIResource getWhite()
	{
		return color();
	}

	@Override
	public ColorUIResource getFocusColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getControl()
	{
		return color();
	}

	@Override
	public ColorUIResource getControlShadow()
	{
		return color();
	}

	@Override
	public ColorUIResource getControlDarkShadow()
	{
		return color();
	}

	@Override
	public ColorUIResource getControlInfo()
	{
		return color();
	}

	@Override
	public ColorUIResource getControlHighlight()
	{
		return color();
	}

	@Override
	public ColorUIResource getControlDisabled()
	{
		return color();
	}

	@Override
	public ColorUIResource getPrimaryControl()
	{
		return color();
	}

	@Override
	public ColorUIResource getPrimaryControlShadow()
	{
		return color();
	}

	@Override
	public ColorUIResource getPrimaryControlDarkShadow()
	{
		return color();
	}

	@Override
	public ColorUIResource getPrimaryControlInfo()
	{
		return color();
	}

	@Override
	public ColorUIResource getPrimaryControlHighlight()
	{
		return color();
	}

	@Override
	public ColorUIResource getSystemTextColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getInactiveSystemTextColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getTextHighlightColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getHighlightedTextColor()
	{
		return color();
	}

	@Override
	public ColorUIResource getWindowTitleBackground()
	{
		return color();
	}

	@Override
	public ColorUIResource getWindowTitleForeground()
	{
		return color();
	}

	@Override
	public ColorUIResource getWindowTitleInactiveBackground()
	{
		return color();
	}

	@Override
	public ColorUIResource getWindowTitleInactiveForeground()
	{
		return color();
	}

	@Override
	public ColorUIResource getMenuBackground()
	{
		return color();
	}

	@Override
	public ColorUIResource getMenuForeground()
	{
		return color();
	}

	@Override
	public ColorUIResource getMenuSelectedBackground()
	{
		return color();
	}

	@Override
	public ColorUIResource getMenuSelectedForeground()
	{
		return color();
	}

	@Override
	public ColorUIResource getSeparatorBackground()
	{
		return color();
	}

	@Override
	public ColorUIResource getSeparatorForeground()
	{
		return color();
	}

	@Override
	public ColorUIResource getAcceleratorForeground()
	{
		return color();
	}

	@Override
	public ColorUIResource getAcceleratorSelectedForeground()
	{
		return color();
	}

}
