package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parser.parameteridentification.ParameterWithTypeList;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "M", right =
{ "M", "comma", "P" })
public class M__M_comma_P_TokenReducer extends ProductionTokenPayloadReducer<ParameterWithTypeList>
{

	@Override
	public ParameterWithTypeList reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		ParameterWithTypeList list = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		list.add(NonTerminalToken.getPayloadFromTokenList(reducees, 2));
		return list;
	}

}