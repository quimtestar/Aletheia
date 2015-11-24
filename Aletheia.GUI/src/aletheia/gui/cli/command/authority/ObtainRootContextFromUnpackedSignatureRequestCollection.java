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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.PackedSignatureRequest;
import aletheia.model.statement.RootContext;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.FilteredCloseableCollection;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.TrivialCloseableCollection;
import aletheia.utilities.collections.UniqueCloseableIterable;

@TaggedCommand(tag = "orcusr", groupPath = "/authority", factory = ObtainRootContextFromUnpackedSignatureRequestCollection.Factory.class)
public class ObtainRootContextFromUnpackedSignatureRequestCollection extends TransactionalCommand
{
	private final CloseableCollection<PackedSignatureRequest> packedSignatureRequestCollection;

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

	public ObtainRootContextFromUnpackedSignatureRequestCollection(CommandSource from, Transaction transaction,
			CloseableCollection<PackedSignatureRequest> packedSignatureRequestCollection)
	{
		super(from, transaction);
		this.packedSignatureRequestCollection = packedSignatureRequestCollection;
		this.myListenableAborter = new MyListenableAborter();
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		PeerToPeerNode peerToPeerNode = getPeerToPeerNode();
		CloseableIterator<UUID> iterator = new UniqueCloseableIterable<UUID>(new FilteredCloseableCollection<UUID>(new NotNullFilter<UUID>(),
				new BijectionCloseableCollection<>(new Bijection<PackedSignatureRequest, UUID>()
				{

					@Override
					public UUID forward(PackedSignatureRequest packedSignatureRequest)
					{
						return packedSignatureRequest.getRootContextSignatureUuid();
					}

					@Override
					public PackedSignatureRequest backward(UUID output)
					{
						throw new UnsupportedOperationException();
					}
				}, packedSignatureRequestCollection))).iterator();
		try
		{
			RootContext rootCtx = null;
			while (iterator.hasNext())
			{
				UUID uuid = iterator.next();
				rootCtx = peerToPeerNode.obtainRootContext(uuid, myListenableAborter);
				if (rootCtx != null)
					break;
			}
			if (rootCtx == null)
				getErr().println("Not found.");
			else
				pushSelectStatement(rootCtx);
		}
		finally
		{
			iterator.close();
		}
		return null;
	}

	@Override
	public void cancel(String cause)
	{
		super.cancel(cause);
		myListenableAborter.abort(cause);
	}

	public static class Factory extends AbstractVoidCommandFactory<ObtainRootContextFromUnpackedSignatureRequestCollection>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public ObtainRootContextFromUnpackedSignatureRequestCollection parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split)
				throws CommandParseException
		{
			checkMinParameters(split);
			UUID uuid;
			try
			{
				uuid = UUID.fromString(split.get(0));
			}
			catch (IllegalArgumentException e)
			{
				throw new CommandParseException(e);
			}
			CloseableCollection<PackedSignatureRequest> packedSignatureRequestCollection;
			PackedSignatureRequest packedSignatureRequest = cliJPanel.getPersistenceManager().getPackedSignatureRequest(transaction, uuid);
			if (packedSignatureRequest != null)
				packedSignatureRequestCollection = new TrivialCloseableCollection<>(Collections.singleton(packedSignatureRequest));
			else
				packedSignatureRequestCollection = cliJPanel.getPersistenceManager().packedSignatureRequestContextPackingDateCollection(transaction, uuid);
			return new ObtainRootContextFromUnpackedSignatureRequestCollection(cliJPanel, transaction, packedSignatureRequestCollection);
		}

		@Override
		protected String paramSpec()
		{
			return "<UUID>";
		}

		@Override
		public String shortHelp()
		{
			return "Obtains a root context for an unpacked signature request or an unpacked signature request's virtual context (the one specified in the most recently packed signature request).";
		}
	}

}
