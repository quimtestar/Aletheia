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
package aletheia.gui.cli.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aletheia.gui.cli.command.AbstractCommandFactory.CompletionSet.Completion;
import aletheia.gui.cli.command.Command.CommandParseEmbeddedException;
import aletheia.gui.cli.command.Command.CommandParseException;
import aletheia.gui.cli.command.Command.CommandParseTermParserException;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NamespaceExtreme;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureMap;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;
import aletheia.utilities.collections.EmptyCloseableSet;
import aletheia.utilities.collections.TrivialCloseableSet;

public abstract class AbstractCommandFactory<C extends Command, E>
{

	protected static Statement findStatementSpec(PersistenceManager persistenceManager, Transaction transaction, Context ctx, String spec)
			throws CommandParseException
	{
		try
		{
			return persistenceManager.getStatement(transaction, UUID.fromString(spec));
		}
		catch (IllegalArgumentException e)
		{
			return findStatementPath(persistenceManager, transaction, ctx, spec);
		}
	}

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
				st = MiscUtilities.firstFromCloseableIterable(map.values());
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
					st = MiscUtilities.firstFromCloseableIterable(map.values());
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
					ctx = (Context) st;
					if (s.startsWith("$"))
						st = ctx.getStatementByHexRef(transaction, s);
					else
						st = ctx.identifierToStatement(transaction).get(Identifier.parse(s));
				}
			}
			else
				st = st.refresh(transaction);
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

	protected static Collection<Statement> findMultiStatementSpec(PersistenceManager persistenceManager, Transaction transaction, Context ctx, String spec,
			boolean localIfMulti) throws CommandParseException
	{
		try
		{
			return Collections.singleton(persistenceManager.getStatement(transaction, UUID.fromString(spec)));
		}
		catch (IllegalArgumentException e)
		{
			return findMultiStatementPath(persistenceManager, transaction, ctx, spec, localIfMulti);
		}
	}

	protected static Collection<Statement> findMultiStatementPath(PersistenceManager persistenceManager, Transaction transaction, Context ctx, String path)
			throws CommandParseException
	{
		return findMultiStatementPath(persistenceManager, transaction, ctx, path, false);
	}

	protected static Collection<Statement> findMultiStatementPath(PersistenceManager persistenceManager, Transaction transaction, Context ctx, String path,
			boolean localIfMulti) throws CommandParseException
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
				if (localIfMulti)
					return ((Context) st).localIdentifierToStatement(transaction).subMap(initiator, terminator).values();
				else
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
			return TermParser.parseTerm(transaction, ctx, new StringReader(s), parameterIdentifiers);
		}
		catch (ParserBaseException e)
		{
			throw new CommandParseTermParserException(e, s);
		}
	}

	protected static Term parseTerm(Context ctx, Transaction transaction, String s) throws CommandParseException
	{
		return parseTerm(ctx, transaction, s, null);
	}

	protected static ParameterIdentification parseParameterIdentification(String s) throws CommandParseException
	{
		try
		{
			return ParameterIdentification.parse(s);
		}
		catch (ParserBaseException e)
		{
			throw CommandParseEmbeddedException.embed(e);
		}
	}

	protected static TermParser.ParameterIdentifiedTerm parseParameterIdentifiedTerm(Context ctx, Transaction transaction, String s)
			throws CommandParseException
	{
		try
		{
			return TermParser.parseParameterIdentifiedTerm(transaction, ctx, new StringReader(s));
		}
		catch (ParserBaseException e)
		{
			throw new CommandParseTermParserException(e, s);
		}
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

		public MissingParametersException(int needed, int obtained)
		{
			super("Missing " + (needed - obtained) + " parameters");
		}

	}

	protected void checkMinParameters(List<String> split) throws MissingParametersException
	{
		if (split.size() < minParameters())
			throw new MissingParametersException(minParameters(), split.size());
	}

	public abstract C parse(CommandSource from, Transaction transaction, E extra, List<String> split) throws CommandParseException;

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
		return specToStatementAuthoritySignatures(persistenceManager, transaction, activeContext, split, false);
	}

	protected Collection<StatementAuthoritySignature> specToStatementAuthoritySignatures(PersistenceManager persistenceManager, Transaction transaction,
			Context activeContext, List<String> split, boolean localIfMulti) throws CommandParseException
	{
		Collection<Statement> statements = findMultiStatementSpec(persistenceManager, transaction, activeContext, split.get(0), localIfMulti);
		if (statements == null || statements.isEmpty())
			throw new CommandParseException("Invalid statement");
		Collection<StatementAuthoritySignature> signatures = new ArrayList<>();
		for (Statement statement : statements)
		{
			StatementAuthority statementAuthority = statement.getAuthority(transaction);
			if (statementAuthority == null)
				continue;
			StatementAuthoritySignatureMap signatureMap = statementAuthority.signatureMap(transaction);
			if (split.size() <= 1)
				signatures.addAll(signatureMap.values());
			else if (split.size() <= 2)
			{
				Signatory authorizer = persistenceManager.getSignatory(transaction, UUID.fromString(split.get(1)));
				StatementAuthoritySignature signature = signatureMap.get(authorizer);
				if (signature == null)
					continue;
				signatures.add(signature);
			}
			else if (split.size() > 3)
			{
				Statement ctxSt = findStatementSpec(persistenceManager, transaction, activeContext, split.get(1));
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
				signatures.add(signature);
			}
			else
				throw new MissingParametersException(3, split.size());
		}
		return signatures;
	}

	public static class CompletionSet implements Iterable<CompletionSet.Completion>
	{
		private final String queried;

		public static class Completion
		{
			private final String contents;
			private final String post;

			public Completion(String contents, String post)
			{
				super();
				this.contents = contents;
				this.post = post;
			}

			public String getContents()
			{
				return contents;
			}

			public String getPost()
			{
				return post;
			}
		}

		private final Collection<Completion> completions;

		public CompletionSet(String queried, Collection<Completion> completions)
		{
			super();
			this.queried = queried;
			this.completions = completions;
		}

		public CompletionSet(String queried, Collection<String> completionsContent, String post)
		{
			this(queried, new BijectionCollection<>(new Bijection<String, Completion>()
			{

				@Override
				public Completion forward(String contents)
				{
					return new Completion(contents, post);
				}
			}, completionsContent));
		}

		public String getQueried()
		{
			return queried;
		}

		public Collection<Completion> getCompletions()
		{
			return Collections.unmodifiableCollection(completions);
		}

		public int size()
		{
			return completions.size();
		}

		@Override
		public Iterator<Completion> iterator()
		{
			return completions.iterator();
		}

		public Completion first()
		{
			return MiscUtilities.firstFromIterable(this);
		}

		public String commonPrefix()
		{
			return MiscUtilities.commonPrefix(new BijectionCollection<>(new Bijection<Completion, String>()
			{

				@Override
				public String forward(Completion completion)
				{
					return completion.getContents();
				}
			}, completions));
		}

	}

	public CompletionSet completionSet(CommandSource from, List<String> split)
	{
		return identifierCompletionSet(from, split);
	}

	public CompletionSet identifierCompletionSet(CommandSource from, List<String> split)
	{
		String last = MiscUtilities.lastFromList(split);
		if (last == null)
			return null;
		Pattern pattern = Pattern.compile("/??(([a-zA-Z0-9_]+\\.)*[a-zA-Z0-9_]+/)*([a-zA-Z0-9_]+\\.)*[a-zA-Z0-9_]*$");
		Matcher matcher = pattern.matcher(last);
		if (!matcher.find())
			return null;
		String fullPath = matcher.group();
		ArrayList<String> splittedPath = new ArrayList<>(Arrays.asList(fullPath.split("/")));
		if (fullPath.endsWith("/"))
			splittedPath.add("");
		try (Transaction transaction = from.getPersistenceManager().beginDirtyTransaction())
		{
			Context context = from.getActiveContext();
			for (int i = 0; i < splittedPath.size() - 1; i++)
			{
				if (i == 0 && splittedPath.get(i).isEmpty())
					context = null;
				else
				{
					try
					{
						if (context == null)
						{
							GenericRootContextsMap grcm = from.getPersistenceManager().identifierToRootContexts(transaction)
									.get(Identifier.parse(splittedPath.get(i)));
							if (grcm == null)
								return null;
							context = MiscUtilities.firstFromCloseableIterable(grcm.values());
						}
						else
							context = (Context) context.identifierToStatement(transaction).get(Identifier.parse(splittedPath.get(i)));
						if (context == null)
							return null;
					}
					catch (InvalidNameException | ClassCastException e)
					{
						return null;
					}
				}
			}
			String fullName = splittedPath.get(splittedPath.size() - 1);
			Pattern namePattern = Pattern.compile("(([a-zA-Z0-9_]+\\.)*)([a-zA-Z0-9_]*)");
			Matcher nameMatcher = namePattern.matcher(fullName);
			if (!nameMatcher.matches())
				return null;
			Namespace namespace;
			try
			{
				String dirty = nameMatcher.group(1);
				if (dirty.isEmpty())
					namespace = RootNamespace.instance;
				else
					namespace = Namespace.parse(dirty.substring(0, dirty.length() - 1));
			}
			catch (InvalidNameException e)
			{
				return null;
			}
			String queried = nameMatcher.group(3);
			NamespaceExtreme initiator = namespace.initiator(queried);
			NamespaceExtreme terminator = namespace.terminator(queried);
			CloseableSortedMap<Identifier, ?> identifierMap;
			if (context == null)
				identifierMap = from.getPersistenceManager().identifierToRootContexts(transaction).subMap(initiator, terminator);
			else
				identifierMap = context.identifierToStatement(transaction).subMap(initiator, terminator);
			List<Completion> completions = new ArrayList<>();
			Identifier identifier = identifierMap.isEmpty() ? null : identifierMap.firstKey();
			while (identifier != null)
			{
				Namespace suffix = identifier.makeSuffix(namespace);
				if (suffix instanceof RootNamespace)
					break;
				else if (suffix instanceof NodeNamespace)
				{
					String name = ((NodeNamespace) suffix).headName();
					String post = "";
					try
					{
						Identifier id = new Identifier(namespace, name);
						NamespaceExtreme idT = id.terminator();
						CloseableSortedMap<Identifier, ?> identifierHeadMap = identifierMap.headMap(idT);
						if (identifierHeadMap.containsKey(id))
						{
							if (identifierHeadMap.tailMap(id.initiator()).isEmpty())
								post = " ";
						}
						else
							post = ".";
						identifierMap = identifierMap.tailMap(idT);
					}
					catch (InvalidNameException e)
					{
						return null;
					}
					completions.add(new Completion(name, post));
					identifier = identifierMap.isEmpty() ? null : identifierMap.firstKey();
				}
				else
					return null;
			}
			return new CompletionSet(queried, completions);
		}
	}

	public CompletionSet fileNameCompletionSet(CommandSource from, List<String> split)
	{
		String fullPath = MiscUtilities.lastFromList(split);
		if (fullPath == null)
			return null;
		File directory;
		String prefix;
		try
		{
			File file = new File(fullPath);
			File canonical = file.getCanonicalFile();
			if (fullPath.isEmpty() || fullPath.endsWith("/"))
			{
				directory = canonical;
				prefix = "";
			}
			else
			{
				directory = canonical.getParentFile();
				prefix = canonical.getName();
			}
		}
		catch (IOException e)
		{
			return null;
		}
		if (!directory.isDirectory())
			return null;
		File[] files = directory.listFiles((File f) -> f.getName().startsWith(prefix));
		Arrays.sort(files);
		return new CompletionSet(prefix, new BijectionCollection<>(new Bijection<File, Completion>()
		{
			@Override
			public Completion forward(File file)
			{
				return new Completion(file.getName(), file.isDirectory() ? "/" : " ");
			}
		}, Arrays.asList(files)));
	}

}
