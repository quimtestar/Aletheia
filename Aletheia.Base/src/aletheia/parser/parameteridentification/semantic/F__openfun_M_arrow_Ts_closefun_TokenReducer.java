package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parser.parameteridentification.ParameterWithTypeList;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "F", right =
{ "openfun", "M", "arrow", "Ts", "closefun" })
public class F__openfun_M_arrow_Ts_closefun_TokenReducer extends ProductionTokenPayloadReducer<FunctionParameterIdentification>
{
	@Override
	public FunctionParameterIdentification reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		ParameterWithTypeList list = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		ParameterIdentification body = NonTerminalToken.getPayloadFromTokenList(reducees, 3);
		return makeFunctioParameterIdentification(list, body);
	}

}