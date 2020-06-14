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
package aletheia.gui.cli.command.prooffinder;

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "pft", groupPath = "/prooffinder", factory = ProofFinderThrottle.Factory.class)
public abstract class ProofFinderThrottle extends Command
{

	protected ProofFinderThrottle(CommandSource from)
	{
		super(from);
	}

	public static class Factory extends AbstractVoidCommandFactory<ProofFinderThrottle>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public ProofFinderThrottle parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			if (split.size() > 0)
			{
				float throttle = Float.parseFloat(split.get(0));
				return new ProofFinderSetThrottle(from, throttle);
			}
			else
			{
				return new ProofFinderGetThrottle(from);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<throttle>]";
		}

		@Override
		public String shortHelp()
		{
			return "Obtains/changes the cpu throttle (float number from 0 to 1) of the proof finder thread.";
		}

	}

}
