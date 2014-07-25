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
import java.util.Collection;
import java.util.UUID;

import aletheia.model.statement.Context;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

public class StatementSubscriptionInitialDialog extends StatementSubscriptionDialog
{
	public StatementSubscriptionInitialDialog(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		Collection<Context> contexts = new BijectionCollection<>(new Bijection<UUID, Context>()
		{
			@Override
			public Context forward(UUID uuid)
			{
				return getPersistenceManager().getContext(getTransaction(), uuid);
			}

			@Override
			public UUID backward(Context output)
			{
				throw new UnsupportedOperationException();
			}
		}, getLocalSubscription().rootContextUuids());
		dialogateStatementSubscriptions(contexts);
	}
}
