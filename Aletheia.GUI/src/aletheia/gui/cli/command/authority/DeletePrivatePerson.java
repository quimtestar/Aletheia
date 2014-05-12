package aletheia.gui.cli.command.authority;

import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "dpp", groupPath = "/authority", factory = DeletePrivatePerson.Factory.class)
public class DeletePrivatePerson extends TransactionalCommand
{
	private final String nick;

	protected DeletePrivatePerson(CliJPanel from, Transaction transaction, String nick)
	{
		super(from, transaction);
		this.nick = nick;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		PrivatePerson person = getPersistenceManager().privatePersonsByNick(getTransaction()).get(nick);
		if (person == null)
			throw new Exception("Not found");
		if (!person.isOrphan())
			throw new Exception("Not orphan");
		person.delete(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeletePrivatePerson>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public DeletePrivatePerson parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			String nick = split.get(0);
			return new DeletePrivatePerson(cliJPanel, transaction, nick);
		}

		@Override
		protected String paramSpec()
		{
			return "<nick>";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes a person with private key by nick";
		}

	}

}
