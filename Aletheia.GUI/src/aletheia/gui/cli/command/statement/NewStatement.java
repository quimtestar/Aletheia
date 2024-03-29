/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import aletheia.gui.cli.command.AbstractCommandFactory;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandGroup.CommandGroupException;
import aletheia.gui.cli.command.CommandSource;
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
	protected final static String subStatementFormat = "sub_%02d";
	protected final static int subStatementOverflow = 100;

	private final Identifier identifier;

	public NewStatement(CommandSource from, Transaction transaction, Identifier identifier)
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
		Statement statement = nsData.statement.refresh(getTransaction());
		if (!statement.isProved() && statement instanceof Context)
			putSelectContextConsequent(getTransaction(), (Context) statement);
		else
			putSelectStatement(getTransaction(), statement);

		Context newActiveContext;
		if (statement instanceof Context && !statement.isProved())
			newActiveContext = (Context) statement;
		else
			newActiveContext = statement.getContext(getTransaction());

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
				NewRootContext.class,
				NewAuto.class,
				ContextFromStatement.class
				);
		// @formatter:on

		private final SortedMap<String, AbstractNewStatementFactory<? extends NewStatement>> taggedFactories;
		private final RootCommandGroup rootCommandGroup;

		public Factory()
		{
			try
			{
				this.taggedFactories = new TreeMap<>();
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
		public NewStatement parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Identifier identifier = Identifier.parse(split.get(0));
				String tag = split.get(1);
				AbstractNewStatementFactory<? extends NewStatement> factory = taggedFactories.get(tag);
				if (factory == null)
					throw new CommandParseException("Bad new statement command");
				return factory.parse(from, transaction, identifier, split.subList(2, split.size()));
			}
			catch (InvalidNameException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
		}

		@Override
		public CompletionSet completionSet(CommandSource from, List<String> split)
		{
			switch (split.size())
			{
			case 0:
				return null;
			case 1:
				return identifierCompletionSet(from, split);
			case 2:
				String prefix = split.get(1);
				return new CompletionSet(prefix, taggedFactories.subMap(prefix, prefix.concat(String.valueOf(Character.MAX_VALUE))).keySet(), " ");
			default:
				String tag = split.get(1);
				AbstractNewStatementFactory<? extends NewStatement> factory = taggedFactories.get(tag);
				if (factory == null)
					return null;
				return factory.completionSet(from, split.subList(2, split.size()));
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
