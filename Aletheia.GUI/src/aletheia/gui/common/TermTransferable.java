package aletheia.gui.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import aletheia.model.term.Term;

public class TermTransferable extends AletheiaTransferable
{
	private final Term term;

	public TermTransferable(Term term)
	{
		super(Arrays.<DataFlavor> asList(TermDataFlavor.instance, DataFlavor.stringFlavor));
		this.term = term;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(TermDataFlavor.instance))
			return term;
		else if (flavor.equals(DataFlavor.stringFlavor))
			return term.toString();
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
