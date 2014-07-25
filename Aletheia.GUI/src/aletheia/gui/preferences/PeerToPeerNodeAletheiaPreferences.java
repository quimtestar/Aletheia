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

public class PeerToPeerNodeAletheiaPreferences extends NodeAletheiaPreferences
{
	private final static String NODE_PATH = "peertopeernode_server";

	private final static String P2P_GENDER = "p2p_gender";

	private final static PeerToPeerNodeGender defaultP2pGender = PeerToPeerNodeGender.DISABLED;

	private final FemalePeerToPeerNodeAletheiaPreferences femalePeerToPeerNodeAletheiaPreferences;
	private final MalePeerToPeerNodeAletheiaPreferences malePeerToPeerNodeAletheiaPreferences;

	protected PeerToPeerNodeAletheiaPreferences(GUIAletheiaPreferences guiAletheiaPreferences)
	{
		super(guiAletheiaPreferences, NODE_PATH);
		this.femalePeerToPeerNodeAletheiaPreferences = new FemalePeerToPeerNodeAletheiaPreferences(this);
		this.malePeerToPeerNodeAletheiaPreferences = new MalePeerToPeerNodeAletheiaPreferences(this);
	}

	public PeerToPeerNodeGender getP2pGender()
	{
		return PeerToPeerNodeGender.valueOf(getPreferences().get(P2P_GENDER, defaultP2pGender.name()));
	}

	public void setP2pGender(PeerToPeerNodeGender p2pGender)
	{
		getPreferences().put(P2P_GENDER, p2pGender.name());
	}

	public FemalePeerToPeerNodeAletheiaPreferences femalePeerToPeerNode()
	{
		return femalePeerToPeerNodeAletheiaPreferences;
	}

	public MalePeerToPeerNodeAletheiaPreferences malePeerToPeerNode()
	{
		return malePeerToPeerNodeAletheiaPreferences;
	}

}
