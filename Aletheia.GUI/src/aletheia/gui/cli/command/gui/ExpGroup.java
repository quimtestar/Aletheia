/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "expgroup", groupPath = "/gui", factory = ExpGroup.Factory.class)
public class ExpGroup extends TransactionalCommand
{
	private final Namespace prefix;

	public ExpGroup(CliJPanel from, Transaction transaction, Namespace prefix)
	{
		super(from, transaction);
		this.prefix = prefix;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		expandGroup(getActiveContext(), prefix);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ExpGroup>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public ExpGroup parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			Namespace prefix = RootNamespace.instance;
			if (split.size() > 0)
				try
				{
					prefix = Namespace.parse(split.get(0));
				}
				catch (InvalidNameException e)
				{
					throw new CommandParseException(e);
				}
			return new ExpGroup(cliJPanel, transaction, prefix);
		}

		@Override
		protected String paramSpec()
		{
			return "[<prefix>]";
		}

		@Override
		public String shortHelp()
		{
			return "Expands all the subgroups of a specificed prefix on this context.";
		}

	}
}
