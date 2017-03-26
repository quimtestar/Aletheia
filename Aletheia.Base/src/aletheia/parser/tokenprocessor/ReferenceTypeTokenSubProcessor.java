package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class ReferenceTypeTokenSubProcessor extends TokenSubProcessor<ReferenceType, Void>
{
	protected ReferenceTypeTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ReferenceType subProcess(NonTerminalToken token, String input, Void parameter) throws TermParserException
	{
		return subProcess(token);
	}

	protected abstract ReferenceType subProcess(NonTerminalToken token);

}
