/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.gui.cli.command.parameteridentification;

import java.util.List;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.SignatureIsValidException;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "upi", groupPath = "/pi", factory = UpdateParameterIdentification.Factory.class)
public abstract class UpdateParameterIdentification extends TransactionalCommand
{
	private final Statement statement;
	private final ParameterIdentification parameterIdentification;

	public UpdateParameterIdentification(CommandSource from, Transaction transaction, Statement statement, ParameterIdentification parameterIdentification)
	{
		super(from, transaction);
		this.statement = statement;
		this.parameterIdentification = parameterIdentification;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	protected ParameterIdentification getParameterIdentification()
	{
		return parameterIdentification;
	}

	protected abstract void updateParameterIdentification() throws SignatureIsValidException;

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		updateParameterIdentification();
		return null;
	}

	public static class UpdateTermParameterIdentification extends UpdateParameterIdentification
	{

		public UpdateTermParameterIdentification(CommandSource from, Transaction transaction, Statement statement,
				ParameterIdentification parameterIdentification)
		{
			super(from, transaction, statement, parameterIdentification);
		}

		@Override
		protected void updateParameterIdentification() throws SignatureIsValidException
		{
			//TODO
			throw new UnsupportedOperationException();
		}

	}

	public static class UpdateValueParameterIdentification extends UpdateParameterIdentification
	{

		public UpdateValueParameterIdentification(CommandSource from, Transaction transaction, Declaration declaration,
				ParameterIdentification parameterIdentification)
		{
			super(from, transaction, declaration, parameterIdentification);
		}

		@Override
		protected Declaration getStatement()
		{
			return (Declaration) super.getStatement();
		}

		@Override
		protected void updateParameterIdentification() throws SignatureIsValidException
		{
			getStatement().updateValueParameterIdentification(getTransaction(), getParameterIdentification());
		}

	}

	public static class UpdateInstanceParameterIdentification extends UpdateParameterIdentification
	{

		public UpdateInstanceParameterIdentification(CommandSource from, Transaction transaction, Specialization specialization,
				ParameterIdentification parameterIdentification)
		{
			super(from, transaction, specialization, parameterIdentification);
		}

		@Override
		protected Specialization getStatement()
		{
			return (Specialization) super.getStatement();
		}

		@Override
		protected void updateParameterIdentification() throws SignatureIsValidException
		{
			getStatement().updateInstanceParameterIdentification(getTransaction(), getParameterIdentification());
		}

	}

	public static class Factory extends AbstractVoidCommandFactory<UpdateParameterIdentification>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public UpdateParameterIdentification parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			boolean value = split.remove("-v") || split.remove("-value");
			boolean instance = split.remove("-i") || split.remove("-instance");
			if (value && instance)
				throw new CommandParseException("Can't specify both value and instance switches");
			Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			ParameterIdentification parameterIdentification = parseParameterIdentification(split.get(1));
			if (value)
			{
				if (!(statement instanceof Declaration))
					throw new CommandParseException("Statement not a declaration");
				return new UpdateValueParameterIdentification(from, transaction, (Declaration) statement, parameterIdentification);
			}
			else if (instance)
			{
				if (!(statement instanceof Specialization))
					throw new CommandParseException("Statement not a specialization");
				return new UpdateInstanceParameterIdentification(from, transaction, (Specialization) statement, parameterIdentification);
			}
			else
				return new UpdateTermParameterIdentification(from, transaction, statement, parameterIdentification);
		}

		@Override
		protected String paramSpec()
		{
			return "([-v[alue]] | [-i[nstance]]) <statement> <parameter identification>";
		}

		@Override
		public String shortHelp()
		{
			return "Update the parameter identification of a statement's term (or a declaration's value or a specialization's instance).";
		}

	}

}
