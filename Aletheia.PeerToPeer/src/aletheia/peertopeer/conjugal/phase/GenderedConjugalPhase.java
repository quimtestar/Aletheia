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
import java.util.UUID;

import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.LoopSubPhase;
import aletheia.peertopeer.base.phase.SubPhase;
import aletheia.peertopeer.conjugal.dialog.LoopConjugalDialogType;
import aletheia.peertopeer.conjugal.dialog.LoopConjugalDialogTypeDialogActive;
import aletheia.peertopeer.conjugal.dialog.LoopConjugalDialogTypeDialogPassive;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;

public abstract class GenderedConjugalPhase extends SubPhase
{
	protected abstract class LoopConjugalSubPhase extends LoopSubPhase<LoopConjugalDialogType>
	{

		protected abstract class Command<C extends Command<C>> extends LoopSubPhase<LoopConjugalDialogType>.Command<C>
		{
			public Command(LoopConjugalDialogType loopDialogType)
			{
				super(loopDialogType);
			}
		}

		protected class ValedictionCommand extends Command<ValedictionCommand>
		{
			public ValedictionCommand()
			{
				super(LoopConjugalDialogType.Valediction);
			}
		}

		public LoopConjugalSubPhase() throws IOException
		{
			super(GenderedConjugalPhase.this, LoopConjugalDialogTypeDialogActive.class, LoopConjugalDialogTypeDialogPassive.class);
		}

		@Override
		protected boolean serverPhase(LoopConjugalDialogType loopDialogType) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (loopDialogType)
			{
			case Valediction:
				valedictionDialog();
				return false;
			default:
				return genderedServerPhase(loopDialogType);
			}
		}

		protected abstract boolean genderedServerPhase(LoopConjugalDialogType loopDialogType) throws IOException, ProtocolException, InterruptedException,
				DialogStreamException;

		@Override
		protected boolean clientPhase(LoopSubPhase<LoopConjugalDialogType>.Command<?> command) throws IOException, ProtocolException, InterruptedException,
				DialogStreamException
		{
			switch (command.getLoopDialogType())
			{
			case Valediction:
				valedictionDialog();
				return false;
			default:
				return genderedClientPhase((Command<?>) command);
			}
		}

		protected abstract boolean genderedClientPhase(Command<?> command) throws IOException, ProtocolException, InterruptedException, DialogStreamException;

		@Override
		protected ValedictionCommand makeValedictionCommand()
		{
			return new ValedictionCommand();
		}

		protected <C extends Command<C>> void command(final Command<C> command, Transaction transaction)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					command(command);
				}
			});
		}

	}

	public GenderedConjugalPhase(ConjugalPhase conjugalPhase)
	{
		super(conjugalPhase);
	}

	@Override
	protected ConjugalPhase getParentPhase()
	{
		return (ConjugalPhase) super.getParentPhase();
	}

	protected abstract LoopConjugalSubPhase getLoopConjugalSubPhase();

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		getLoopConjugalSubPhase().run();
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		getLoopConjugalSubPhase().shutdown(fast);
	}

	public UUID getPeerNodeUuid()
	{
		return getParentPhase().getPeerNodeUuid();
	}

	public class OpenConnectionException extends Exception
	{
		private static final long serialVersionUID = -3963550560694333928L;

		protected OpenConnectionException(String message)
		{
			super(message);
		}
	}

}
