package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class ParameterRefTokenSubProcessor extends TokenSubProcessor<ParameterRef, Void>
{
	protected ParameterRefTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterRef subProcess(NonTerminalToken token, String input, Void parameter) throws TermParserException
	{
		return subProcess(token, input);
	}

	protected abstract ParameterRef subProcess(NonTerminalToken token, String input) throws TermParserException;

}
