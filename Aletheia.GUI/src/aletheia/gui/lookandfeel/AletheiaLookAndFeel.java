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
package aletheia.gui.lookandfeel;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import aletheia.gui.preferences.GUIAletheiaPreferences;

public class AletheiaLookAndFeel extends MetalLookAndFeel
{
	private static final long serialVersionUID = 1398088013985281002L;

	public static enum Theme
	{

		Light(new LightAletheiaTheme(), "\u2600 Light"), Dark(new DarkAletheiaTheme(), "\u263e Dark"),;

		private final AletheiaTheme aletheiaTheme;
		private final String label;

		Theme(AletheiaTheme aletheiaTheme, String label)
		{
			this.aletheiaTheme = aletheiaTheme;
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	};

	public static boolean changeTheme(Theme theme, Collection<? extends Component> components)
	{
		if (theme.aletheiaTheme == getCurrentTheme())
			return false;
		else
		{
			try
			{
				UIManager.setLookAndFeel(new AletheiaLookAndFeel(theme));
			}
			catch (UnsupportedLookAndFeelException e)
			{
				throw new Error(e);
			}
			SwingUtilities.invokeLater(() -> {
				for (Component c : components)
					SwingUtilities.updateComponentTreeUI(c);
			});
			return true;
		}
	}

	public static boolean changeTheme(Theme theme, Component... components)
	{
		return changeTheme(theme, Arrays.asList(components));
	}

	public static AletheiaTheme theme()
	{
		try
		{
			return (AletheiaTheme) MetalLookAndFeel.getCurrentTheme();
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}

	private AletheiaLookAndFeel(Theme theme)
	{
		super();
		setCurrentTheme(theme.aletheiaTheme);
	}

	public AletheiaLookAndFeel()
	{
		this(GUIAletheiaPreferences.instance.appearance().getTheme());
	}

}
