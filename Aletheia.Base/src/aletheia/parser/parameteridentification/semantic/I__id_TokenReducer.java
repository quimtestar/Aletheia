package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "I", right =
{ "id" })
public class I__id_TokenReducer extends ProductionTokenPayloadReducer<Identifier>
{

	@Override
	public Identifier reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		String name = TaggedTerminalToken.getTextFromTokenList(reducees, 0);
		try
		{
			return new Identifier(name);
		}
		catch (InvalidNameException e)
		{
			throw new SemanticException(reducees.get(0), e);
		}
	}

}