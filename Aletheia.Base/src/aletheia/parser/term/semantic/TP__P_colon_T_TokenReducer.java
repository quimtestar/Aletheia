package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "TP", right =
{ "P", "colon", "T" })
public class TP__P_colon_T_TokenReducer extends ProductionTokenPayloadReducer<TypedParameterRef>
{

	@Override
	public TypedParameterRef reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		ParameterRef parameterRef = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Term type = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		ParameterVariableTerm parameter = new ParameterVariableTerm(type);
		return new TypedParameterRef(parameterRef, parameter);
	}

}