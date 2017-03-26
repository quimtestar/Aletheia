package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class BooleanTokenSubProcessor extends TokenSubProcessor<Boolean, Void>
{

	protected BooleanTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Boolean subProcess(NonTerminalToken token, String input, Void parameter) throws TermParserException
	{
		return subProcess(token, input);
	}

	protected abstract boolean subProcess(NonTerminalToken token, String input) throws TermParserException;

}
