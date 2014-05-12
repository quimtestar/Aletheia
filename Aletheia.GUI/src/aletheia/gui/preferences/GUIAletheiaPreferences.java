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
package aletheia.gui.preferences;

import aletheia.preferences.NodeAletheiaPreferences;
import aletheia.preferences.RootAletheiaPreferences;

public class GUIAletheiaPreferences extends NodeAletheiaPreferences
{
	private final static String NODE_PATH = "GUI";

	private final static String PERSISTENCE_CLASS = "persistence_class";

	public final static GUIAletheiaPreferences instance = new GUIAletheiaPreferences();

	private final AppearanceAletheiaPreferences appearanceAletheiaPreferences;

	private final PeerToPeerNodeAletheiaPreferences peerToPeerNodeAletheiaPreferences;

	private GUIAletheiaPreferences()
	{
		super(RootAletheiaPreferences.instance, NODE_PATH);
		this.appearanceAletheiaPreferences = new AppearanceAletheiaPreferences(this);
		this.peerToPeerNodeAletheiaPreferences = new PeerToPeerNodeAletheiaPreferences(this);
	}

	public PersistenceClass getPersistenceClass()
	{
		return PersistenceClass.valueOf(getPreferences().get(PERSISTENCE_CLASS, PersistenceClass.values()[0].name()));
	}

	public void setPersistenceClass(PersistenceClass persistenceClass)
	{
		getPreferences().put(PERSISTENCE_CLASS, persistenceClass.name());
	}

	public AppearanceAletheiaPreferences appearance()
	{
		return appearanceAletheiaPreferences;
	}

	public PeerToPeerNodeAletheiaPreferences peerToPeerNode()
	{
		return peerToPeerNodeAletheiaPreferences;
	}

}
