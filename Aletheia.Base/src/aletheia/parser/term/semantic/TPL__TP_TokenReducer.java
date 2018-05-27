package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefList;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "TPL", right =
{ "TP" })
public class TPL__TP_TokenReducer extends ProductionTokenPayloadReducer<TypedParameterRefList>
{

	@Override
	public TypedParameterRefList reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		TypedParameterRefList typedParameterRefList = new TypedParameterRefList(antecedentReferenceMap(globals, antecedents));
		TypedParameterRef typedParameterRef = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		typedParameterRefList.addTypedParameterRef(typedParameterRef);
		return typedParameterRefList;
	}

}