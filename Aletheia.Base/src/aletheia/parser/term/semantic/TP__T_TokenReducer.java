package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "TP", right =
{ "T" })
public class TP__T_TokenReducer extends ProductionTokenPayloadReducer<TypedParameterRef>
{

	@Override
	public TypedParameterRef reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term type = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		ParameterVariableTerm parameter = new ParameterVariableTerm(type);
		return new TypedParameterRef(null, parameter);
	}

}