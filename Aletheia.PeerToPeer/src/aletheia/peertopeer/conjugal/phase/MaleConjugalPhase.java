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
package aletheia.peertopeer.conjugal.phase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;

import aletheia.peertopeer.SplicedConnectionId;
import aletheia.peertopeer.MalePeerToPeerNode;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.conjugal.dialog.FemaleOpenConnectionDialogMale;
import aletheia.peertopeer.conjugal.dialog.LoopConjugalDialogType;
import aletheia.peertopeer.conjugal.dialog.MaleOpenConnectionDialogMale;
import aletheia.peertopeer.conjugal.dialog.UpdateMaleNodeUuidsDialogMale;
import aletheia.peertopeer.network.dialog.UpdateBindPortLoopDialogServer;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.AsynchronousInvoker;

public class MaleConjugalPhase extends GenderedConjugalPhase
{
	protected class MaleLoopConjugalSubPhase extends LoopConjugalSubPhase
	{

		public MaleLoopConjugalSubPhase() throws IOException
		{
			super();
		}

		protected class MaleOpenConnectionCommand extends Command<MaleOpenConnectionCommand>
		{
			protected abstract class Result extends Command<MaleOpenConnectionCommand>.Result
			{

			}

			protected class ConnectedResult extends Result
			{
				private final SplicedConnectionId splicedConnectionId;

				protected ConnectedResult(SplicedConnectionId splicedConnectionId)
				{
					super();
					this.splicedConnectionId = splicedConnectionId;
				}

				protected SplicedConnectionId getSplicedConnectionId()
				{
					return splicedConnectionId;
				}

			}

			protected class ErrorResult extends Result
			{
				private final String message;

				protected ErrorResult(String message)
				{
					super();
					this.message = message;
				}

				protected String getMessage()
				{
					return message;
				}
			}

			private final InetSocketAddress socketAddress;
			private final UUID expectedPeerNodeUuid;

			public MaleOpenConnectionCommand(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid)
			{
				super(LoopConjugalDialogType.MaleOpenConnection);
				this.socketAddress = socketAddress;
				this.expectedPeerNodeUuid = expectedPeerNodeUuid;
			}

			public InetSocketAddress getSocketAddress()
			{
				return socketAddress;
			}

			public UUID getExpectedPeerNodeUuid()
			{
				return expectedPeerNodeUuid;
			}

			@Override
			public int hashCode()
			{
				return System.identityHashCode(this);
			}

			@Override
			public boolean equals(Object obj)
			{
				return this == obj;
			}

		}

		protected class UpdateMaleNodeUuidsCommand extends Command<UpdateMaleNodeUuidsCommand>
		{
			private final Collection<UUID> addUuids;
			private final Collection<UUID> removeUuids;

			public UpdateMaleNodeUuidsCommand(Collection<UUID> addUuids, Collection<UUID> removeUuids)
			{
				super(LoopConjugalDialogType.UpdateMaleNodeUuids);
				this.addUuids = addUuids;
				this.removeUuids = removeUuids;
			}

			protected Collection<UUID> getAddUuids()
			{
				return addUuids;
			}

			protected Collection<UUID> getRemoveUuids()
			{
				return removeUuids;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((addUuids == null) ? 0 : addUuids.hashCode());
				result = prime * result + ((removeUuids == null) ? 0 : removeUuids.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				UpdateMaleNodeUuidsCommand other = (UpdateMaleNodeUuidsCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (addUuids == null)
				{
					if (other.addUuids != null)
						return false;
				}
				else if (!addUuids.equals(other.addUuids))
					return false;
				if (removeUuids == null)
				{
					if (other.removeUuids != null)
						return false;
				}
				else if (!removeUuids.equals(other.removeUuids))
					return false;
				return true;
			}

			private MaleLoopConjugalSubPhase getOuterType()
			{
				return MaleLoopConjugalSubPhase.this;
			}

		}

		@Override
		protected boolean genderedServerPhase(LoopConjugalDialogType loopDialogType) throws IOException, ProtocolException, InterruptedException,
		DialogStreamException
		{
			switch (loopDialogType)
			{
			case UpdateBindPort:
				updateBindPortDialogMale();
				return true;
			case FemaleOpenConnection:
				femaleOpenConnectionDialogMale();
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected boolean genderedClientPhase(Command<?> command) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (command.getLoopDialogType())
			{
			case MaleOpenConnection:
				maleOpenConnectionDialogMale((MaleOpenConnectionCommand) command);
				return true;
			case UpdateMaleNodeUuids:
				updateMaleNodeUuidsDialogMale((UpdateMaleNodeUuidsCommand) command);
				return true;
			default:
				throw new Error();
			}
		}

		private void updateBindPortDialogMale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			UpdateBindPortLoopDialogServer updateBindPortLoopDialogServer = dialog(UpdateBindPortLoopDialogServer.class, this);
			getMalePeerToPeerNode().setExternalBindSocketPort(updateBindPortLoopDialogServer.getBindPort());
		}

		private void maleOpenConnectionDialogMale(MaleOpenConnectionCommand command) throws IOException, ProtocolException, InterruptedException,
		DialogStreamException
		{
			try
			{
				MaleOpenConnectionDialogMale openConnectionDialogMale = dialog(MaleOpenConnectionDialogMale.class, this, command.getSocketAddress(),
						command.getExpectedPeerNodeUuid());
				MaleOpenConnectionCommand.Result result;
				SplicedConnectionId splicedConnectionId = openConnectionDialogMale.getSplicedConnectionId();
				if (splicedConnectionId != null)
					result = command.new ConnectedResult(splicedConnectionId);
				else
					result = command.new ErrorResult(openConnectionDialogMale.getErrorMessage());
				command.setResult(result);
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void updateMaleNodeUuidsDialogMale(UpdateMaleNodeUuidsCommand command) throws IOException, ProtocolException, InterruptedException,
		DialogStreamException
		{
			dialog(UpdateMaleNodeUuidsDialogMale.class, this, command.getAddUuids(), command.getRemoveUuids());
		}

		private void femaleOpenConnectionDialogMale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(FemaleOpenConnectionDialogMale.class, this);
		}

		public SplicedConnectionId maleOpenConnection(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid) throws InterruptedException,
		CancelledCommandException, OpenConnectionException
		{
			MaleOpenConnectionCommand.Result result = (MaleOpenConnectionCommand.Result) commandResult(new MaleOpenConnectionCommand(socketAddress,
					expectedPeerNodeUuid));
			if (result instanceof MaleOpenConnectionCommand.ConnectedResult)
			{
				MaleOpenConnectionCommand.ConnectedResult connectedResult = (MaleOpenConnectionCommand.ConnectedResult) result;
				return connectedResult.getSplicedConnectionId();
			}
			else if (result instanceof MaleOpenConnectionCommand.ErrorResult)
				throw new OpenConnectionException(((MaleOpenConnectionCommand.ErrorResult) result).getMessage());
			else
				throw new Error();
		}

		public void updateMaleNodeUuids(Collection<UUID> addUuids, Collection<UUID> removeUuids)
		{
			command(new UpdateMaleNodeUuidsCommand(addUuids, removeUuids));
		}

	}

	private final MaleLoopConjugalSubPhase maleLoopConjugalSubPhase;

	public MaleConjugalPhase(ConjugalPhase conjugalPhase) throws IOException
	{
		super(conjugalPhase);
		maleLoopConjugalSubPhase = new MaleLoopConjugalSubPhase();

		AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
		{

			@Override
			public void invoke()
			{
				updateMaleNodeUuids(getPeerToPeerNode().maleNodeUuids(), null);
			}
		});
	}

	private MalePeerToPeerNode getMalePeerToPeerNode()
	{
		return (MalePeerToPeerNode) getPeerToPeerNode();
	}

	@Override
	protected LoopConjugalSubPhase getLoopConjugalSubPhase()
	{
		return maleLoopConjugalSubPhase;
	}

	public SplicedConnectionId maleOpenConnection(InetSocketAddress socketAddress, UUID expectedPeerNodeUuid) throws InterruptedException,
	CancelledCommandException, OpenConnectionException
	{
		return maleLoopConjugalSubPhase.maleOpenConnection(socketAddress, expectedPeerNodeUuid);
	}

	public void updateMaleNodeUuids(Collection<UUID> addUuids, Collection<UUID> removeUuids)
	{
		maleLoopConjugalSubPhase.updateMaleNodeUuids(addUuids, removeUuids);
	}

}
