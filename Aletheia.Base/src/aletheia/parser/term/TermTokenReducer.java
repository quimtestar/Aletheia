package aletheia.parser.term;

import java.util.Arrays;
import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.AssociatedProduction;
import aletheia.parsergenerator.tokens.ProductionManagedTokenReducer;
import aletheia.parsergenerator.tokens.ProductionReducer;
import aletheia.parsergenerator.tokens.ProductionReducerFactory;
import aletheia.parsergenerator.tokens.SemanticException;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class TermTokenReducer extends ProductionManagedTokenReducer<TermParserToken>
{
	@AssociatedProduction(left = "I", right =
	{ "id" })
	private static class I__id_ProductionReducer extends ProductionReducer<TermParserToken, TermTokenReducer>
	{
		public I__id_ProductionReducer(TermTokenReducer tokenReducer, Production production)
		{
			super(tokenReducer, production);
		}

		@Override
		public TermParserToken reduce(List<Token<? extends Symbol>> antecedents, List<Token<? extends Symbol>> reducees) throws SemanticException
		{
			String name = ((TaggedTerminalToken) reducees.get(0)).getText();
			try
			{
				return new IdentifierTermParserToken(getProduction(), reducees, new Identifier(name));
			}
			catch (InvalidNameException e)
			{
				throw new SemanticException(reducees.get(0).getStartLocation(), reducees.get(0).getStopLocation(), e);
			}
		}

	}

	private static ProductionReducerFactory<TermParserToken, TermTokenReducer> productionReducerFactory = new ProductionReducerFactory<>(
			Arrays.asList(I__id_ProductionReducer.class));

	public TermTokenReducer()
	{
		super(productionReducerFactory);
	}

}
