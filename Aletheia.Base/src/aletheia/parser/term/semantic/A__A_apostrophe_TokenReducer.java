package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "A", "apostrophe" })
public class A__A_apostrophe_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		while (term instanceof ProjectionTerm)
			term = ((ProjectionTerm) term).getFunction();
		if (term instanceof FunctionTerm)
		{
			FunctionTerm functionTerm = (FunctionTerm) term;
			Term body = functionTerm.getBody();
			if (body.isFreeVariable(functionTerm.getParameter()))
				throw new SemanticException(reducees.get(0), "Function's body depends on function parameter");
			else
				return body;
		}
		else
			throw new SemanticException(reducees.get(0), "Only can take the body of a function term");
	}

}