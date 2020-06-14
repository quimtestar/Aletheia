/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
import aletheia.model.statement.RootContext;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "up", groupPath = "/gui", factory = Up.Factory.class)
public class Up extends TransactionalCommand
{
	public Up(CommandSource from, Transaction transaction)
	{
		super(from, transaction);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		if (ctx instanceof RootContext)
			ctx = null;
		else
			ctx = ctx.getContext(getTransaction());
		setActiveContext(ctx);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Up>
	{

		@Override
		public Up parse(CommandSource from, Transaction transaction, Void extra, List<String> split)
		{
			return new Up(from, transaction);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "";
		}

		@Override
		public String shortHelp()
		{
			return "Sets the active context one level up.";
		}

	}

}
