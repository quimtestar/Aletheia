/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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
package aletheia.test.authority;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.test.TransactionalBerkeleyDBPersistenceManagerTest;

/*
 * WARNING: Might leave the database inconsistent if interrupted (signedDependencies and signedProof flags).
 * If thus happen, a full execution should fix it.
 */
public class AuthorityTest0002 extends TransactionalBerkeleyDBPersistenceManagerTest
{

	public AuthorityTest0002()
	{
		super();
		setReadOnly(false);
	}

	@Override
	protected void run(BerkeleyDBPersistenceManager persistenceManager, Transaction transaction) throws Exception
	{
		for (StatementAuthority stAuth : persistenceManager.statementAuthoritySet(transaction))
		{
			try (Transaction transaction1 = persistenceManager.beginTransaction())
			{
				System.out.println(" -> " + stAuth.getStatementUuid());
				for (StatementAuthoritySignature signature : stAuth.signatureMap(transaction1).values())
					persistenceManager.deleteStatementAuthoritySignature(transaction1, signature);
				stAuth.getEntity().setValidSignature(false);
				stAuth.getEntity().setSignedDependencies(false);
				stAuth.getEntity().setSignedProof(false);
				persistenceManager.putStatementAuthority(transaction1, stAuth);
				stAuth.deleteDelegateTree(transaction1);
				transaction1.commit();
			}
		}

		StatementAuthority.checkSignedDependencies(transaction, persistenceManager.statementAuthoritySet(transaction));
		StatementAuthority.checkSignedProof(transaction, persistenceManager.statementAuthoritySet(transaction));

		System.out.println("done!");
	}

}
