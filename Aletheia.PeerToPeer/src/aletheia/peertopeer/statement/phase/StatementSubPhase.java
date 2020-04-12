/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
package aletheia.peertopeer.statement.phase;

import java.io.IOException;
import java.lang.reflect.Constructor;

import aletheia.peertopeer.base.dialog.Dialog;
import aletheia.peertopeer.base.phase.SubPhase;
import aletheia.peertopeer.statement.dialog.InitializeStatementProofDialog;
import aletheia.protocol.ProtocolException;

public abstract class StatementSubPhase extends SubPhase
{

	public StatementSubPhase(StatementPhase statementPhase)
	{
		super(statementPhase);
	}

	@Override
	protected StatementPhase getParentPhase()
	{
		return (StatementPhase) super.getParentPhase();
	}

	public StatementPhase getStatementPhase()
	{
		return getParentPhase();
	}

	public void statementProofSubPhase(Class<? extends InitializeStatementProofDialog> initializeStatementProofDialogClass, Object... initargs)
			throws IOException, ProtocolException, InterruptedException, Dialog.DialogStreamException
	{
		Constructor<? extends InitializeStatementProofDialog> initializeStatementProofDialogConstructor = dialogConstructor(initializeStatementProofDialogClass,
				initargs);
		StatementProofSubPhase statementProofSubPhase = new StatementProofSubPhase(this, initializeStatementProofDialogConstructor);
		statementProofSubPhase.run();
	}

}
