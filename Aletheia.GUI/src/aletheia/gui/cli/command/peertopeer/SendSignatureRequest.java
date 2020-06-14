/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.persistence.Transaction;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

@TaggedCommand(tag = "ssr", groupPath = "/p2p", factory = SendSignatureRequest.Factory.class)
public class SendSignatureRequest extends PeerToPeerCommand
{
	private final Person person;
	private final SignatureRequest signatureRequest;

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

	public SendSignatureRequest(CommandSource from, Person person, SignatureRequest signatureRequest)
	{
		super(from);
		this.person = person;
		this.signatureRequest = signatureRequest;
		this.myListenableAborter = new MyListenableAborter();
	}

	@Override
	public void run() throws Exception
	{
		try
		{
			boolean received = getPeerToPeerNode().sendSignatureRequest(person, signatureRequest, myListenableAborter);
			if (!received)
				throw new Exception("Not received.");
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

	public static class Factory extends AbstractVoidCommandFactory<SendSignatureRequest>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public SendSignatureRequest parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Person person = specToPerson(from.getPersistenceManager(), transaction, split.get(0));
			if (person == null)
				throw new CommandParseException("Person not found.");
			UUID signatureRequestUuid;
			try
			{
				signatureRequestUuid = UUID.fromString(split.get(1));
			}
			catch (IllegalArgumentException e)
			{
				throw new CommandParseException(e);
			}
			SignatureRequest signatureRequest = from.getPersistenceManager().getSignatureRequest(transaction, signatureRequestUuid);
			if (signatureRequest == null)
				throw new CommandParseException("Request not found.");
			if (signatureRequest instanceof UnpackedSignatureRequest)
			{
				if (((UnpackedSignatureRequest) signatureRequest).rootContextSignatureUuid(transaction) == null)
					throw new CommandParseException("Missing root context signature");
			}
			return new SendSignatureRequest(from, person, signatureRequest);
		}

		@Override
		protected String paramSpec()
		{
			return "(<person UUID> | <nick>) <signature request UUID>";
		}

		@Override
		public String shortHelp()
		{
			return "Send a signature request to another person.";
		}
	}

}
