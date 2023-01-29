/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import aletheia.peertopeer.SplicedConnectionId;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.conjugal.dialog.FemaleOpenConnectionDialogFemale;
import aletheia.peertopeer.conjugal.dialog.LoopConjugalDialogType;
import aletheia.peertopeer.conjugal.dialog.MaleOpenConnectionDialogFemale;
import aletheia.peertopeer.conjugal.dialog.UpdateMaleNodeUuidsDialogFemale;
import aletheia.peertopeer.network.dialog.UpdateBindPortLoopDialogClient;
import aletheia.protocol.ProtocolException;

public class FemaleConjugalPhase extends GenderedConjugalPhase
{
	protected class FemaleLoopConjugalSubPhase extends LoopConjugalSubPhase
	{

		public FemaleLoopConjugalSubPhase() throws IOException
		{
			super();
		}

		protected class UpdateBindPortCommand extends Command<UpdateBindPortCommand>
		{
			public UpdateBindPortCommand()
			{
				super(LoopConjugalDialogType.UpdateBindPort);
			}
		}

		protected class FemaleOpenConnectionCommand extends Command<FemaleOpenConnectionCommand>
		{
			protected abstract class Result extends Command<FemaleOpenConnectionCommand>.Result
			{

			}

			protected class AcceptedResult extends Result
			{
				protected AcceptedResult()
				{
					super();
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

			private final SplicedConnectionId splicedConnectionId;

			public FemaleOpenConnectionCommand(SplicedConnectionId splicedConnectionId)
			{
				super(LoopConjugalDialogType.FemaleOpenConnection);
				this.splicedConnectionId = splicedConnectionId;
			}

			public SplicedConnectionId getSplicedConnectionId()
			{
				return splicedConnectionId;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((splicedConnectionId == null) ? 0 : splicedConnectionId.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				FemaleOpenConnectionCommand other = (FemaleOpenConnectionCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (splicedConnectionId == null)
				{
					if (other.splicedConnectionId != null)
						return false;
				}
				else if (!splicedConnectionId.equals(other.splicedConnectionId))
					return false;
				return true;
			}

			private FemaleLoopConjugalSubPhase getOuterType()
			{
				return FemaleLoopConjugalSubPhase.this;
			}

		}

		@Override
		protected boolean genderedServerPhase(LoopConjugalDialogType loopDialogType)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (loopDialogType)
			{
			case MaleOpenConnection:
				maleOpenConnectionDialogFemale();
				return true;
			case UpdateMaleNodeUuids:
				updateMaleNodeUuidsDialogFemale();
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
			case UpdateBindPort:
				updateBindPortDialogFemale();
				return true;
			case FemaleOpenConnection:
				femaleOpenConnectionDialogFemale((FemaleOpenConnectionCommand) command);
				return true;
			default:
				throw new Error();
			}
		}

		private void updateBindPortDialogFemale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateBindPortLoopDialogClient.class, this);
		}

		private void maleOpenConnectionDialogFemale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(MaleOpenConnectionDialogFemale.class, this);
		}

		private void femaleOpenConnectionDialogFemale(FemaleOpenConnectionCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				FemaleOpenConnectionDialogFemale dialog = dialog(FemaleOpenConnectionDialogFemale.class, this, command.getSplicedConnectionId());
				FemaleOpenConnectionCommand.Result result;
				if (dialog.isAccepted())
					result = command.new AcceptedResult();
				else
					result = command.new ErrorResult(dialog.getErrorMessage());
				command.setResult(result);
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void updateMaleNodeUuidsDialogFemale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateMaleNodeUuidsDialogFemale.class, this);
		}

		public void updateBindPort()
		{
			command(new UpdateBindPortCommand());
		}

		public void femaleOpenConnection(SplicedConnectionId splicedConnectionId)
				throws InterruptedException, CancelledCommandException, OpenConnectionException
		{
			FemaleOpenConnectionCommand.Result result = (FemaleOpenConnectionCommand.Result) commandResult(
					new FemaleOpenConnectionCommand(splicedConnectionId));
			if (result instanceof FemaleOpenConnectionCommand.AcceptedResult)
				return;
			else if (result instanceof FemaleOpenConnectionCommand.ErrorResult)
				throw new OpenConnectionException(((FemaleOpenConnectionCommand.ErrorResult) result).getMessage());
			else
				throw new Error();
		}
	}

	private final FemaleLoopConjugalSubPhase femaleLoopConjugalSubPhase;

	public FemaleConjugalPhase(ConjugalPhase conjugalPhase) throws IOException
	{
		super(conjugalPhase);
		femaleLoopConjugalSubPhase = new FemaleLoopConjugalSubPhase();

		updateBindPort();
	}

	@Override
	protected FemaleLoopConjugalSubPhase getLoopConjugalSubPhase()
	{
		return femaleLoopConjugalSubPhase;
	}

	public void updateBindPort()
	{
		femaleLoopConjugalSubPhase.updateBindPort();
	}

	public void femaleOpenConnection(SplicedConnectionId splicedConnectionId) throws InterruptedException, CancelledCommandException, OpenConnectionException
	{
		femaleLoopConjugalSubPhase.femaleOpenConnection(splicedConnectionId);
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		try
		{
			super.run();
		}
		finally
		{
			getPeerToPeerNode().terminatedFemaleConjugalPhase(this);
		}
	}

}
