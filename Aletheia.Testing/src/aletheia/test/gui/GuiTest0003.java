/*******************************************************************************
 * Copyright (c) 2019, 2020 Quim Testar.
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
package aletheia.test.gui;

import aletheia.gui.app.splash.SplashStartupProgressListener;
import aletheia.test.BerkeleyDBPersistenceManagerTest;

/**
 * Just testing the splash screen
 */
public class GuiTest0003 extends BerkeleyDBPersistenceManagerTest
{

	public GuiTest0003()
	{
		super();
		getConfiguration().setStartupProgressListener(new SplashStartupProgressListener());
	}

}
