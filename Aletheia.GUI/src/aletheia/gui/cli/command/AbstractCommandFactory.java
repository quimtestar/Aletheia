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
package aletheia.gui.cli.command;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.Command.CommandParseEmbeddedException;
import aletheia.gui.cli.command.Command.CommandParseException;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaTermParser;
import aletheia.parser.TermParserException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureMap;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.EmptyCloseableSet;
import aletheia.utilities.collections.TrivialCloseableSet;

public abstract class AbstractCommandFactory<C extends Command, E>
{

	protected static Statement findStatementPath(PersistenceManager persistenceManager, Transaction transaction, Context ctx, String path)
			throws CommandParseException
	{
		try
		{
			Statement st;
			if (path.startsWith("/"))
			{
				int b2 = path.indexOf("/", 1);
				String sroot;
				if (b2 > 0)
				{
					sroot = path.substring(1, b2);
					path = path.substring(b2 + 1);
				}
				else
				{
					sroot = path.substring(1);
					path = "";
				}
				GenericRootContextsMap map = persistenceManager.identifierToRootContexts(transaction).get(Identifier.parse(sroot));
				if (map == null || map.isEmpty())
					return null;
				CloseableIterator<RootContext> iterator = map.values().iterator();
				try
				{
					st = iterator.next();
				}
				finally
				{
					iterator.close();
				}
			}
			else
			{
				if (ctx == null)
				{
					int b2 = path.indexOf("/");
					String sroot;
					if (b2 >= 0)
					{
						sroot = path.substring(0, b2);
						path = path.substring(b2 + 1);
					}
					else
					{
						sroot = path;
						path = "";
					}
					GenericRootContextsMap map = persistenceManager.identifierToRootContexts(transaction).get(Identifier.parse(sroot));
					if (map == null || map.isEmpty())
						return null;
					CloseableIterator<RootContext> iterator = map.values().iterator();
					try
					{
						st = iterator.next();
					}
					finally
					{
						iterator.close();
					}
				}
				else
					st = ctx;
			}
			if (!path.isEmpty())
			{
				for (String s : path.split("/"))
				{
					if ((st == null) || !(st instanceof Context))
						return null;
					st = ((Context) st).identifierToStatement(transaction).get(Identifier.parse(s));
				}
			}
			return st;
		}
		catch (InvalidNameException e)
		{
			throw CommandParseEmbeddedException.embed(e);
		}
		finally
		{

		}
	}

	protected static Collection<Statement> findMultiStatementPath(PersistenceManager persistenceManager, Transaction transaction, Context ctx, String path)
			throws CommandParseException
	{
		try
		{
			if (path.endsWith("."))
			{
				int b = path.lastIndexOf("/");
				String ctxPath = b >= 0 ? path.substring(0, b) : "";
				String sprefix = (b >= 0 ? path.substring(b + 1) : path).replaceFirst("\\.$", "");
				Statement st = findStatementPath(persistenceManager, transaction, ctx, ctxPath);
				if ((st == null) || !(st instanceof Context))
					return Collections.emptySet();
				Namespace prefix = Namespace.parse(sprefix);
				Identifier initiator = prefix instanceof NodeNamespace ? ((NodeNamespace) prefix).asIdentifier() : prefix.initiator();
				Identifier terminator = prefix.terminator();
				return ((Context) st).identifierToStatement(transaction).subMap(initiator, terminator).values();
			}
			else
			{
				Statement st = findStatementPath(persistenceManager, transaction, ctx, path);
				if (st == null)
					return Collections.emptySet();
				return Collections.singleton(st);
			}
		}
		catch (InvalidNameException e)
		{
			throw CommandParseEmbeddedException.embed(e);
		}

	}

	protected static Term parseTerm(Context ctx, Transaction transaction, String s, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws CommandParseException
	{
		try
		{
			return AletheiaTermParser.parseTerm(ctx, transaction, s, parameterIdentifiers);
		}
		catch (TermParserException e)
		{
			throw CommandParseEmbeddedException.embed(e);
		}
	}

	protected static Term parseTerm(Context ctx, Transaction transaction, String s) throws CommandParseException
	{
		return parseTerm(ctx, transaction, s, null);
	}

	protected static CloseableSet<Person> specToPersons(PersistenceManager persistenceManager, Transaction transaction, String personSpec)
	{
		try
		{
			UUID uuid = UUID.fromString(personSpec);
			Person person = persistenceManager.getPerson(transaction, uuid);
			if (person != null)
				return new TrivialCloseableSet<>(Collections.singleton(person));
		}
		catch (IllegalArgumentException e)
		{
		}
		CloseableSet<Person> set = persistenceManager.personsByNick(transaction).get(personSpec);
		if (set != null)
			return set;
		return new EmptyCloseableSet<>();
	}

	protected static Person specToPerson(PersistenceManager persistenceManager, Transaction transaction, String personSpec)
	{
		CloseableIterator<Person> iterator = specToPersons(persistenceManager, transaction, personSpec).iterator();
		try
		{
			if (iterator.hasNext())
			{
				Person person = iterator.next();
				if (!iterator.hasNext())
					return person;
			}
			return null;
		}
		finally
		{
			iterator.close();
		}
	}

	protected abstract int minParameters();

	public static class MissingParametersException extends CommandParseException
	{
		private static final long serialVersionUID = 6784530411346301645L;

		public MissingParametersException()
		{
			super("Missing parameters");
		}

	}

	protected void checkMinParameters(List<String> split) throws MissingParametersException
	{
		if (split.size() < minParameters())
			throw new MissingParametersException();
	}

	public abstract C parse(CliJPanel cliJPanel, Transaction transaction, E extra, List<String> split) throws CommandParseException;

	public RootCommandGroup rootCommandGroup()
	{
		return null;
	}

	public AbstractCommandFactory<? extends Command, ?> getTaggedFactory(String tag)
	{
		return null;
	}

	protected abstract String paramSpec();

	public abstract String shortHelp();

	public void longHelp(PrintStream out)
	{
		out.println(shortHelp());
	}

	public String commandSpec(String tag)
	{
		String paramSpec = paramSpec();
		if (paramSpec == null || paramSpec.isEmpty())
			return tag;
		else
			return tag + " " + paramSpec;
	}

	protected Collection<StatementAuthoritySignature> specToStatementAuthoritySignatures(PersistenceManager persistenceManager, Transaction transaction,
			Context activeContext, List<String> split) throws CommandParseException
	{
		Statement statement = findStatementPath(persistenceManager, transaction, activeContext, split.get(0));
		if (statement == null)
			throw new CommandParseException("Invalid statement");
		StatementAuthority statementAuthority = statement.getAuthority(transaction);
		if (statementAuthority == null)
			throw new CommandParseException("Statement not authored");
		StatementAuthoritySignatureMap signatureMap = statementAuthority.signatureMap(transaction);
		if (split.size() <= 1)
			return signatureMap.values();
		else if (split.size() <= 2)
		{
			Signatory authorizer = persistenceManager.getSignatory(transaction, UUID.fromString(split.get(1)));
			StatementAuthoritySignature signature = signatureMap.get(authorizer);
			if (signature == null)
				throw new CommandParseException("Signature not found");
			return Collections.singleton(signature);
		}
		else if (split.size() > 3)
		{
			Statement ctxSt = findStatementPath(persistenceManager, transaction, activeContext, split.get(1));
			Namespace prefix;
			try
			{
				prefix = Namespace.parse(split.get(2));
			}
			catch (InvalidNameException e1)
			{
				throw new CommandParseException(e1);
			}
			Person delegate = specToPerson(persistenceManager, transaction, split.get(3));
			DelegateAuthorizer da = ctxSt.getAuthority(transaction).getDelegateAuthorizer(transaction, prefix, delegate);
			if (da == null)
				throw new CommandParseException("Bad context/prefix/delegate");
			Signatory authorizer = da.getAuthorizer(transaction);
			if (authorizer == null)
				throw new CommandParseException("Authorizer not setted");
			StatementAuthoritySignature signature = signatureMap.get(authorizer);
			if (signature == null)
				throw new CommandParseException("Signature not found");
			return Collections.singleton(signature);
		}
		else
			throw new MissingParametersException();

	}

}
