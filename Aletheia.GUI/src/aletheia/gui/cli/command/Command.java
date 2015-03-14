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
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.gui.cli.CliJPanel;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.TTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.TermParserException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.CombinedMap;

public abstract class Command
{
	private final CliJPanel from;
	private final PrintStream out;
	private final PrintStream outB;
	private final PrintStream err;
	private final PrintStream errB;

	protected Command(CliJPanel from)
	{
		this.from = from;
		this.out = from.out();
		this.outB = from.outB();
		this.err = from.err();
		this.errB = from.errB();
	}

	public CliJPanel getFrom()
	{
		return from;
	}

	protected PrintStream getOut()
	{
		return out;
	}

	protected PrintStream getOutB()
	{
		return outB;
	}

	public PrintStream getErr()
	{
		return err;
	}

	protected PrintStream getErrB()
	{
		return errB;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return from.getPersistenceManager();
	}

	protected TaggedCommand taggedCommand()
	{
		Class<?> c = getClass();
		while (Command.class.isAssignableFrom(c))
		{
			TaggedCommand tc = c.getAnnotation(TaggedCommand.class);
			if (tc != null)
				return tc;
			c = c.getSuperclass();
		}
		return null;
	}

	public String getTag()
	{
		TaggedCommand tc = taggedCommand();
		if (tc == null)
			return null;
		return tc.tag();
	}

	public static class CommandException extends Exception
	{
		private static final long serialVersionUID = 1547556668766059807L;

		protected CommandException()
		{
			super();
		}

		protected CommandException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected CommandException(String message)
		{
			super(message);
		}

		protected CommandException(Throwable cause)
		{
			super(cause);
		}
	}

	public static class NotActiveContextException extends CommandException
	{
		private static final long serialVersionUID = -4414068899185688398L;

		public NotActiveContextException()
		{
			super("There is no active context");
		}

	}

	protected static String termToString(Context ctx, Transaction transaction, Term term)
	{
		return termToString(ctx, transaction, term, null);
	}

	protected static String termToString(Context ctx, Transaction transaction, Term term, List<Assumption> assumptions)
	{
		Map<VariableTerm, Identifier> baseMap;
		if (ctx == null)
			baseMap = Collections.emptyMap();
		else
			baseMap = new AdaptedMap<VariableTerm, Identifier>(ctx.variableToIdentifier(transaction));
		Map<VariableTerm, Identifier> termMap = new HashMap<VariableTerm, Identifier>();
		CombinedMap<VariableTerm, Identifier> combinedMap = new CombinedMap<VariableTerm, Identifier>(termMap, baseMap);
		return termToString(term, combinedMap, termMap, new TermToStringData(), assumptions == null ? null : new ArrayDeque<Assumption>(assumptions));
	}

	private static class TermToStringData
	{
		int n = 0;
	}

	private static String termToString(Term term, CombinedMap<VariableTerm, Identifier> combinedMap, Map<VariableTerm, Identifier> termMap,
			TermToStringData termToStringData)
	{
		return termToString(term, combinedMap, termMap, termToStringData, null);
	}

	private static String termToString(Term term, CombinedMap<VariableTerm, Identifier> combinedMap, Map<VariableTerm, Identifier> termMap,
			TermToStringData termToStringData, Deque<Assumption> assumptions)
	{
		if (term instanceof SimpleTerm)
		{
			if (term instanceof CompositionTerm)
			{
				CompositionTerm comp = (CompositionTerm) term;
				String sHead = termToString(comp.getHead(), combinedMap, termMap, termToStringData);
				String sTail = termToString(comp.getTail(), combinedMap, termMap, termToStringData);
				if (comp.getTail() instanceof CompositionTerm)
					return sHead + " (" + sTail + ")";
				else
					return sHead + " " + sTail;
			}
			else if (term instanceof VariableTerm)
			{
				Identifier id = combinedMap.get(term);
				if (id == null)
					return term.toString();
				return combinedMap.get(term).toString();
			}
			else if (term instanceof TTerm)
			{
				return "T";
			}
			else if (term instanceof ProjectionTerm)
			{
				ProjectionTerm proj = (ProjectionTerm) term;
				String sfun = termToString(proj.getFunction(), combinedMap, termMap, termToStringData);
				return sfun + "* ";
			}
			else
				throw new Error();
		}
		else if (term instanceof FunctionTerm)
		{
			FunctionTerm func = (FunctionTerm) term;
			String sType = termToString(func.getParameter().getType(), combinedMap, termMap, termToStringData);
			Identifier newId;
			try
			{
				Assumption a = null;
				if (assumptions != null)
				{
					if (!assumptions.isEmpty())
						a = assumptions.pollFirst();
					else
						assumptions = null;
				}
				if (func.getBody().freeVariables().contains(func.getParameter()))
				{
					/*
					 * TODO: This can lead to name collisions and a string that does not parse back to the original term.
					 * I'll leave it this way for now because this routine is not used in any critical task and
					 * can't think of any simple and cheap solution.
					 */
					if (a != null && a.getIdentifier() != null)
						newId = new Identifier(a.getIdentifier().getName());
					else
						newId = new Identifier(String.format("v%03d", termToStringData.n++));
				}
				else
					newId = new Identifier("_");
			}
			catch (InvalidNameException e)
			{
				throw new Error();
			}
			Identifier oldId = termMap.put(func.getParameter(), newId);
			String sBody = termToString(func.getBody(), combinedMap, termMap, termToStringData, assumptions);
			if (oldId == null)
				termMap.remove(func.getParameter());
			else
				termMap.put(func.getParameter(), oldId);
			return "<" + newId.toString() + ":" + sType + " -> " + sBody + ">";
		}
		else
			throw new Error();
	}

	public static Command parse(CliJPanel cliJPanel, Transaction transaction, String command) throws CommandParseException
	{
		return factory.parse(cliJPanel, transaction, command);
	}

	public abstract void run() throws Exception;

	public void cancel(String cause)
	{

	}

	public static class CancelledCommandException extends CommandException
	{
		private static final long serialVersionUID = -4773388121782670211L;

		private CancelledCommandException(String message)
		{
			super(message);
		}

		private CancelledCommandException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	private CancelledCommandException makeCancelledCommandException(String message, Throwable cause)
	{
		String message_ = "Cancelled command";
		String tag = getTag();
		if ((tag != null) && !tag.isEmpty())
			message_ += " '" + tag + "'";
		if ((message != null) && !message.isEmpty())
			message_ += " " + message;
		if (cause == null)
			return new CancelledCommandException(message_);
		else
			return new CancelledCommandException(message_, cause);
	}

	protected CancelledCommandException makeCancelledCommandException(String message)
	{
		return makeCancelledCommandException(message, null);
	}

	protected CancelledCommandException makeCancelledCommandException(Throwable cause)
	{
		return makeCancelledCommandException(cause.getMessage(), cause);
	}

	protected CancelledCommandException makeCancelledCommandException()
	{
		return makeCancelledCommandException((String) null);
	}

	public static class CommandParseException extends CommandException
	{
		private static final long serialVersionUID = -8171574313255080036L;

		protected CommandParseException()
		{
			super();
		}

		protected CommandParseException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public CommandParseException(String message)
		{
			super(message);
		}

		public CommandParseException(Throwable cause)
		{
			super(cause);
		}

	}

	public static class CommandParseEmbeddedException extends CommandParseException
	{
		private static final long serialVersionUID = 382503306349310017L;

		protected CommandParseEmbeddedException(Throwable cause)
		{
			super(cause.getMessage(), cause);
		}

		public static CommandParseException embed(Exception e)
		{
			if (e instanceof CommandParseException)
				return (CommandParseException) e;
			else if (e instanceof TermParserException)
				return new CommandParseTermParserException((TermParserException) e);
			else if (e instanceof InvalidNameException)
				return new CommandParseInvalidNameException((InvalidNameException) e);
			else
				return new CommandParseEmbeddedException(e);
		}
	}

	public static class CommandParseTermParserException extends CommandParseEmbeddedException
	{
		private static final long serialVersionUID = -4893733325185413824L;

		public CommandParseTermParserException(TermParserException cause)
		{
			super(cause);
		}

		@Override
		public TermParserException getCause()
		{
			return (TermParserException) super.getCause();
		}

	}

	public static class CommandParseInvalidNameException extends CommandParseEmbeddedException
	{
		private static final long serialVersionUID = -6171007475638315726L;

		public CommandParseInvalidNameException(InvalidNameException cause)
		{
			super(cause);
		}

		@Override
		public InvalidNameException getCause()
		{
			return (InvalidNameException) super.getCause();
		}

	}

	protected final static GlobalCommandFactory factory = GlobalCommandFactory.instance;

}
