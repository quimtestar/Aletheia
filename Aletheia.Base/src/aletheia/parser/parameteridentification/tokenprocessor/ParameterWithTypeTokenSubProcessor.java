package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithType;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class ParameterWithTypeTokenSubProcessor extends TokenSubProcessor<ParameterWithType, Void>
{

	protected ParameterWithTypeTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterWithType subProcess(NonTerminalToken token, String input, Void parameter) throws AletheiaParserException
	{
		return subProcess(token, input);
	}

	protected abstract ParameterWithType subProcess(NonTerminalToken token, String input) throws AletheiaParserException;

}
