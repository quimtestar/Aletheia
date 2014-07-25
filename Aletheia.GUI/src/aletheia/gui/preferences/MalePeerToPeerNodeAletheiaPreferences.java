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

public class MalePeerToPeerNodeAletheiaPreferences extends GenderPeerToPeerNodeAletheiaPreferences
{
	private final static String NODE_PATH = "male";

	private final static String P2P_SURROGATE_ADDRESS = "p2p_surrogate_address";
	private final static String P2P_SURROGATE_PORT = "p2p_surrogate_port";

	private final static String defaultP2pSurrogateAddress = "";
	private final static int defaultP2pSurrogatePort = 0;

	public MalePeerToPeerNodeAletheiaPreferences(PeerToPeerNodeAletheiaPreferences parent)
	{
		super(parent, NODE_PATH);
	}

	public String getP2pSurrogateAddress()
	{
		return getPreferences().get(P2P_SURROGATE_ADDRESS, defaultP2pSurrogateAddress);
	}

	public void setP2pSurrogateAddress(String p2pSurrogateAddress)
	{
		getPreferences().put(P2P_SURROGATE_ADDRESS, p2pSurrogateAddress);
	}

	public int getP2pSurrogatePort()
	{
		return getPreferences().getInt(P2P_SURROGATE_PORT, defaultP2pSurrogatePort);
	}

	public void setP2pSurrogatePort(int p2pSurrogatePort)
	{
		getPreferences().putInt(P2P_SURROGATE_PORT, p2pSurrogatePort);
	}

}
