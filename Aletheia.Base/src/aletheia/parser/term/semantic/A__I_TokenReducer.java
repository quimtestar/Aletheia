package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;

@AssociatedProduction(left = "A", right =
{ "I" })
public class A__I_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		IdentifiableVariableTerm variable = context.identifierToVariable(transaction).get(identifier);
		if (variable == null)
			throw new SemanticException(reducees.get(0), "Identifier:" + "'" + identifier + "'" + " not defined");
		return variable;
		/* TODO
		VariableTerm variable = tempParameterTable.get(new IdentifierParameterRef(identifier));
		if (variable == null && context != null && transaction != null)
		{
			Statement statement = context.identifierToStatement(transaction).get(identifier);
			if (statement != null)
				variable = statement.getVariable();
		}
		if (variable == null)
			throw new TokenProcessorException("Identifier:" + "'" + identifier + "'" + " not defined", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation());
		return variable;
		*/
	}

}