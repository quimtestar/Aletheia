package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

/**
 * F -> openfun P colon T arrow T closefun;
 */
@ProcessorProduction(left = "F", right =
{ "openfun", "P", "colon", "T", "arrow", "T", "closefun" })
public class F_openfun_P_colon_T_arrow_T_closefun_FunctionParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected F_openfun_P_colon_T_arrow_T_closefun_FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected FunctionParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		Identifier parameter = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(1), input);
		ParameterIdentification parameterType = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(3), input);
		ParameterIdentification body = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(5), input);
		return new FunctionParameterIdentification(parameter, parameterType, body);
	}

}
