package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.CompositionTerm.CompositionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "A", right =
{ "openpar", "T", "closepar" })
public class A_openpar_T_closepar_ParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected A_openpar_T_closepar_ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		return new CompositionParameterIdentification(null,
				getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(1), input));
	}

}
