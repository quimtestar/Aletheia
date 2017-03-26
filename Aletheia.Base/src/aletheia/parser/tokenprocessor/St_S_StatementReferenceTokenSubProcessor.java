package aletheia.parser.tokenprocessor;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "S_t", right =
{ "S" })
public class St_S_StatementReferenceTokenSubProcessor extends StatementReferenceTokenSubProcessor
{

	protected St_S_StatementReferenceTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	public Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction, ReferenceType referenceType)
			throws TermParserException
	{
		Statement statement = getProcessor().processStatement((NonTerminalToken) token.getChildren().get(0), input, context, transaction);
		return dereferenceStatement(statement, referenceType, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation(),
				input);
	}

}
