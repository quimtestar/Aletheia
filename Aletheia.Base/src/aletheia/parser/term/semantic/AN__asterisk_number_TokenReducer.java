package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "AN", right =
{ "asterisk", "number" })
public class AN__asterisk_number_TokenReducer extends ProductionTokenPayloadReducer<Integer>
{

	@Override
	public Integer reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		return Integer.parseInt(TaggedTerminalToken.getTextFromTokenList(reducees, 1));
	}

}