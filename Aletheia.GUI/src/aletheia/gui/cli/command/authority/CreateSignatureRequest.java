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

import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "csr", groupPath = "/authority", factory = CreateSignatureRequest.Factory.class)
public class CreateSignatureRequest extends TransactionalCommand
{
	private final Context context;

	public CreateSignatureRequest(CliJPanel from, Transaction transaction, Context context)
	{
		super(from, transaction);
		this.context = context;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		final UnpackedSignatureRequest unpackedSignatureRequest = UnpackedSignatureRequest.create(getPersistenceManager(), getTransaction(), new Date(),
				context);
		getTransaction().runWhenCommit(new Transaction.Hook()
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						signatureRequestJTreeSelectUnpackedSignatureRequest(unpackedSignatureRequest);
					}
				});
			}
		});
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<CreateSignatureRequest>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public CreateSignatureRequest parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Context context;
			if (split.size() > 0)
			{
				Statement statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
				if (statement == null)
					throw new CommandParseException("Invalid statement");
				if (!(statement instanceof Context))
					throw new CommandParseException("Not a context");
				context = (Context) statement;
			}
			else
			{
				context = cliJPanel.getActiveContext();
				if (context == null)
					throw new CommandParseException("No active context");
			}
			return new CreateSignatureRequest(cliJPanel, transaction, context);
		}

		@Override
		protected String paramSpec()
		{
			return "[<context>]";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a new signature request associated to a context.";
		}
	}

}
