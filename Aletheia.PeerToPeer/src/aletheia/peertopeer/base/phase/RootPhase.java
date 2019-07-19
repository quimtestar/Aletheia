/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer.base.phase;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerConnection.Gender;
import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.dialog.SalutationDialog;
import aletheia.peertopeer.network.InitialNetworkPhaseType;
import aletheia.peertopeer.network.NetworkMalePeerToPeerConnection;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.MiscUtilities.NoConstructorException;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public abstract class RootPhase extends Phase
{
	private final static Logger logger = LoggerManager.instance.logger();
	private final static int localProtocolVersion = 3;

	private int protocolVersion;
	private SubRootPhaseType subRootPhaseType;
	private SubRootPhase subRootPhase;
	private boolean shutdown;
	private boolean shutdownFast;
	private boolean ended;

	public RootPhase(PeerToPeerConnection peerToPeerConnection)
	{
		super(peerToPeerConnection);
		this.protocolVersion = 0;
		this.subRootPhaseType = null;
		this.subRootPhase = null;
		this.shutdown = false;
		this.shutdownFast = false;
		this.ended = false;
	}

	@Override
	public int getProtocolVersion()
	{
		return protocolVersion;
	}

	public SubRootPhaseType getSubRootPhaseType()
	{
		return subRootPhaseType;
	}

	public synchronized SubRootPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		ListenableAborter.Listener aborterListener = new ListenableAborter.Listener()
		{
			@Override
			public void abort()
			{
				synchronized (RootPhase.this)
				{
					RootPhase.this.notifyAll();
				}
			}
		};
		aborter.addListener(aborterListener);
		try
		{
			while (!ended && subRootPhase == null)
			{
				aborter.checkAbort();
				wait();
			}
			return subRootPhase;
		}
		finally
		{
			aborter.removeListener(aborterListener);
		}
	}

	public synchronized SubRootPhase waitForSubRootPhase() throws InterruptedException
	{
		try
		{
			return waitForSubRootPhase(ListenableAborter.nullListenableAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	private synchronized void setSubRootPhase(SubRootPhase subRootPhase) throws InterruptedException
	{
		this.subRootPhase = subRootPhase;
		if (shutdown == true)
			subRootPhase.shutdown(shutdownFast);
		notifyAll();
	}

	private synchronized void setEnded()
	{
		this.ended = true;
		notifyAll();
	}

	protected abstract SalutationDialog salutationDialog(int localProtcolVersion)
			throws IOException, ProtocolException, InterruptedException, DialogStreamException;

	protected abstract SubRootPhaseType subRootPhaseDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException;

	private SubRootPhase createSubRootPhase(SubRootPhaseType subRootPhaseType, UUID peerNodeUuid)
	{
		try
		{
			return MiscUtilities.construct(subRootPhaseType.getSubRootPhaseClass(), this, peerNodeUuid);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoConstructorException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug(": starting");
		boolean subRootPhaseRan = false;
		try
		{
			SalutationDialog salutationDialog = salutationDialog(localProtocolVersion);
			protocolVersion = Math.min(salutationDialog.getPeerProtocolVersion(), localProtocolVersion);
			if (protocolVersion < 0)
				throw new ProtocolException();
			if (salutationDialog.isPeerNodeUuidValid())
			{
				UUID peerNodeUuid = salutationDialog.getPeerNodeUuid();
				logger.debug("nodeUuid: " + getNodeUuid() + "    peerNodeUuid: " + peerNodeUuid);
				subRootPhaseType = subRootPhaseDialog();
				if (subRootPhaseType != null)
				{
					logger.debug("subRootPhaseType: " + subRootPhaseType);
					SubRootPhase subRootPhase = createSubRootPhase(subRootPhaseType, peerNodeUuid);
					try
					{
						setSubRootPhase(subRootPhase);
						if (getPeerToPeerNode().addSubRootPhase(subRootPhase))
						{
							subRootPhase.run();
							subRootPhaseRan = true;
						}
					}
					finally
					{
						getPeerToPeerNode().removeSubRootPhase(subRootPhase);
					}
				}
			}
		}
		finally
		{
			if (getGender() == Gender.MALE && getSubRootPhaseType() == SubRootPhaseType.Network)
				if (((NetworkMalePeerToPeerConnection) getPeerToPeerConnection()).getInitialNetworkPhaseType() == InitialNetworkPhaseType.Joining)
					if (!subRootPhaseRan)
						getPeerToPeerNode().setNotJoiningToNetworkIfPending();
			setEnded();
			logger.debug(": ending");
		}
	}

	@Override
	public synchronized void shutdown(boolean fast)
	{
		super.shutdown(fast);
		shutdown = true;
		shutdownFast = fast;
		if (subRootPhase != null)
			subRootPhase.shutdown(fast);
	}

	@Override
	protected RootPhase rootPhase()
	{
		return this;
	}

}
