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
package aletheia.peertopeer.statement;

import java.util.UUID;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;

public class StatementAuthoritySignatureAuthorizerUuidBijection implements Bijection<StatementAuthoritySignature, UUID>
{
	private final Transaction transaction;
	private final StatementAuthority statementAuthority;

	public StatementAuthoritySignatureAuthorizerUuidBijection(Transaction transaction, StatementAuthority statementAuthority)
	{
		super();
		this.transaction = transaction;
		this.statementAuthority = statementAuthority;
	}

	@Override
	public UUID forward(StatementAuthoritySignature statementAuthoritySignature)
	{
		return statementAuthoritySignature.getAuthorizerUuid();
	}

	@Override
	public StatementAuthoritySignature backward(UUID authorizerUuid)
	{
		return statementAuthority.getSignature(transaction, authorizerUuid);
	}
}
