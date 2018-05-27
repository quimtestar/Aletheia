package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.NumberedParameterRef;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "P", right =
{ "atparam" })
public class P__atparam_TokenReducer extends ProductionTokenPayloadReducer<NumberedParameterRef>
{

	@Override
	public NumberedParameterRef reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		String atParam = TaggedTerminalToken.getTextFromTokenList(reducees, 0);
		return new NumberedParameterRef(atParam);
	}

}