package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "A", "bang", "I" })
public class A__A_bang_I_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		Statement statement = globals.getContext().identifierToStatement(globals.getTransaction()).get(identifier);
		if (statement instanceof Declaration)
		{
			Declaration declaration = (Declaration) statement;
			try
			{
				return term.replace(declaration.getVariable(), declaration.getValue());
			}
			catch (ReplaceTypeException e)
			{
				throw new SemanticException(reducees, e);
			}
		}
		else
			throw new SemanticException(reducees.get(2), "Referenced statement: '" + identifier + "' after the bang must be a declaration");
	}

}