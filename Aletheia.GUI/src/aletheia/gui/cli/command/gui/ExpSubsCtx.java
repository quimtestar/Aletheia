/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.gui.cli.command.gui;

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "expsubsctx", groupPath = "/gui", factory = ExpSubsCtx.Factory.class)
public class ExpSubsCtx extends TransactionalCommand
{
	public ExpSubsCtx(CommandSource from, Transaction transaction)
	{
		super(from, transaction);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Context activeContext = getActiveContext();
		if (activeContext == null)
			throw new NotActiveContextException();
		expandSubscribedContexts(activeContext);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ExpSubsCtx>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public ExpSubsCtx parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			return new ExpSubsCtx(from, transaction);
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "Expands all the subscribed contexts under the active one.";
		}

	}
}
