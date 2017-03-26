package aletheia.parser.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class IdentifierTokenSubProcessor extends TokenSubProcessor<Identifier, Void>
{

	protected IdentifierTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Identifier subProcess(NonTerminalToken token, String input, Void parameter) throws TermParserException
	{
		return subProcess(token, input);
	}

	protected abstract Identifier subProcess(NonTerminalToken token, String input) throws TermParserException;

}
