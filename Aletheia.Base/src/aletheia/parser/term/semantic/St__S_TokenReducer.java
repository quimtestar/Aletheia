package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
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
{ "S" })
public class St__S_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		ReferenceType referenceType = NonTerminalToken.findLastPayloadInList(antecedents, new TaggedNonTerminalSymbol("R_t"));
		Statement statement = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		switch (referenceType)
		{
		case TYPE:
			return statement.getTerm();
		case INSTANCE:
		{
			if (!(statement instanceof Specialization))
				throw new SemanticException(reducees.get(0), "Cannot reference the instance of a non-specialization statement");
			return ((Specialization) statement).getInstance();
		}
		case VALUE:
		{
			if (!(statement instanceof Declaration))
				throw new SemanticException(reducees.get(0), "Cannot reference the value of a non-declaration statement");
			return ((Declaration) statement).getValue();
		}
		default:
			throw new Error();
		}
	}

}