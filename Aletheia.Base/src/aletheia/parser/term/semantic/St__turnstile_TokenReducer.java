package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "S_t", right =
{ "turnstile" })
public class St__turnstile_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		ReferenceType referenceType = NonTerminalToken.findLastPayloadInList(antecedents, new TaggedNonTerminalSymbol("R_t"));

		if (globals.getContext() == null)
			throw new SemanticException(reducees, "Cannot refer to the consequent without a context");
		if (referenceType != ReferenceType.TYPE)
			throw new SemanticException(reducees, "Invalid reference type to the consequent");
		return globals.getContext().getConsequent();
	}

}