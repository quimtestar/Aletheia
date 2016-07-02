/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.ephemeral.phase;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import aletheia.model.authority.Person;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.model.statement.RootContext;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.LoopSubPhase;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.base.phase.RootPhase;
import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.peertopeer.ephemeral.dialog.LoopEphemeralDialogType;
import aletheia.peertopeer.ephemeral.dialog.LoopEphemeralDialogTypeDialogActive;
import aletheia.peertopeer.ephemeral.dialog.LoopEphemeralDialogTypeDialogPassive;
import aletheia.peertopeer.ephemeral.dialog.ObtainRootContextsDialogClient;
import aletheia.peertopeer.ephemeral.dialog.ObtainRootContextsDialogServer;
import aletheia.peertopeer.ephemeral.dialog.PersonsDialogClient;
import aletheia.peertopeer.ephemeral.dialog.PersonsDialogServer;
import aletheia.peertopeer.ephemeral.dialog.SendDeferredMessageDialogClient;
import aletheia.peertopeer.ephemeral.dialog.SendDeferredMessageDialogServer;
import aletheia.peertopeer.ephemeral.dialog.SendSignatureRequestDialogClient;
import aletheia.peertopeer.ephemeral.dialog.SendSignatureRequestDialogServer;
import aletheia.peertopeer.ephemeral.dialog.TransmitDeferredMessagesDialogClient;
import aletheia.peertopeer.ephemeral.dialog.TransmitDeferredMessagesDialogServer;
import aletheia.peertopeer.ephemeral.dialog.TransmitRootContextsDialogClient;
import aletheia.peertopeer.ephemeral.dialog.TransmitRootContextsDialogServer;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public class EphemeralPhase extends SubRootPhase
{

	private class LoopEphemeralSubPhase extends LoopSubPhase<LoopEphemeralDialogType>
	{

		protected abstract class Command<C extends Command<C>> extends LoopSubPhase<LoopEphemeralDialogType>.Command<C>
		{

			public Command(LoopEphemeralDialogType loopDialogType)
			{
				super(loopDialogType);
			}
		}

		protected class ObtainRootContextsCommand extends Command<ObtainRootContextsCommand>
		{
			public class Result extends Command<ObtainRootContextsCommand>.Result
			{
				private final Map<UUID, RootContext> rootContexts;

				public Result(Map<UUID, RootContext> rootContexts)
				{
					this.rootContexts = rootContexts;
				}

				public Map<UUID, RootContext> getRootContexts()
				{
					return rootContexts;
				}

			}

			private final Collection<UUID> uuids;

			public ObtainRootContextsCommand(Collection<UUID> uuids)
			{
				super(LoopEphemeralDialogType.ObtainRootContexts);
				this.uuids = new HashSet<>(uuids);
			}

			public Collection<UUID> getUuids()
			{
				return Collections.unmodifiableCollection(uuids);
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((uuids == null) ? 0 : uuids.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				ObtainRootContextsCommand other = (ObtainRootContextsCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (uuids == null)
				{
					if (other.uuids != null)
						return false;
				}
				else if (!uuids.equals(other.uuids))
					return false;
				return true;
			}

			private LoopEphemeralSubPhase getOuterType()
			{
				return LoopEphemeralSubPhase.this;
			}

		}

		protected class TransmitRootContextsCommand extends Command<TransmitRootContextsCommand>
		{
			private final Collection<UUID> uuids;

			public TransmitRootContextsCommand(Collection<UUID> uuids)
			{
				super(LoopEphemeralDialogType.TransmitRootContexts);
				this.uuids = new HashSet<>(uuids);
			}

			public Collection<UUID> getUuids()
			{
				return Collections.unmodifiableCollection(uuids);
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((uuids == null) ? 0 : uuids.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				ObtainRootContextsCommand other = (ObtainRootContextsCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (uuids == null)
				{
					if (other.uuids != null)
						return false;
				}
				else if (!uuids.equals(other.uuids))
					return false;
				return true;
			}

			private LoopEphemeralSubPhase getOuterType()
			{
				return LoopEphemeralSubPhase.this;
			}

		}

		protected class SendSignatureRequestCommand extends Command<SendSignatureRequestCommand>
		{
			public class Result extends Command<SendSignatureRequestCommand>.Result
			{
				private final boolean received;

				public Result(boolean received)
				{
					this.received = received;
				}

				public boolean isReceived()
				{
					return received;
				}
			}

			private final SignatureRequest signatureRequest;

			public SendSignatureRequestCommand(SignatureRequest signatureRequest)
			{
				super(LoopEphemeralDialogType.SendSignatureRequest);
				this.signatureRequest = signatureRequest;
			}

			public SignatureRequest getSignatureRequest()
			{
				return signatureRequest;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((signatureRequest == null) ? 0 : signatureRequest.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				SendSignatureRequestCommand other = (SendSignatureRequestCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (signatureRequest == null)
				{
					if (other.signatureRequest != null)
						return false;
				}
				else if (!signatureRequest.equals(other.signatureRequest))
					return false;
				return true;
			}

			private LoopEphemeralSubPhase getOuterType()
			{
				return LoopEphemeralSubPhase.this;
			}
		}

		protected class SendDeferredMessageCommand extends Command<SendDeferredMessageCommand>
		{
			public class Result extends Command<SendDeferredMessageCommand>.Result
			{
				private final NodeAddress redirectAddress;

				public Result(NodeAddress redirectAddress)
				{
					this.redirectAddress = redirectAddress;
				}

				public NodeAddress getRedirectAddress()
				{
					return redirectAddress;
				}

			}

			private final UUID recipientUuid;
			private final DeferredMessageContent content;

			public SendDeferredMessageCommand(UUID recipientUuid, DeferredMessageContent content)
			{
				super(LoopEphemeralDialogType.SendDeferredMessage);
				this.recipientUuid = recipientUuid;
				this.content = content;
			}

			public UUID getRecipientUuid()
			{
				return recipientUuid;
			}

			public DeferredMessageContent getContent()
			{
				return content;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((content == null) ? 0 : content.hashCode());
				result = prime * result + ((recipientUuid == null) ? 0 : recipientUuid.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				SendDeferredMessageCommand other = (SendDeferredMessageCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (content == null)
				{
					if (other.content != null)
						return false;
				}
				else if (!content.equals(other.content))
					return false;
				if (recipientUuid == null)
				{
					if (other.recipientUuid != null)
						return false;
				}
				else if (!recipientUuid.equals(other.recipientUuid))
					return false;
				return true;
			}

			private LoopEphemeralSubPhase getOuterType()
			{
				return LoopEphemeralSubPhase.this;
			}

		}

		protected class TransmitDeferredMessagesCommand extends Command<TransmitDeferredMessagesCommand>
		{
			private final Collection<DeferredMessage> deferredMessages;

			public TransmitDeferredMessagesCommand(Collection<DeferredMessage> deferredMessages)
			{
				super(LoopEphemeralDialogType.TransmitDeferredMessages);
				this.deferredMessages = deferredMessages;
			}

			public Collection<DeferredMessage> getDeferredMessages()
			{
				return deferredMessages;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((deferredMessages == null) ? 0 : deferredMessages.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				TransmitDeferredMessagesCommand other = (TransmitDeferredMessagesCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (deferredMessages == null)
				{
					if (other.deferredMessages != null)
						return false;
				}
				else if (!deferredMessages.equals(other.deferredMessages))
					return false;
				return true;
			}

			private LoopEphemeralSubPhase getOuterType()
			{
				return LoopEphemeralSubPhase.this;
			}

		}

		protected class PersonsCommand extends Command<PersonsCommand>
		{
			private final Collection<Person> persons;

			public PersonsCommand(Collection<Person> persons)
			{
				super(LoopEphemeralDialogType.Persons);
				this.persons = persons;
			}

			public Collection<Person> getPersons()
			{
				return persons;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((persons == null) ? 0 : persons.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				PersonsCommand other = (PersonsCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (persons == null)
				{
					if (other.persons != null)
						return false;
				}
				else if (!persons.equals(other.persons))
					return false;
				return true;
			}

			private LoopEphemeralSubPhase getOuterType()
			{
				return LoopEphemeralSubPhase.this;
			}

		}

		protected class ValedictionCommand extends Command<ValedictionCommand>
		{
			public ValedictionCommand()
			{
				super(LoopEphemeralDialogType.Valediction);
			}

		}

		public LoopEphemeralSubPhase() throws IOException
		{
			super(EphemeralPhase.this, LoopEphemeralDialogTypeDialogActive.class, LoopEphemeralDialogTypeDialogPassive.class);
		}

		@Override
		protected boolean serverPhase(LoopEphemeralDialogType loopDialogType) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (loopDialogType)
			{
			case Valediction:
				valedictionDialog();
				return false;
			case ObtainRootContexts:
				obtainRootContextsDialogServer();
				return true;
			case TransmitRootContexts:
				transmitRootContextsDialogServer();
				return true;
			case SendSignatureRequest:
				sendSignatureRequestDialogServer();
				return true;
			case SendDeferredMessage:
				sendDeferredMessageDialogServer();
				return true;
			case TransmitDeferredMessages:
				transmitDeferredMessagesDialogServer();
				return true;
			case Persons:
				personsDialogServer();
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected boolean clientPhase(LoopSubPhase<LoopEphemeralDialogType>.Command<?> command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (command.getLoopDialogType())
			{
			case Valediction:
				valedictionDialog();
				return false;
			case ObtainRootContexts:
				obtainRootContextsDialogClient((ObtainRootContextsCommand) command);
				return true;
			case TransmitRootContexts:
				transmitRootContextsDialogClient((TransmitRootContextsCommand) command);
				return true;
			case SendSignatureRequest:
				sendSignatureRequestDialogClient((SendSignatureRequestCommand) command);
				return true;
			case SendDeferredMessage:
				sendDeferredMessageDialogClient((SendDeferredMessageCommand) command);
				return true;
			case TransmitDeferredMessages:
				transmitDeferredMessagesDialogClient((TransmitDeferredMessagesCommand) command);
				return true;
			case Persons:
				personsDialogClient((PersonsCommand) command);
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected ValedictionCommand makeValedictionCommand()
		{
			return new ValedictionCommand();
		}

		@SuppressWarnings("unused")
		protected <C extends Command<C>> void command(final Command<C> command, Transaction transaction)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					command(command);
				}
			});
		}

		protected <C extends Command<C>> Command<C>.Result commandResult(Command<C> command, ListenableAborter aborter)
				throws InterruptedException, CancelledCommandException, AbortException
		{
			return super.commandResult(command, aborter);
		}

		@SuppressWarnings("unused")
		protected <C extends Command<C>> Command<C>.Result commandResult(final Command<C> command, final ListenableAborter aborter, Transaction transaction)
				throws InterruptedException
		{
			class Exec
			{
				private Command<C>.Result result;
				private Exception exception;
			}

			final Exec exec = new Exec();

			synchronized (exec)
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						synchronized (exec)
						{
							try
							{
								exec.result = commandResult(command, aborter);
							}
							catch (Exception e)
							{
								exec.exception = e;
							}
							finally
							{
								exec.notifyAll();
							}
						}
					}
				});
				while (exec.result == null && exec.exception == null)
					exec.wait();
				if (exec.exception != null)
				{
					if (exec.exception instanceof InterruptedException)
						throw (InterruptedException) exec.exception;
					else if (exec.exception instanceof RuntimeException)
						throw (RuntimeException) exec.exception;
					else
						throw new RuntimeException(exec.exception);
				}
				return exec.result;
			}

		}

		public RootContext obtainRootContext(UUID uuid, ListenableAborter aborter) throws InterruptedException, CancelledCommandException, AbortException
		{
			return obtainRootContexts(Collections.singleton(uuid), aborter).get(uuid);
		}

		public RootContext obtainRootContext(UUID uuid) throws InterruptedException, CancelledCommandException
		{
			return obtainRootContexts(Collections.singleton(uuid)).get(uuid);
		}

		public Map<UUID, RootContext> obtainRootContexts(Collection<UUID> uuids, ListenableAborter aborter)
				throws InterruptedException, CancelledCommandException, AbortException
		{
			return ((ObtainRootContextsCommand.Result) commandResult(new ObtainRootContextsCommand(uuids), aborter)).getRootContexts();
		}

		public Map<UUID, RootContext> obtainRootContexts(Collection<UUID> uuids) throws InterruptedException, CancelledCommandException
		{
			return ((ObtainRootContextsCommand.Result) commandResult(new ObtainRootContextsCommand(uuids))).getRootContexts();
		}

		public void transmitRootContext(UUID uuid)
		{
			transmitRootContexts(Collections.singleton(uuid));
		}

		public void transmitRootContexts(Collection<UUID> uuids)
		{
			command(new TransmitRootContextsCommand(uuids));
		}

		public boolean sendSignatureRequest(SignatureRequest signatureRequest) throws InterruptedException, CancelledCommandException
		{
			return ((SendSignatureRequestCommand.Result) commandResult(new SendSignatureRequestCommand(signatureRequest))).isReceived();
		}

		public boolean sendSignatureRequest(SignatureRequest signatureRequest, ListenableAborter aborter)
				throws InterruptedException, CancelledCommandException, AbortException
		{
			return ((SendSignatureRequestCommand.Result) commandResult(new SendSignatureRequestCommand(signatureRequest), aborter)).isReceived();
		}

		public NodeAddress sendDeferredMessage(UUID recipientUuid, DeferredMessageContent content, ListenableAborter aborter)
				throws InterruptedException, CancelledCommandException, AbortException
		{
			return ((SendDeferredMessageCommand.Result) commandResult(new SendDeferredMessageCommand(recipientUuid, content), aborter)).getRedirectAddress();
		}

		public NodeAddress sendDeferredMessage(UUID recipientUuid, DeferredMessageContent content) throws InterruptedException, CancelledCommandException
		{
			return ((SendDeferredMessageCommand.Result) commandResult(new SendDeferredMessageCommand(recipientUuid, content))).getRedirectAddress();
		}

		public void transmitDeferredMessages(Collection<DeferredMessage> deferredMessages) throws InterruptedException, CancelledCommandException
		{
			commandResult(new TransmitDeferredMessagesCommand(deferredMessages));
		}

		public void persons(Collection<Person> persons) throws InterruptedException, CancelledCommandException
		{
			commandResult(new PersonsCommand(persons));
		}

		public void persons(Collection<Person> persons, ListenableAborter aborter) throws InterruptedException, CancelledCommandException, AbortException
		{
			commandResult(new PersonsCommand(persons), aborter);
		}

		private void obtainRootContextsDialogClient(ObtainRootContextsCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				ObtainRootContextsDialogClient obtainRootContextsDialogClient = dialog(ObtainRootContextsDialogClient.class, this, command.getUuids());
				command.setResult(command.new Result(obtainRootContextsDialogClient.getRootContexts()));
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void obtainRootContextsDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(ObtainRootContextsDialogServer.class, this);
		}

		private void transmitRootContextsDialogClient(TransmitRootContextsCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(TransmitRootContextsDialogClient.class, this, command.getUuids());
		}

		private void transmitRootContextsDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(TransmitRootContextsDialogServer.class, this);
		}

		private void sendSignatureRequestDialogClient(SendSignatureRequestCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				SendSignatureRequestDialogClient dialog = dialog(SendSignatureRequestDialogClient.class, this, command.getSignatureRequest());
				command.setResult(command.new Result(dialog.isReceived()));
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void sendSignatureRequestDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(SendSignatureRequestDialogServer.class, this);
		}

		private void sendDeferredMessageDialogClient(SendDeferredMessageCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				SendDeferredMessageDialogClient sendDeferredMessageDialogClient = dialog(SendDeferredMessageDialogClient.class, this,
						command.getRecipientUuid(), command.getContent());
				command.setResult(command.new Result(sendDeferredMessageDialogClient.getRedirectAddress()));
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void sendDeferredMessageDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(SendDeferredMessageDialogServer.class, this);
		}

		private void transmitDeferredMessagesDialogClient(TransmitDeferredMessagesCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				dialog(TransmitDeferredMessagesDialogClient.class, this, command.getDeferredMessages());
				command.setResult(command.new Result());
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void transmitDeferredMessagesDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(TransmitDeferredMessagesDialogServer.class, this);
		}

		private void personsDialogClient(PersonsCommand command) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				dialog(PersonsDialogClient.class, this, command.getPersons());
				command.setResult(command.new Result());
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void personsDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(PersonsDialogServer.class, this);
		}

	}

	private LoopEphemeralSubPhase loopEphemeralSubPhase;

	public EphemeralPhase(RootPhase rootPhase, UUID peerNodeUuid) throws IOException
	{
		super(rootPhase, peerNodeUuid);
		this.loopEphemeralSubPhase = new LoopEphemeralSubPhase();
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		loopEphemeralSubPhase.run();
	}

	public RootContext obtainRootContext(UUID uuid, ListenableAborter aborter) throws InterruptedException, CancelledCommandException, AbortException
	{
		return loopEphemeralSubPhase.obtainRootContext(uuid, aborter);
	}

	public RootContext obtainRootContext(UUID uuid) throws InterruptedException, CancelledCommandException
	{
		return loopEphemeralSubPhase.obtainRootContext(uuid);
	}

	public Map<UUID, RootContext> obtainRootContexts(Collection<UUID> uuids, ListenableAborter aborter)
			throws InterruptedException, CancelledCommandException, AbortException
	{
		return loopEphemeralSubPhase.obtainRootContexts(uuids, aborter);
	}

	public Map<UUID, RootContext> obtainRootContexts(Collection<UUID> uuids) throws InterruptedException, CancelledCommandException
	{
		return loopEphemeralSubPhase.obtainRootContexts(uuids);
	}

	public void transmitRootContext(UUID uuid)
	{
		loopEphemeralSubPhase.transmitRootContext(uuid);
	}

	public void transmitRootContexts(Collection<UUID> uuids)
	{
		loopEphemeralSubPhase.transmitRootContexts(uuids);
	}

	public boolean sendSignatureRequest(SignatureRequest signatureRequest) throws InterruptedException, CancelledCommandException
	{
		return loopEphemeralSubPhase.sendSignatureRequest(signatureRequest);
	}

	public boolean sendSignatureRequest(SignatureRequest signatureRequest, ListenableAborter aborter)
			throws InterruptedException, CancelledCommandException, AbortException
	{
		return loopEphemeralSubPhase.sendSignatureRequest(signatureRequest, aborter);
	}

	public NodeAddress sendDeferredMessage(UUID recipientUuid, DeferredMessageContent content, ListenableAborter aborter)
			throws InterruptedException, CancelledCommandException, AbortException
	{
		return loopEphemeralSubPhase.sendDeferredMessage(recipientUuid, content, aborter);
	}

	public NodeAddress sendDeferredMessage(UUID recipientUuid, DeferredMessageContent content) throws InterruptedException, CancelledCommandException
	{
		return loopEphemeralSubPhase.sendDeferredMessage(recipientUuid, content);
	}

	public void transmitDeferredMessages(Collection<DeferredMessage> deferredMessages) throws InterruptedException, CancelledCommandException
	{
		loopEphemeralSubPhase.transmitDeferredMessages(deferredMessages);
	}

	public void persons(Collection<Person> persons) throws InterruptedException, CancelledCommandException
	{
		loopEphemeralSubPhase.persons(persons);
	}

	public void persons(Collection<Person> persons, ListenableAborter aborter) throws InterruptedException, CancelledCommandException, AbortException
	{
		loopEphemeralSubPhase.persons(persons, aborter);
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		loopEphemeralSubPhase.shutdown(fast);
	}

}
