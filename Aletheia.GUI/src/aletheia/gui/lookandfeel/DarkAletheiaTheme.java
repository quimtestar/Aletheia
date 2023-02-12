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
	static DarkAletheiaTheme instance = new DarkAletheiaTheme();

	private DarkAletheiaTheme()
	{
		put(Key.default_, new ColorUIResource(224, 224, 224));
		put(Key.normalBackground, new ColorUIResource(0x202020));
		put(Key.selectedBackground, black);
		put(Key.activeContext, lightPurple);
		put(Key.groupSorter, new ColorUIResource(0x8080ff));
		put(Key.provenLabel, lightGreen);
		put(Key.unprovenLabel, lightOrange);
		put(Key.notValidSignatureSymbol, lightGray);
		put(Key.validSignatureSymbol, lightGray);
		put(Key.signedDependenciesSymbol, lightGray);
		put(Key.signedProofSymbol, lightGray);
		put(Key.subscribeSymbol, lightCyan);
	}

}
