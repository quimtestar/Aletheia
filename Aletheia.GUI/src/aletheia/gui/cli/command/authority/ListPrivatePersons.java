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
