package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.parameterRef.ParameterRef;
import aletheia.parser.term.parameterRef.TypedParameterRefWithValue;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "TP", right =
{ "P", "assignment", "T" })
public class TP__P_assignment_T_TokenReducer extends ProductionTokenPayloadReducer<TypedParameterRefWithValue>
{

	@Override
	public TypedParameterRefWithValue reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		ParameterRef parameterRef = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Term value = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		Term type = value.getType();
		if (type == null)
			throw new SemanticException(reducees.get(2), "Typeless substitution term");
		ParameterVariableTerm parameter = new ParameterVariableTerm(type);
		return new TypedParameterRefWithValue(parameterRef, parameter, value);
	}

}