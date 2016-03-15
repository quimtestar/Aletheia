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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import aletheia.gui.cli.command.CommandSource;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.TermParserException;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.prooffinder.ProofFinder;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.CombinedMap;

public abstract class Command
{
	private final CommandSource from;

	protected Command(CommandSource from)
	{
		this.from = from;
	}

	public CommandSource getFrom()
	{
		return from;
	}

	protected PrintStream getOut()
	{
		return from.getOut();
	}

	protected PrintStream getOutB()
	{
		return from.getOutB();
	}

	protected PrintStream getOutP()
	{
		return from.getOutP();
	}

	protected PrintStream getErr()
	{
		return from.getErr();
	}

	protected PrintStream getErrB()
	{
		return from.getErrB();
	}

	protected PersistenceManager getPersistenceManager()
	{
		return from.getPersistenceManager();
	}

	protected void lock(Transaction owner)
	{
		from.lock(Collections.singleton(owner));
	}

	protected void lock(Collection<Transaction> owners)
	{
		from.lock(owners);
	}

	protected void setActiveContext(Context activeContext)
	{
		from.setActiveContext(activeContext);
	}

	protected void signatureRequestJTreeSelectStatement(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement)
	{
		from.signatureRequestJTreeSelectStatement(unpackedSignatureRequest, statement);
	}

	protected void signatureRequestJTreeSelectUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest)
	{
		from.signatureRequestJTreeSelectUnpackedSignatureRequest(unpackedSignatureRequest);
	}

	protected class PeerToPeerNotStartedException extends CommandException
	{
		private static final long serialVersionUID = -5509028093118788832L;

		private PeerToPeerNotStartedException()
		{
			super("P2P node not started");
		}
	}

	protected PeerToPeerNode getPeerToPeerNode() throws PeerToPeerNotStartedException
	{
		PeerToPeerNode peerToPeerNode = from.getPeerToPeerNode();
		if (peerToPeerNode == null)
			throw new PeerToPeerNotStartedException();
		return peerToPeerNode;
	}

	protected void pushSelectStatement(Statement statement)
	{
		from.pushSelectStatement(statement);
	}

	protected void pushSelectStatement(Transaction transaction, Statement statement)
	{
		from.pushSelectStatement(transaction, statement);
	}

	protected Context getActiveContext()
	{
		return from.getActiveContext();
	}

	protected char[] passPhrase()
	{
		return from.passPhrase();
	}

	protected void expandAllContexts(Context context)
	{
		from.expandAllContexts(context);
	}

	protected void nodeStructureReset(Context context)
	{
		from.nodeStructureReset(context);
	}

	protected void clear()
	{
		from.clear();
	}

	protected void exit()
	{
		from.exit();
	}

	protected void collapseAll(Context context)
	{
		from.collapseAll(context);
	}

	protected void expandGroup(Context context, Namespace prefix)
	{
		from.expandGroup(context, prefix);
	}

	protected void expandSubscribedContexts(Context context)
	{
		from.expandSubscribedContexts(context);
	}

	protected void expandUnprovedContexts(Context context)
	{
		from.expandUnprovedContexts(context);
	}

	protected void openExtraFrame(String extraTitle)
	{
		from.openExtraFrame(extraTitle);
	}

	protected ProofFinder getProofFinder()
	{
		return from.getProofFinder();
	}

	protected void command(Command command) throws InterruptedException
	{
		from.command(command);
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
		return term.toString(ctx != null ? ctx.variableToIdentifier(transaction) : null);
	}

	protected static String termToString(Context ctx, Transaction transaction, Term term, List<Assumption> assumptions)
	{
		Map<ParameterVariableTerm, Identifier> localVariableToIdentifier = new HashMap<ParameterVariableTerm, Identifier>();
		{
			Term body = term;
			Iterator<Assumption> assumptionIterator = assumptions.iterator();
			while (body instanceof FunctionTerm)
			{
				FunctionTerm function = (FunctionTerm) body;
				Assumption assumption = null;
				if (assumptionIterator.hasNext())
					assumption = assumptionIterator.next();
				if (function.getBody().freeVariables().contains(function.getParameter()) && assumption != null && assumption.getIdentifier() != null)
					localVariableToIdentifier.put(function.getParameter(), assumption.getIdentifier());
				body = function.getBody();
			}
		}
		if (ctx == null)
			return term.toString(localVariableToIdentifier);
		else
			return term.toString(new CombinedMap<VariableTerm, Identifier>(new AdaptedMap<VariableTerm, Identifier>(localVariableToIdentifier),
					new AdaptedMap<VariableTerm, Identifier>(ctx.variableToIdentifier(transaction))));
	}

	public static Command parse(CommandSource from, Transaction transaction, String command) throws CommandParseException
	{
		return factory.parse(from, transaction, command);
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
			super(cause.getMessage(), cause);
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
