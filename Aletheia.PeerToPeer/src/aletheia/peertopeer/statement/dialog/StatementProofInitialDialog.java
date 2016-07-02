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
package aletheia.peertopeer.statement.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.statement.Subscription;
import aletheia.peertopeer.statement.message.AvailableProofsMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.BijectionCollection;

public class StatementProofInitialDialog extends InitializeStatementProofDialog
{
	public StatementProofInitialDialog(Phase phase)
	{
		super(phase);
	}

	private Collection<Statement> provedStatementsToSend()
	{
		Collection<Statement> provedStatementsToSend = new ArrayList<>();
		Stack<UUID> stack = new Stack<>();
		stack.addAll(getRemoteSubscription().rootContextUuids());
		while (!stack.isEmpty())
		{
			UUID contextUuid = stack.pop();
			Subscription.SubContextSubscription subContextSubscription = getRemoteSubscription().subContextSubscriptions().get(contextUuid);
			if (subContextSubscription != null)
			{
				for (UUID statementUuid : subContextSubscription.proofUuids())
				{
					StatementAuthority sa = getPersistenceManager().getStatementAuthority(getTransaction(), statementUuid);
					if (sa != null && sa.isSignedProof())
						provedStatementsToSend.add(sa.getStatement(getTransaction()));
				}
				stack.addAll(subContextSubscription.contextUuids());
			}
		}
		return provedStatementsToSend;
	}

	private AvailableProofsMessage dialogateAvailableProofs(Collection<Statement> provedStatementsToSend)
			throws ProtocolException, IOException, InterruptedException
	{
		sendMessage(new AvailableProofsMessage(new BijectionCollection<>(getStatementUuidBijection(), provedStatementsToSend)));
		return recvMessage(AvailableProofsMessage.class);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		Collection<Statement> provedStatementsToSend = provedStatementsToSend();
		AvailableProofsMessage availableProofsMessage = dialogateAvailableProofs(provedStatementsToSend);
		for (UUID uuid : availableProofsMessage.getUuids())
			getStatementStack().push(new StatementStackEntry(uuid));
		setSending(true);
		setReceiving(true);
	}

}
