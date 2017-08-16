package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithType;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.utilities.collections.ReverseList;

public abstract class FunctionParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	protected FunctionParameterIdentification subProcess(ParameterWithTypeList parameterWithTypeList, ParameterIdentification body)
	{
		for (ParameterWithType parameterWithType : new ReverseList<>(parameterWithTypeList))
		{
			Identifier parameter = null;
			ParameterIdentification parameterType = null;
			if (parameterWithType != null)
			{
				parameter = parameterWithType.getParameter();
				parameterType = parameterWithType.getParameterType();
				if (parameter != null || parameterType != null || body != null)
					body = new FunctionParameterIdentification(parameter, parameterType, body);
			}
		}
		return (FunctionParameterIdentification) body;

	}

	@Override
	protected abstract FunctionParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException;

}
