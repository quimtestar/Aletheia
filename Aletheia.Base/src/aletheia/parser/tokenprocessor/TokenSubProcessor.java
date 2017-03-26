package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class TokenSubProcessor<R, P>
{
	private final TokenProcessor processor;

	protected TokenSubProcessor(TokenProcessor processor)
	{
		super();
		this.processor = processor;
	}

	protected TokenProcessor getProcessor()
	{
		return processor;
	}

	protected abstract R subProcess(NonTerminalToken token, String input, P parameter) throws TermParserException;

}
