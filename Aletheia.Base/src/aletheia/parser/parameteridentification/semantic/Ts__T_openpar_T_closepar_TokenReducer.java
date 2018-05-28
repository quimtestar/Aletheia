package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.model.term.CompositionTerm.CompositionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "Ts", right =
{ "T", "openpar", "T", "closepar" })
public class Ts__T_openpar_T_closepar_TokenReducer extends ProductionTokenPayloadReducer<ParameterIdentification>
{

	@Override
	public ParameterIdentification reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		ParameterIdentification head = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		ParameterIdentification tail = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
		if (head instanceof CompositionParameterIdentification || head == null)
			return new CompositionParameterIdentification((CompositionParameterIdentification) head, tail);
		else
			return new CompositionParameterIdentification(null,
					new CompositionParameterIdentification(new CompositionParameterIdentification(null, head), tail));
	}

}