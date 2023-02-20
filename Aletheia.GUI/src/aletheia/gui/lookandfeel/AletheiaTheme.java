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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.OceanTheme;

public abstract class AletheiaTheme extends OceanTheme
{
	public static enum Key
	{
		//@formatter:off
		default_,
		normalBackground,
		selectedBackground,
		activeContext,
		groupSorter,
		provenLabel,
		unprovenLabel,
		turnstile,
		tick,
		xMark,
		questionMark,
		notValidSignatureSymbol,
		validSignatureSymbol,
		signedDependenciesSymbol,
		signedProofSymbol,
		subscribeSymbol,
		true_,
		false_,
		privatePerson,
		delegateTree,
		packedSignetureRequest,
		unpackedSignetureRequest,
		focusBorder,
		tableBackground,
		bracketHighlightOk,
		bracketHighlightWarning,
		bracketHighlightError,
		commandLineError,
		//@formatter:on
	}

	protected static final ColorUIResource black = new ColorUIResource(Color.black);
	protected static final ColorUIResource darkPurple = new ColorUIResource(0x800080);
	protected static final ColorUIResource lightPurple = new ColorUIResource(0xff60ff);
	protected static final ColorUIResource darkGreen = new ColorUIResource(Color.green.darker().darker());
	protected static final ColorUIResource lightGreen = new ColorUIResource(128, 255, 128);
	protected static final ColorUIResource darkOrange = new ColorUIResource(Color.orange.darker().darker());
	protected static final ColorUIResource orange = new ColorUIResource(Color.orange);
	protected static final ColorUIResource green = new ColorUIResource(Color.green);
	protected static final ColorUIResource red = new ColorUIResource(Color.red);
	protected static final ColorUIResource darkGray = new ColorUIResource(0x606060);
	protected static final ColorUIResource darkCyan = new ColorUIResource(0x008080);
	protected static final ColorUIResource blue = new ColorUIResource(Color.blue);
	protected static final ColorUIResource darkRed = new ColorUIResource(Color.red.darker().darker());
	protected static final ColorUIResource white = new ColorUIResource(Color.white);
	protected static final ColorUIResource lightOrange = new ColorUIResource(Color.orange.brighter().brighter());
	protected static final ColorUIResource lightGray = new ColorUIResource(0xc0c0c0);
	protected static final ColorUIResource lightCyan = new ColorUIResource(0x80c0c0);
	protected static final ColorUIResource darkBlue = new ColorUIResource(Color.blue.darker());
	protected static final ColorUIResource lightBlue = new ColorUIResource(0x8080ff);
	protected static final ColorUIResource lightRed = new ColorUIResource(0xff4040);
	protected static final ColorUIResource cyan = new ColorUIResource(Color.cyan);

	private final Map<Key, ColorUIResource> colorMap;

	protected AletheiaTheme()
	{
		this.colorMap = new EnumMap<>(Key.class);
		put(Key.default_, super.getUserTextColor());
		put(Key.normalBackground, super.getWindowBackground());
		put(Key.selectedBackground, new ColorUIResource(Color.lightGray));
		put(Key.activeContext, darkPurple);
		put(Key.groupSorter, blue);
		put(Key.provenLabel, darkGreen);
		put(Key.unprovenLabel, darkOrange);
		put(Key.turnstile, orange);
		put(Key.tick, green);
		put(Key.xMark, red);
		put(Key.questionMark, red);
		put(Key.notValidSignatureSymbol, darkGray);
		put(Key.validSignatureSymbol, darkGray);
		put(Key.signedDependenciesSymbol, darkGray);
		put(Key.signedProofSymbol, darkGray);
		put(Key.subscribeSymbol, darkCyan);
		put(Key.true_, green);
		put(Key.false_, red);
		put(Key.privatePerson, darkGreen);
		put(Key.delegateTree, blue);
		put(Key.packedSignetureRequest, darkRed);
		put(Key.unpackedSignetureRequest, blue);
		put(Key.focusBorder, blue);
		put(Key.tableBackground, new ColorUIResource(0xeeeeee));
		put(Key.bracketHighlightOk, blue);
		put(Key.bracketHighlightWarning, darkCyan);
		put(Key.bracketHighlightError, red);
		put(Key.commandLineError, red);
		assertAllKeysDefined();
	}

	protected void assertAllKeysDefined()
	{
		assert colorMap.keySet().containsAll(Arrays.asList(Key.values())) : String.format("Look and feel theme class %s: unassigned keys: %s",
				getClass().getName(),
				Arrays.stream(Key.values()).filter(Predicate.not(colorMap::containsKey)).map(Key::toString).collect(Collectors.joining(", ")));
	}

	protected ColorUIResource put(Key key, ColorUIResource color)
	{
		return colorMap.put(key, color);
	}

	public ColorUIResource get(Key key)
	{
		return colorMap.get(key);
	}

	public ColorUIResource getDefault()
	{
		return get(Key.default_);
	}

	public ColorUIResource getNormalBackground()
	{
		return get(Key.normalBackground);
	}

	public ColorUIResource getSelectedBackground()
	{
		return get(Key.selectedBackground);
	}

	public ColorUIResource getActiveContext()
	{
		return get(Key.activeContext);
	}

	public ColorUIResource getGroupSorter()
	{
		return get(Key.groupSorter);
	}

	public ColorUIResource getProvenLabel()
	{
		return get(Key.provenLabel);
	}

	public ColorUIResource getUnprovenLabel()
	{
		return get(Key.unprovenLabel);
	};

	public ColorUIResource getPrivatePerson()
	{
		return get(Key.privatePerson);
	}

	public ColorUIResource getTurnstile()
	{
		return get(Key.turnstile);
	}

	public ColorUIResource getTableBackground()
	{
		return get(Key.tableBackground);
	}

	public ColorUIResource getFocusBorder()
	{
		return get(Key.focusBorder);
	}

	public ColorUIResource getBracketHighlightOk()
	{
		return get(Key.bracketHighlightOk);
	}

	public ColorUIResource getBracketHighlightWarning()
	{
		return get(Key.bracketHighlightWarning);
	}

	public ColorUIResource getBracketHighlightError()
	{
		return get(Key.bracketHighlightError);
	}

	public ColorUIResource getCommandLineError()
	{
		return get(Key.commandLineError);
	}

	@Override
	public ColorUIResource getUserTextColor()
	{
		return getDefault();
	}

	@Override
	public ColorUIResource getControlTextColor()
	{
		return getDefault();
	}

	@Override
	public ColorUIResource getSystemTextColor()
	{
		return getDefault();
	}

	@Override
	public ColorUIResource getWindowBackground()
	{
		return getNormalBackground();
	}

	@Override
	public ColorUIResource getMenuForeground()
	{
		return getDefault();
	}

	@Override
	public ColorUIResource getMenuBackground()
	{
		return getNormalBackground();
	}

	@Override
	public ColorUIResource getMenuSelectedForeground()
	{
		return getDefault();
	}

}
