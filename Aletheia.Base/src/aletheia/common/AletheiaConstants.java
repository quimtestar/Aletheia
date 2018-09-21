/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.common;

/**
 * Constants for the general use in the Aletheia system.
 */
public class AletheiaConstants
{
	/**
	 * 'Aletheia' in the greek alphabet for use as a window title.
	 */
	private static final String TITLE_default = "\u1f08\u03bb\u03ae\u03b8\u03b5\u03b9\u03b1 identifier_parameters";
	public static final String TITLE;

	/**
	 * Path of the user preferences' node to use for storing the preferences
	 * related to the system.
	 *
	 * @see java.util.prefs
	 */
	private static final String PREFERENCES_NODE_PATH_default = "aletheia_identified_parameters";
	public static final String PREFERENCES_NODE_PATH;

	static
	{
		{
			String s = System.getProperty("aletheia.title");
			TITLE = s == null ? TITLE_default : s;
		}
		{
			String s = System.getProperty("aletheia.preferences.node");
			PREFERENCES_NODE_PATH = s == null ? PREFERENCES_NODE_PATH_default : s;
		}
	}

}
