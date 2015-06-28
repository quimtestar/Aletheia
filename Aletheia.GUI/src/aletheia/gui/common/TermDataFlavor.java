package aletheia.gui.common;

import aletheia.model.term.Term;

public class TermDataFlavor extends AletheiaDataFlavor
{
	public static final TermDataFlavor instance = new TermDataFlavor();

	private TermDataFlavor()
	{
		super(Term.class);
	}

}
