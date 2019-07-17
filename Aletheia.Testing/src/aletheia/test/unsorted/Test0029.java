package aletheia.test.unsorted;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import aletheia.gui.app.FontManager;
import aletheia.test.Test;

public class Test0029 extends Test
{

	@Override
	public void run() throws Exception
	{
		FontManager fontManager = new FontManager(20);
		JFrame frame = new JFrame("Test0029");
		StyledDocument document = new DefaultStyledDocument();
		JTextPane textPane = new JTextPane(document);
		//textPane.setText("\u2190 \u00bbpoctlla\u00ab \u2192");
		textPane.setFont(fontManager.defaultFont());
		textPane.setPreferredSize(new Dimension(800, 600));
		frame.setContentPane(textPane);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		AttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground((MutableAttributeSet) attributeSet, Color.red);
		StyleConstants.setBold((MutableAttributeSet) attributeSet, true);
		StyleConstants.setUnderline((MutableAttributeSet) attributeSet, true);

		document.insertString(0, "\u2190 \u00bbpoctlla\u00ab \u2192", attributeSet);
	}

}
