package aletheia.parser.term.semantic;

import java.util.List;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "S", right =
{ "hexref" })
public class S__hexref_TokenReducer extends ProductionTokenPayloadReducer<Statement>
{

	@Override
	public Statement reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Context context = antecedentContext(globals, antecedents);
		String hexRef = TaggedTerminalToken.getTextFromTokenList(reducees, 0);
		if (context == null)
		{
			Statement statement = globals.getPersistenceManager().getRootContextByHexRef(globals.getTransaction(), hexRef);
			if (statement == null)
				throw new SemanticException(reducees.get(0), "Reference: + " + "'" + hexRef + "'" + " not found on root level");
			return statement;
		}
		else
		{
			Statement statement = context.getStatementByHexRef(globals.getTransaction(), hexRef, 5000);
			if (statement == null)
				throw new SemanticException(reducees.get(0), "Reference: + " + "'" + hexRef + "'" + " not found in context: \"" + context.label() + "\"");
			return statement;
		}
	}

}