package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parser.parameteridentification.ParameterWithType;
import aletheia.parser.parameteridentification.ParameterWithTypeList;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.utilities.collections.ReverseList;

@AssociatedProduction(left = "F", right =
{ "openfun", "M", "closefun" })
public class F__openfun_M_closefun_TokenReducer extends ProductionTokenPayloadReducer<FunctionParameterIdentification>
{

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
	public FunctionParameterIdentification reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		ParameterWithTypeList list = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		return subProcess(list, null);
	}

}