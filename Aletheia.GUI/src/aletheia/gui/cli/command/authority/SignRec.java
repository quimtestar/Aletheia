/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.PrivateSignatory;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "signrec", groupPath = "/authority", factory = SignRec.Factory.class)
public class SignRec extends Sign
{
	public SignRec(CommandSource from, Transaction transaction, Context context, ContextAuthority contextAuthority, PrivatePerson delegate)
	{
		super(from, transaction, context, contextAuthority, delegate);
	}

	@Override
	protected Context getStatement()
	{
		return (Context) super.getStatement();
	}

	protected Context getContext()
	{
		return getStatement();
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		PrivateSignatory authorizer;
		DelegateAuthorizer da = getStatementAuthority().getOrCreateDelegateAuthorizer(getTransaction(), RootNamespace.instance, getDelegate());
		Signatory authorizer_ = da.getAuthorizer(getTransaction());
		if (authorizer_ == null)
		{
			authorizer = da.createAuthorizer(getTransaction());
			da.sign(getTransaction());
			da.persistenceUpdate(getTransaction());
		}
		else
		{
			if (!(authorizer_ instanceof PrivateSignatory))
				throw new SignCommandException("Haven't got private data for this authorizer. Maybe try entering the passphrase?");
			authorizer = (PrivateSignatory) authorizer_;
		}
		for (Statement st : getContext().descendentStatements(getTransaction()))
		{
			StatementAuthority stAuth = st.getAuthority(getTransaction());
			if (stAuth != null && !stAuth.isValidSignature())
				stAuth.createSignature(getTransaction(), authorizer);
		}
		return null;
	}

	public static class Factory extends Sign.Factory
	{

		@Override
		protected SignRec makeSign(CommandSource from, Transaction transaction, Statement statement, StatementAuthority statementAuthority,
				PrivatePerson delegate) throws CommandParseException
		{
			if (!(statement instanceof Context))
				throw new CommandParseException("Not a context");
			return new SignRec(from, transaction, (Context) statement, (ContextAuthority) statementAuthority, delegate);
		}

		@Override
		public String shortHelp()
		{
			return "Sets a delegate authorizer for the root namespace in this context and signs all the statements that descend from it.";
		}

	}

}
