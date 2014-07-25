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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class FemalePeerToPeerNodeAletheiaPreferences extends GenderPeerToPeerNodeAletheiaPreferences
{
	private final static String NODE_PATH = "female";

	private final static String P2P_EXTERNAL_ADDRESS = "p2p_external_address";
	private final static String P2P_EXTERNAL_PORT = "p2p_external_port";

	private final static int defaultP2pExternalPort = 0;

	public FemalePeerToPeerNodeAletheiaPreferences(PeerToPeerNodeAletheiaPreferences parent)
	{
		super(parent, NODE_PATH);
	}

	public InetAddress getP2pExternalAddress()
	{
		byte[] p2pAddressBytes = getPreferences().getByteArray(P2P_EXTERNAL_ADDRESS, null);
		if (p2pAddressBytes == null)
			return null;
		try
		{
			return InetAddress.getByAddress(p2pAddressBytes);
		}
		catch (UnknownHostException e)
		{
			return null;
		}
	}

	public void setP2pExternalAddress(InetAddress p2pExternalAddress)
	{
		if (p2pExternalAddress != null)
			getPreferences().putByteArray(P2P_EXTERNAL_ADDRESS, p2pExternalAddress.getAddress());
		else
			getPreferences().remove(P2P_EXTERNAL_ADDRESS);
	}

	public int getP2pExternalPort()
	{
		return getPreferences().getInt(P2P_EXTERNAL_PORT, defaultP2pExternalPort);
	}

	public void setP2pExternalPort(int p2pExternalPort)
	{
		getPreferences().putInt(P2P_EXTERNAL_PORT, p2pExternalPort);
	}

}
