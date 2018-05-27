package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer.ReferenceType;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "R_t", right =
{ "ampersand", "caret" })
public class Rt__ampersand_caret_TokenReducer extends ProductionTokenPayloadReducer<ReferenceType>
{

	@Override
	public ReferenceType reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		return ReferenceType.INSTANCE;
	}

}