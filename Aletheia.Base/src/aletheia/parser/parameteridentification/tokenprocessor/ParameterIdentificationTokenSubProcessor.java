package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class ParameterIdentificationTokenSubProcessor extends TokenSubProcessor<ParameterIdentification, Void>
{

	protected ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token, String input, Void parameter) throws AletheiaParserException
	{
		return subProcess(token, input);
	}

	protected abstract ParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException;

}
