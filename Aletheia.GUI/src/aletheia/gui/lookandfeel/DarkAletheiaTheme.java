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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

import aletheia.gui.icons.IconManager;

class DarkAletheiaTheme extends AletheiaTheme
{
	static DarkAletheiaTheme instance = new DarkAletheiaTheme();

	private DarkAletheiaTheme()
	{
		put(Key.default_, new ColorUIResource(224, 224, 224));
		put(Key.normalBackground, new ColorUIResource(0x202020));
		put(Key.selectedBackground, black);
		put(Key.activeContext, lightPurple);
		put(Key.groupSorter, lightBlue);
		put(Key.provenLabel, lightGreen);
		put(Key.unprovenLabel, lightOrange);
		put(Key.notValidSignatureSymbol, lightGray);
		put(Key.validSignatureSymbol, lightGray);
		put(Key.signedDependenciesSymbol, lightGray);
		put(Key.signedProofSymbol, lightGray);
		put(Key.subscribeSymbol, lightCyan);
		put(Key.focusBorder, lightBlue);
		put(Key.tableBackground, new ColorUIResource(0x333333));
		put(Key.delegateTree, lightBlue);
		put(Key.privatePerson, lightGreen);
		put(Key.true_, green);
	}

	/**
	 * Tree branch lines and some other stuff (parts of the folder icon, ...)
	 */
	@Override
	protected ColorUIResource getPrimary3()
	{
		return new ColorUIResource(0x505a64);
	}

	@Override
	public ColorUIResource getControl()
	{
		return new ColorUIResource(0x333333);
	}

	@Override
	protected ColorUIResource getPrimary1()
	{
		return new ColorUIResource(0xb0caff);
	}

	@Override
	public ColorUIResource getControlHighlight()
	{
		return getBlack();
	}

	@Override
	protected ColorUIResource getSecondary1()
	{
		return new ColorUIResource(0x515c66);
	}

	@Override
	protected ColorUIResource getSecondary2()
	{
		return new ColorUIResource(0x14171a);
	}

	@Override
	protected ColorUIResource getPrimary2()
	{
		return new ColorUIResource(0x384f66);
	}

	@Override
	public ColorUIResource getControlInfo()
	{
		return getWhite();
	}

	@Override
	protected ColorUIResource getSecondary3()
	{
		return new ColorUIResource(0x333333);
	}

	@Override
	public void addCustomEntriesToTable(UIDefaults table)
	{
		super.addCustomEntriesToTable(table);
		Optional.ofNullable(IconManager.instance.darkTreeCollapsedIcon).ifPresent(icon -> {
			table.put("Tree.collapsedIcon", icon);
		});
		Optional.ofNullable(IconManager.instance.darkTreeExpandedIcon).ifPresent(icon -> {
			table.put("Tree.expandedIcon", icon);
		});
		Stream.of("Button.gradient", "CheckBox.gradient", "CheckBoxMenuItem.gradient", "InternalFrame.activeTitleGradient", "RadioButtonMenuItem.gradient",
				"ScrollBar.gradient", "ToggleButton.gradient")
				.forEach(k -> table.put(k, List.of(Float.valueOf(.3f), Float.valueOf(0f), new ColorUIResource(0x0c0c0d), getBlack(), getSecondary2())));
		table.put("MenuBar.gradient", List.of(Float.valueOf(1f), Float.valueOf(0f), getBlack(), new ColorUIResource(0x262626), new ColorUIResource(0x262626)));
		table.put("TabbedPane.selected", getPrimary2());
		table.put("TabbedPane.contentAreaColor", getPrimary2());
		table.put("TabbedPane.unselectedBackground", getNormalBackground());
	}

}
