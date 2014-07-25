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
package aletheia.gui.cli.command.authority;

import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.Transaction;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.CloseableSortedMap;

@TaggedCommand(tag = "lpp", groupPath = "/authority", factory = ListPrivatePersons.Factory.class)
public class ListPrivatePersons extends TransactionalCommand
{
	private final String fromNick;
	private final String toNick;

	protected ListPrivatePersons(CliJPanel from, Transaction transaction, String fromNick, String toNick)
	{
		super(from, transaction);
		this.fromNick = fromNick;
		this.toNick = toNick;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		CloseableSortedMap<String, PrivatePerson> pbn = getPersistenceManager().privatePersonsByNick(getTransaction());
		String toNick_;
		if (toNick != null && pbn.containsKey(toNick))
			toNick_ = MiscUtilities.nthFromCloseableIterable(pbn.tailMap(toNick).keySet(), 1);
		else
			toNick_ = toNick;
		for (PrivatePerson pp : pbn.subMap(fromNick, toNick_).values())
			getOut().println(pp.getNick() + "\t" + pp.getName() + "\t" + pp.getEmail() + "\t" + pp.getUuid());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ListPrivatePersons>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public ListPrivatePersons parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			String fromNick = split.size() > 0 ? split.get(0) : null;
			String toNick = split.size() > 1 ? split.get(1) : fromNick;
			return new ListPrivatePersons(cliJPanel, transaction, fromNick, toNick);
		}

		@Override
		protected String paramSpec()
		{
			return "[<nick> [<to nick>]]";
		}

		@Override
		public String shortHelp()
		{
			return "Lists (an interval of) persons with private keys of the system.";
		}

	}

}
