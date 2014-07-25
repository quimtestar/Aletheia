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
package aletheia.gui.cli.command.peertopeer;

import java.util.List;
import java.util.UUID;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.statement.RootContext;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

@TaggedCommand(tag = "orc", groupPath = "/p2p", factory = ObtainRootContext.Factory.class)
public class ObtainRootContext extends PeerToPeerCommand
{
	private final UUID uuid;

	private class MyListenableAborter extends ListenableAborter
	{
		private boolean aborted = false;
		private String cause = null;

		public synchronized void abort(String cause)
		{
			this.aborted = true;
			this.cause = cause;
			super.abort();
		}

		@Override
		public synchronized void checkAbort() throws AbortException
		{
			if (aborted)
				throw new AbortException(cause);
		}

	}

	private final MyListenableAborter myListenableAborter;

	public ObtainRootContext(CliJPanel from, UUID uuid)
	{
		super(from);
		this.uuid = uuid;
		this.myListenableAborter = new MyListenableAborter();
	}

	@Override
	public void run() throws Exception
	{
		try
		{
			RootContext rootCtx = getPeerToPeerNode().obtainRootContext(uuid, myListenableAborter);
			if (rootCtx == null)
				getErr().println("Not found.");
			else
				getFrom().getAletheiaJPanel().getContextJTree().pushSelectStatement(rootCtx);
		}
		catch (AbortException e)
		{
			throw makeCancelledCommandException(e);
		}

	}

	@Override
	public void cancel(String cause)
	{
		myListenableAborter.abort(cause);
	}

	public static class Factory extends AbstractVoidCommandFactory<ObtainRootContext>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public ObtainRootContext parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			UUID uuid = UUID.fromString(split.get(0));
			return new ObtainRootContext(cliJPanel, uuid);
		}

		@Override
		protected String paramSpec()
		{
			return "<UUID>";
		}

		@Override
		public String shortHelp()
		{
			return "Obtain a root context from the network.";
		}
	}

}
