package aletheia.gui.cli.command;

import java.io.PrintStream;
import java.util.Collection;

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

	PrintStream getErr();

	PrintStream getErrB();

	PersistenceManager getPersistenceManager();

	void lock(Collection<? extends Transaction> owners);

	void setActiveContext(Context activeContext);

	Context getActiveContext();

	void signatureRequestJTreeSelectStatement(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement);

	void signatureRequestJTreeSelectUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest);

	PeerToPeerNode getPeerToPeerNode();

	void pushSelectStatement(Statement statement);

	void pushSelectStatement(Transaction transaction, Statement statement);

	char[] passPhrase();

	void expandAllContexts(Context context);

	void nodeStructureReset(Context context);

	void clear();

	void collapseAll(Context context);

	void expandGroup(Context context, Namespace prefix);

	void expandSubscribedContexts(Context context);

	void expandUnprovedContexts(Context context);

	void openExtraFrame(String extraTitle);

	ProofFinder getProofFinder();

	void command(Command command) throws InterruptedException;

}
