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
package aletheia.gui.cli.command.statement;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractCommandFactory;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandGroup.CommandGroupException;
import aletheia.gui.cli.command.RootCommandGroup;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "new", groupPath = "/statement", factory = NewStatement.Factory.class)
public abstract class NewStatement extends TransactionalCommand
{
	private final Identifier identifier;

	public NewStatement(CliJPanel from, Transaction transaction, Identifier identifier)
	{
		super(from, transaction);
		this.identifier = identifier;
	}

	protected Identifier getIdentifier()
	{
		return identifier;
	}

	protected class RunNewStatementReturnData
	{
		public final Statement statement;

		public RunNewStatementReturnData(Statement statement)
		{
			this.statement = statement;
		}

	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		RunNewStatementReturnData nsData = runNewStatement();

		nsData.statement.identify(getTransaction(), identifier);
		getFrom().getAletheiaJPanel().getContextJTree().pushSelectStatement(getTransaction(), nsData.statement);

		Context newActiveContext;
		if (nsData.statement instanceof Context)
			newActiveContext = (Context) nsData.statement;
		else
			newActiveContext = nsData.statement.getContext(getTransaction());

		return new RunTransactionalReturnData(newActiveContext);

	}

	protected abstract RunNewStatementReturnData runNewStatement() throws Exception;

	public static class Factory extends AbstractVoidCommandFactory<NewStatement>
	{
		// @formatter:off
		private final static List<Class<? extends NewStatement>> taggedCommandList = Arrays.<Class<? extends NewStatement>> asList(
				NewContext.class,
				NewSpecialization.class,
				NewDeclaration.class,
				NewUnfoldingContext.class,
				NewStrip.class,
				NewCopy.class,
				NewRootContext.class
				);
		// @formatter:on

		private final Map<String, AbstractNewStatementFactory<? extends NewStatement>> taggedFactories;
		private final RootCommandGroup rootCommandGroup;

		public Factory()
		{
			try
			{
				this.taggedFactories = new HashMap<String, AbstractNewStatementFactory<? extends NewStatement>>();
				this.rootCommandGroup = new RootCommandGroup();
				for (Class<? extends Command> c : taggedCommandList)
				{
					TaggedCommand tc = c.getAnnotation(TaggedCommand.class);
					Constructor<? extends AbstractCommandFactory<? extends Command, ?>> constructor = tc.factory().getConstructor();
					@SuppressWarnings("unchecked")
					AbstractNewStatementFactory<? extends NewStatement> factory = (AbstractNewStatementFactory<? extends NewStatement>) constructor
							.newInstance();
					if (taggedFactories.put(tc.tag(), factory) != null)
						throw new Error();
					rootCommandGroup.resolveOrCreatePath(tc.groupPath()).putFactory(tc.tag(), factory);
				}
			}
			catch (IllegalArgumentException | IllegalAccessException | SecurityException | InstantiationException | NoSuchMethodException
					| InvocationTargetException | CommandGroupException e1)
			{
				throw new Error(e1);
			}
			finally
			{

			}
		}

		@Override
		public NewStatement parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Identifier identifier = Identifier.parse(split.get(0));
				String tag = split.get(1);
				AbstractNewStatementFactory<? extends NewStatement> factory = taggedFactories.get(tag);
				if (factory == null)
					throw new CommandParseException("Bad new statement command");
				return factory.parse(cliJPanel, transaction, identifier, split.subList(2, split.size()));
			}
			catch (InvalidNameException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
		}

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		protected String paramSpec()
		{
			return "<identifier> <subcommand> ...";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a new statement with the given identifier. See subcommands.";
		}

		@Override
		public RootCommandGroup rootCommandGroup()
		{
			return rootCommandGroup;
		}

		@Override
		public AbstractNewStatementFactory<? extends NewStatement> getTaggedFactory(String tag)
		{
			return taggedFactories.get(tag);
		}

	}

	public static abstract class AbstractNewStatementFactory<C extends NewStatement> extends AbstractCommandFactory<C, Identifier>
	{

	}

}
