package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "T", right =
{ "T_" })
public class T_T__ParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected T_T__ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		return getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(0), input);
	}

}
