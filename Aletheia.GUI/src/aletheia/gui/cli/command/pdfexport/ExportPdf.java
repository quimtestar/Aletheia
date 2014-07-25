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
package aletheia.gui.cli.command.pdfexport;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.pdfexport.document.ExpandedContextDocument;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "expdf", groupPath = "/pdf", factory = ExportPdf.Factory.class)
public class ExportPdf extends TransactionalCommand
{
	private final Context context;
	private final File file;

	public ExportPdf(CliJPanel from, Transaction transaction, Context context, File file)
	{
		super(from, transaction);
		this.context = context;
		this.file = file;
	}

	protected Context getContext()
	{
		return context;
	}

	protected File getFile()
	{
		return file;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		FileOutputStream fos = new FileOutputStream(file);
		try
		{
			new ExpandedContextDocument(getTransaction(), context, fos);
		}
		finally
		{
			fos.close();
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ExportPdf>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public ExportPdf parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				if (cliJPanel.getActiveContext() == null)
					throw new NotActiveContextException();
				Context context = (Context) cliJPanel.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(0)));
				if (context == null)
					throw new CommandParseException("Invalid context");
				File file = new File(split.get(1));
				return new ExportPdf(cliJPanel, transaction, context, file);
			}
			catch (ClassCastException e)
			{
				throw new CommandParseException("Invalid context");
			}
			catch (NotActiveContextException | InvalidNameException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}

		}

		@Override
		protected String paramSpec()
		{
			return "<context> <file>";
		}

		@Override
		public String shortHelp()
		{
			return "Exports the given context as a pdf file.";
		}

	}

}
