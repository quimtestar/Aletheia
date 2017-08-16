package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "F", right =
{ "openfun", "M", "arrow", "T_", "closefun" })
public class F_openfun_M_arrow_T__closefun_FunctionParameterIdentificationTokenSubProcessor extends FunctionParameterIdentificationTokenSubProcessor
{

	protected F_openfun_M_arrow_T__closefun_FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected FunctionParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		ParameterWithTypeList parameterWithTypeList = getProcessor().processParameterWithTypeList((NonTerminalToken) token.getChildren().get(1), input);
		ParameterIdentification body = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(3), input);
		return subProcess(parameterWithTypeList, body);
	}

}
