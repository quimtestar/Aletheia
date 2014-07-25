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
package aletheia.gui.cli.command.prooffinder;

import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "cpfc", groupPath = "/prooffinder", factory = ClearProofFinderCache.Factory.class)
public class ClearProofFinderCache extends Command
{

	protected ClearProofFinderCache(CliJPanel from)
	{
		super(from);
	}

	@Override
	public void run() throws Exception
	{
		getFrom().getAletheiaJPanel().getProofFinder().clearCache();
	}

	public static class Factory extends AbstractVoidCommandFactory<ClearProofFinderCache>
	{

		@Override
		public ClearProofFinderCache parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			return new ClearProofFinderCache(cliJPanel);
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
			return "Clears the automatic proof finder memory cache.";
		}

	}

}
