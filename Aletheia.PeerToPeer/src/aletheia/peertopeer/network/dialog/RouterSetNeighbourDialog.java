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
package aletheia.peertopeer.network.dialog;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.LocalRouterSet;
import aletheia.peertopeer.network.LocalRouterSet.BookedNeighbourPositionException;
import aletheia.peertopeer.network.LocalRouterSet.LocalRouterSetBookTimeoutException;
import aletheia.peertopeer.network.LocalRouterSet.NeighbourCollisionException;
import aletheia.peertopeer.network.message.RouterSetNeighbourMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class RouterSetNeighbourDialog extends NetworkNonPersistentDialog
{
	private final static Logger logger = LoggerManager.instance.logger();
	private final static int bookNeighbourTimeout = 5000;

	private boolean put;

	public RouterSetNeighbourDialog(Phase phase)
	{
		super(phase);
		this.put = false;
	}

	public boolean isPut()
	{
		return put;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		LocalRouterSet localRouterSet = getLocalRouterSet();
		int i = localRouterSet.neighbourPosition(getNetworkPhase());
		boolean put = false;
		try
		{
			synchronized (localRouterSet)
			{
				if (localRouterSet.getNeighbour(i) == null)
				{
					try
					{
						localRouterSet.bookNeighbourWait(i, bookNeighbourTimeout);
						put = true;
					}
					catch (NeighbourCollisionException | LocalRouterSetBookTimeoutException e)
					{
						logger.debug("Not putting", e);
					}
				}
				else
					logger.debug("Not putting. Slot occupied.");
				sendMessage(new RouterSetNeighbourMessage(put));
			}
			RouterSetNeighbourMessage complementedMessage = recvMessage(RouterSetNeighbourMessage.class);
			synchronized (localRouterSet)
			{
				if (put && complementedMessage.isPut())
				{
					localRouterSet.unbookNeighbour(i);
					localRouterSet.putNeighbour(getNetworkPhase());
					this.put = true;
				}
				else
					logger.debug("Not putting.");
			}
		}
		catch (BookedNeighbourPositionException | NeighbourCollisionException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (put)
				localRouterSet.unbookNeighbour(i);
		}
	}

}
