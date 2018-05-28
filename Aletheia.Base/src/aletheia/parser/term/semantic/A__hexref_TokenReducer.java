package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "hexref" })
public class A__hexref_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		String hexRef = TaggedTerminalToken.getTextFromTokenList(reducees, 0);
		Statement statement = globals.getContext().getStatementByHexRef(globals.getTransaction(), hexRef, 5000);
		if (statement == null)
			throw new SemanticException(reducees.get(0), "Reference not found on context");
		return statement.getVariable();
	}

}