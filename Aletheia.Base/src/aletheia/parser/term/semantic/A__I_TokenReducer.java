package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "I" })
public class A__I_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		VariableTerm variable = resolveReference(globals, antecedents, new IdentifierParameterRef(identifier));
		if (variable == null)
			throw new SemanticException(reducees.get(0), "Identifier:" + "'" + identifier + "'" + " not defined");
		return variable;
	}

}