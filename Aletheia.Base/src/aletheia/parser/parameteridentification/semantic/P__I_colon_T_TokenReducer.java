package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parser.parameteridentification.ParameterWithType;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "P", right =
{ "I", "colon", "T" })
public class P__I_colon_T_TokenReducer extends ProductionTokenPayloadReducer<ParameterWithType>
{

	@Override
	public ParameterWithType reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		ParameterIdentification parameterType = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		return new ParameterWithType(identifier, parameterType);
	}

}