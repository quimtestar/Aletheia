package aletheia.parser.term.semantic;

import java.util.List;
import java.util.UUID;

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
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "S_ts", right =
{ "uuid" })
public class Sts__uuid_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		ReferenceType referenceType = NonTerminalToken.findLastPayloadInList(antecedents, new TaggedNonTerminalSymbol("R_t"));
		UUID uuid = UUID.fromString(TaggedTerminalToken.getTextFromTokenList(reducees, 0));
		Statement statement = globals.getPersistenceManager().getStatement(globals.getTransaction(), uuid);
		if (statement == null)
			throw new SemanticException(reducees.get(0), "Statement not found with UUID: " + uuid);
		try
		{
			return dereferenceStatement(statement, referenceType);
		}
		catch (DereferenceStatementException e)
		{
			throw new SemanticException(reducees.get(0), e);
		}
	}

}