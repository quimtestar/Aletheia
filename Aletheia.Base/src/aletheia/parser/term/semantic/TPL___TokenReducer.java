package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefList;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "TPL", right = {})
public class TPL___TokenReducer extends ProductionTokenPayloadReducer<TypedParameterRefList>
{

	@Override
	public TypedParameterRefList reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		return new TypedParameterRefList(antecedentReferenceMap(globals, antecedents));
	}

}