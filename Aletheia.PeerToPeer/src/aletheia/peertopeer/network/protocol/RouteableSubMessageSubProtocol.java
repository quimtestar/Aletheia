/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.network.protocol;

import java.io.DataOutput;
import java.io.IOException;

import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessageCode;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public abstract class RouteableSubMessageSubProtocol<M extends RouteableSubMessage> extends ExportableProtocol<M>
{
	private final RouteableSubMessageCode routeableSubMessageCode;

	public RouteableSubMessageSubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
	{
		super(0);
		checkVersionAvailability(RouteableSubMessageSubProtocol.class, requiredVersion);
		this.routeableSubMessageCode = routeableSubMessageCode;
	}

	protected RouteableSubMessageCode getRouteableSubMessageCode()
	{
		return routeableSubMessageCode;
	}

	@SuppressWarnings("unchecked")
	public final void sendMessage(DataOutput out, RouteableSubMessage m) throws IOException
	{
		send(out, (M) m);
	}

}
