/*******************************************************************************
 * Copyright (c) 2016, 2019 Quim Testar.
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
package aletheia.gui.cli.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;

import aletheia.gui.app.AletheiaJFrame;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.prooffinder.ProofFinder;

public interface CommandSource
{

	PrintStream getOut();

	PrintStream getOutB();

	PrintStream getOutP();

	PrintStream getErr();

	PrintStream getErrB();

	PersistenceManager getPersistenceManager();

	void lock(Collection<? extends Transaction> owners);

	void setActiveContext(Context activeContext);

	Context getActiveContext();

	void signatureRequestJTreeSelectStatement(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement);

	void signatureRequestJTreeSelectUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest);

	PeerToPeerNode getPeerToPeerNode();

	void putSelectStatement(Statement statement);

	void putSelectStatement(Transaction transaction, Statement statement);

	void putSelectContextConsequent(Context context);

	void putSelectContextConsequent(Transaction transaction, Context context);

	void expandAllContexts(Context context);

	void nodeStructureReset(Context context);

	void resetGui();

	void clear();

	void collapseAll(Context context);

	void expandGroup(Context context, Namespace prefix);

	void expandSubscribedContexts(Context context);

	void expandUnprovedContexts(Context context);

	AletheiaJFrame openExtraFrame(String extraTitle);

	void setExtraTitle(String extraTitle);

	ProofFinder getProofFinder();

	void command(Command command) throws InterruptedException;

	void exit();

	char[] passphrase(boolean confirm);

	boolean confirmDialog(String text);

	void consoleFile(File file) throws FileNotFoundException;

	void restart();

}
