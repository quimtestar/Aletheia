package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "MP", right =
{ "MP", "AN" })
public class MP__MP_AN_TokenReducer extends ProductionTokenPayloadReducer<Integer>
{

	@Override
	public Integer reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		int nMP = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		int nAN = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		return nMP + nAN;
	}

}