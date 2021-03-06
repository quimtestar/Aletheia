/*******************************************************************************
 * Copyright (c) 2018, 2019 Quim Testar
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
package aletheia.gui.app;

import java.awt.Toolkit;
import java.util.Collections;
import java.util.Map;

import aletheia.utilities.CommandLineArguments.Switch;
import aletheia.version.VersionManager;

public abstract class AletheiaGUI
{
	private final Map<String, Switch> globalSwitches;
	private final AletheiaEventQueue aletheiaEventQueue;

	public AletheiaGUI(Map<String, Switch> globalSwitches)
	{
		this.globalSwitches = globalSwitches;
		this.aletheiaEventQueue = new AletheiaEventQueue();
	}

	public AletheiaGUI()
	{
		this(Collections.emptyMap());
	}

	public Map<String, Switch> getGlobalSwitches()
	{
		return globalSwitches;
	}

	public AletheiaEventQueue getAletheiaEventQueue()
	{
		return aletheiaEventQueue;
	}

	protected void run()
	{
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(aletheiaEventQueue);
	}

	protected static void version()
	{
		System.out.println(VersionManager.getVersion());
	}

}
