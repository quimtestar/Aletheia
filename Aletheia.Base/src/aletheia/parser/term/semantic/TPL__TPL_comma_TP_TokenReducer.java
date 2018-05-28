package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.parameterRef.TypedParameterRef;
import aletheia.parser.term.parameterRef.TypedParameterRefList;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "TPL", right =
{ "TPL", "comma", "TP" })
public class TPL__TPL_comma_TP_TokenReducer extends ProductionTokenPayloadReducer<TypedParameterRefList>
{

	@Override
	public TypedParameterRefList reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		TypedParameterRefList typedParameterRefList = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		TypedParameterRef typedParameterRef = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		typedParameterRefList.addTypedParameterRef(typedParameterRef);
		return typedParameterRefList;
	}

}