package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "A", right =
{ "F" })
public class A_F_ParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected A_F_ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		return getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(0), input);
	}

}
