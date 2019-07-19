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
package aletheia.gui.cli.command;

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.persistence.Transaction;

public abstract class DynamicCommand extends Command
{

	protected DynamicCommand(CommandSource from)
	{
		super(from);
	}

	public abstract void dynamicRun() throws Exception;

	@Override
	public final void run() throws Exception
	{
		try
		{
			dynamicRun();
		}
		catch (Throwable t)
		{
			throw new Exception(t);
		}
	}

	public static abstract class Factory<C extends DynamicCommand> extends AbstractVoidCommandFactory<C>
	{
		public Factory()
		{
		}

		@Override
		public final C parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				return dynamicParse(from, transaction, split);
			}
			catch (Throwable t)
			{
				throw new CommandParseException(t);
			}
		}

		public abstract C dynamicParse(CommandSource from, Transaction transaction, List<String> split) throws CommandParseException;

	}

}
