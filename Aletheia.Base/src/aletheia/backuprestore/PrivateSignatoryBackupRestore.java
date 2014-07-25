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
package aletheia.backuprestore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import aletheia.model.authority.Person;
import aletheia.model.authority.Person.PersonCreationException;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.PrivateSignatory;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.VersionProtocol;
import aletheia.protocol.authority.PrivateSignatoryProtocol;
import aletheia.protocol.collection.AbstractCollectionProtocol;
import aletheia.security.utilities.PassphraseEncryptedStreamer;
import aletheia.security.utilities.PassphraseEncryptedStreamer.BadPassphraseException;
import aletheia.security.utilities.PassphraseEncryptedStreamer.PassphraseEncryptedStreamerException;
import aletheia.utilities.collections.EmptyIgnoringCollection;

public class PrivateSignatoryBackupRestore
{
	private static final int backupVersion = 0;

	private final PersistenceManager persistenceManager;
	private final Transaction transaction;

	@ProtocolInfo(availableVersions = 0)
	private class PrivateSignatoryCollectionProtocol extends AbstractCollectionProtocol<PrivateSignatory, Collection<PrivateSignatory>>
	{
		public PrivateSignatoryCollectionProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
		{
			super(0, new PrivateSignatoryProtocol(0, persistenceManager, transaction));
			checkVersionAvailability(PrivateSignatoryCollectionProtocol.class, requiredVersion);
		}

		@Override
		protected Collection<PrivateSignatory> makeCollection(int n)
		{
			return new EmptyIgnoringCollection<PrivateSignatory>()
			{

				@Override
				public boolean add(PrivateSignatory s)
				{
					Person person = getPersistenceManager().getPerson(getTransaction(), s.getUuid());
					if (person != null && !(person instanceof PrivatePerson))
					{
						try
						{
							PrivatePerson.fromPerson(getPersistenceManager(), getTransaction(), person);

						}
						catch (PersonCreationException e)
						{
							throw new Error(e);
						}
					}
					return super.add(s);
				}

			};
		}

	}

	public PrivateSignatoryBackupRestore(PersistenceManager persistenceManager, Transaction transaction)
	{
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
	}

	private PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	private Transaction getTransaction()
	{
		return transaction;
	}

	public void backup(char[] passphrase, OutputStream out) throws IOException
	{
		VersionProtocol versionProtocol = new VersionProtocol();
		versionProtocol.send(new DataOutputStream(out), backupVersion);
		try
		{
			PassphraseEncryptedStreamer passphraseEncryptedStreamer = new PassphraseEncryptedStreamer(0);
			DataOutputStream dos = new DataOutputStream(passphraseEncryptedStreamer.outputStream(passphrase, out));
			PrivateSignatoryCollectionProtocol privateSignatoryCollectionProtocol = new PrivateSignatoryCollectionProtocol(0, persistenceManager, transaction);
			privateSignatoryCollectionProtocol.send(dos, persistenceManager.privateSignatories(transaction).values());
			dos.flush();
		}
		catch (PassphraseEncryptedStreamerException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void backup(char[] passphrase, File file) throws IOException
	{
		FileOutputStream out = new FileOutputStream(file);
		try
		{
			backup(passphrase, out);
		}
		finally
		{
			out.close();
		}
	}

	public class VersionException extends ProtocolException
	{
		private static final long serialVersionUID = -4815096882622954150L;

		protected VersionException(int version)
		{
			super("Private signatory backup version " + version + " not supported :(");
		}

	}

	public void restore(char[] passphrase, InputStream in) throws IOException, ProtocolException, BadPassphraseException
	{
		VersionProtocol versionProtocol = new VersionProtocol();
		int version = versionProtocol.recv(new DataInputStream(in));
		if (version != 0)
			throw new VersionException(version);
		PassphraseEncryptedStreamer passphraseEncryptedStreamer;
		try
		{
			passphraseEncryptedStreamer = new PassphraseEncryptedStreamer(0);
			PrivateSignatoryCollectionProtocol privateSignatoryCollectionProtocol = new PrivateSignatoryCollectionProtocol(0, persistenceManager, transaction);
			privateSignatoryCollectionProtocol.recv(new DataInputStream(passphraseEncryptedStreamer.inputStream(passphrase, in)));
		}
		catch (PassphraseEncryptedStreamerException e)
		{
			throw new ProtocolException(e);
		}
	}

	public void restore(char[] passphrase, File file) throws IOException, ProtocolException, BadPassphraseException
	{
		FileInputStream in = new FileInputStream(file);
		try
		{
			restore(passphrase, in);
		}
		finally
		{
			in.close();
		}
	}

}
