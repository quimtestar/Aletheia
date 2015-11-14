/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.cli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SizeRequirements;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.apache.logging.log4j.Logger;

import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.catalogjtree.CatalogJTree;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.Command.CommandParseException;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.aux.EmptyCommand;
import aletheia.gui.cli.command.gui.Prompt;
import aletheia.gui.cli.command.gui.SimpleMessage;
import aletheia.gui.cli.command.gui.TraceException;
import aletheia.gui.common.NamespaceDataFlavor;
import aletheia.gui.common.PersistentJTreeLayerUI;
import aletheia.gui.common.StatementDataFlavor;
import aletheia.gui.common.TermDataFlavor;
import aletheia.gui.common.UUIDDataFlavor;
import aletheia.gui.font.FontManager;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.gui.MyJSplitPane;

public class CliJPanel extends JPanel
{
	private static final long serialVersionUID = -2211989098955644681L;
	private static final Logger logger = LoggerManager.instance.logger();
	private static final String multiLinePrompt = "\u00bb";
	private static final Pattern multiLinePattern = Pattern.compile("(\n+(" + Pattern.quote(multiLinePrompt) + ")+?\\p{Blank}*)+");

	private static class CommandHistory
	{
		private final static int sizeLimit = 1500;
		private final static int sizePurge = 1000;

		private final ArrayList<String> commandList;
		private int position;

		public CommandHistory()
		{
			commandList = new ArrayList<String>();
			position = 0;
		}

		public synchronized void addAndPosition(String command)
		{
			add(command);
			position = commandList.size();
		}

		public synchronized void add(String command)
		{
			commandList.add(command);
			if (commandList.size() >= sizeLimit)
				purge(sizePurge);
		}

		public synchronized boolean atEnd()
		{
			return position >= commandList.size();
		}

		public synchronized boolean isDecreaseable()
		{
			return position > 0;
		}

		public synchronized String decrease()
		{
			return commandList.get(--position);
		}

		public synchronized boolean isIncreseable()
		{
			return position < commandList.size() - 1;
		}

		public synchronized String increase()
		{
			if (++position >= commandList.size() - 1)
				return commandList.remove(position);
			else
				return commandList.get(position);
		}

		public synchronized String current()
		{
			return commandList.get(position);
		}

		private synchronized void purge(int size)
		{
			int k = commandList.size() - size;
			if (k >= 0)
			{
				commandList.subList(0, k).clear();
				position -= k;
			}
		}

	}

	private class MyKeyListener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_ENTER:
			{
				if (!e.isShiftDown())
					moveCaretToEnd();
				break;
			}
			case KeyEvent.VK_ESCAPE:
			{
				try
				{
					escape();
				}
				catch (InterruptedException e1)
				{
					logger.error(e1.getMessage(), e1);
				}
				updateMinimalCaretPosition();
				break;
			}
			case KeyEvent.VK_BACK_SPACE:
			{
				if (textPane.getCaretPosition() <= minimalCaretPosition)
					e.consume();
				break;
			}
			case KeyEvent.VK_UP:
			{
				if (commandHistory.isDecreaseable())
				{
					try
					{
						String s = getCommandMultilineFiltered();
						if (commandHistory.atEnd() || !s.equals(commandHistory.current()))
							commandHistory.add(s);
						document.remove(minimalCaretPosition, document.getLength() - minimalCaretPosition);
						document.insertString(minimalCaretPosition, commandHistory.decrease(), defaultAttributeSet);
						moveCaretToEnd();
					}
					catch (BadLocationException ex)
					{
						throw new Error(ex);
					}
				}
				e.consume();
				break;
			}
			case KeyEvent.VK_DOWN:
			{
				if (commandHistory.isIncreseable())
				{
					try
					{
						String s = getCommandMultilineFiltered();
						if (commandHistory.atEnd() || !s.equals(commandHistory.current()))
							commandHistory.add(s);
						document.remove(minimalCaretPosition, document.getLength() - minimalCaretPosition);
						document.insertString(minimalCaretPosition, commandHistory.increase(), defaultAttributeSet);
						moveCaretToEnd();
					}
					catch (BadLocationException ex)
					{
						throw new Error(ex);
					}
				}
				e.consume();
				break;
			}
			case KeyEvent.VK_HOME:
			{
				int se = textPane.getSelectionEnd();
				moveCaretToMinimal();
				if (e.isShiftDown())
				{
					textPane.setSelectionStart(textPane.getCaretPosition());
					textPane.setSelectionEnd(se);
				}
				e.consume();
				break;
			}
			case KeyEvent.VK_END:
			{
				int ss = textPane.getSelectionStart();
				moveCaretToEnd();
				if (e.isShiftDown())
				{
					textPane.setSelectionStart(ss);
					textPane.setSelectionEnd(textPane.getCaretPosition());
				}
				e.consume();
				break;
			}
			case KeyEvent.VK_F2:
			case KeyEvent.VK_F3:
			{
				getAletheiaJPanel().getContextJTree().dispatchEvent(e);
				break;
			}
			case KeyEvent.VK_LEFT:
			{
				if (skipMultilinePromptCaretPosition(textPane.getCaretPosition() - 1, true))
					e.consume();
				break;
			}
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			if (e.getKeyChar() == '\n')
			{
				bracketHighLightManager.clearHighLights();
				try
				{
					if (!e.isShiftDown())
					{
						String s = getCommandMultilineFiltered();
						command(s);
						updateMinimalCaretPosition();
					}
					else
					{
						try
						{
							document.insertString(textPane.getCaretPosition(), "\n" + multiLinePrompt + " ", multilinePromptAttributeSet);
							document.setCharacterAttributes(textPane.getCaretPosition() - 1, 1, defaultAttributeSet, true);
							textPane.setCharacterAttributes(defaultAttributeSet, true);
						}
						catch (BadLocationException e1)
						{
							throw new Error(e1);
						}
					}
				}
				catch (InterruptedException e1)
				{
					logger.error(e1.getMessage(), e1);
				}

			}
			else if (e.getKeyChar() == KeyEvent.VK_CANCEL)
			{
				controller.cancelActiveCommand("by user");
			}
		}

	}

	private boolean skipMultilinePromptCaretPosition(int position, boolean before)
	{
		try
		{
			String s = document.getText(position - multiLinePrompt.length() - 1, multiLinePrompt.length() + 1);
			int i = s.indexOf('\n');
			if (i >= 0)
			{
				int pos = before ? position - multiLinePrompt.length() - 1 + i : position + i + 1;
				if (pos <= textPane.getText().length())
				{
					textPane.setCaretPosition(pos);
					return true;
				}
			}
		}
		catch (BadLocationException e1)
		{

		}
		return false;
	}

	private class MyCaretListener implements CaretListener
	{

		@Override
		public void caretUpdate(CaretEvent e)
		{
			if (e.getDot() < minimalCaretPosition)
				if (minimalCaretPosition <= document.getLength())
					textPane.setCaretPosition(minimalCaretPosition);
			skipMultilinePromptCaretPosition(e.getDot(), false);

			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					bracketHighLightManager.update();
				}

			});
		}

	}

	private class MyDocumentListener implements DocumentListener
	{

		@Override
		public void changedUpdate(DocumentEvent e)
		{
		}

		@Override
		public void insertUpdate(DocumentEvent e)
		{
			bracketHighLightManager.textInserted(e.getOffset(), e.getLength());
		}

		@Override
		public void removeUpdate(DocumentEvent e)
		{
			bracketHighLightManager.textRemoved(e.getOffset(), e.getLength());
		}

	}

	private class MyDocumentFilter extends DocumentFilter
	{

		private class OffsetLength
		{
			private final int offset;
			private final int length;

			private OffsetLength(int offset, int length)
			{
				super();
				this.offset = offset;
				this.length = length;
			}
		}

		private OffsetLength offsetLength(int offset, int length)
		{
			try
			{
				if ((length > 0) && (offset - multiLinePrompt.length() - 1 >= 0))
				{
					String s = document.getText(offset - multiLinePrompt.length() - 1, multiLinePrompt.length() + 2);
					int i = s.indexOf('\n');
					if (i >= 0)
					{
						offset -= (multiLinePrompt.length() + 1) - i;
						length += (multiLinePrompt.length() + 1) - i;
					}
				}
				if (length > 0)
				{
					String s = document.getText(offset + length - multiLinePrompt.length() - 1, multiLinePrompt.length() + 1);
					int i = s.indexOf('\n');
					if (i >= 0)
					{
						length += i + 1;
					}
				}

			}
			catch (BadLocationException e1)
			{
			}
			return new OffsetLength(offset, length);
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
		{
			OffsetLength ol = offsetLength(offset, length);
			super.remove(fb, ol.offset, ol.length);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
		{
			// Workaround: when replacing text of a selection that should be highlighted by the BracketHighlightManager,
			// that new text gets the highlight attributes.
			if (length > 0)
				attrs = defaultAttributeSet;
			OffsetLength ol = offsetLength(offset, length);
			super.replace(fb, ol.offset, ol.length, text, attrs);
		}

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
		{
			super.insertString(fb, offset, string, attr);
		}

	}

	private class MyUndoableEditListener implements UndoableEditListener
	{

		@Override
		public void undoableEditHappened(UndoableEditEvent e)
		{
			if (textPane.getCaretPosition() < minimalCaretPosition)
				e.getEdit().undo();
		}

	}

	private class ReaderThread extends Thread
	{

		private class MultiplexedOutputStream extends FilterOutputStream
		{

			private final int channel;
			private final DataOutputStream dataOut;

			public MultiplexedOutputStream(OutputStream out, int channel)
			{
				super(out);
				this.dataOut = new DataOutputStream(out);
				this.channel = channel;
			}

			@Override
			public void write(byte[] buf, int off, int len) throws IOException
			{
				dataOut.writeInt(channel);
				dataOut.writeInt(len);
				dataOut.write(buf, off, len);

			}

			@Override
			public void write(byte[] buf) throws IOException
			{
				write(buf, 0, buf.length);
			}

			@Override
			public void write(int b) throws IOException
			{
				write(new byte[]
				{ (byte) b });
			}

		}

		private final static int bufferSize = 256;

		private final PipedInputStream inputStream;
		private final PipedOutputStream outputStream;
		private final DataInputStream dataInput;
		private final Map<AttributeSet, Integer> channelMap;
		private final List<PrintStream> channels;
		private final List<AttributeSet> attributeSets;

		public ReaderThread()
		{
			super("readerThread");
			inputStream = new PipedInputStream(bufferSize);
			try
			{
				outputStream = new PipedOutputStream(inputStream);
				dataInput = new DataInputStream(inputStream);
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
			channelMap = new HashMap<AttributeSet, Integer>();
			channels = new ArrayList<PrintStream>();
			attributeSets = new ArrayList<AttributeSet>();
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					int chan = dataInput.readInt();
					synchronized (this)
					{
						AttributeSet attributeSet = attributeSets.get(chan);
						int size = dataInput.readInt();
						byte buf[] = new byte[size];
						dataInput.readFully(buf);
						String s = new String(buf, 0, size);
						printString(s, attributeSet);
					}
				}
				catch (EOFException e)
				{
					break;
				}
				catch (IOException e)
				{
					throw new Error(e);
				}
			}
		}

		public synchronized PrintStream getOut(AttributeSet attributeSet)
		{
			Integer chan = channelMap.get(attributeSet);
			PrintStream out;
			if (chan == null)
			{
				chan = channels.size();
				out = new PrintStream(new MultiplexedOutputStream(outputStream, chan));
				channels.add(out);
				attributeSets.add(attributeSet);
				channelMap.put(attributeSet, chan);
			}
			else
				out = channels.get(chan);
			return out;
		}

		public synchronized void close() throws IOException, InterruptedException
		{
			outputStream.close();
			join();
			inputStream.close();
		}

	}

	private class ActiveContextJLabel extends JLabel
	{
		private static final long serialVersionUID = -2570231100322180370L;

		private Context context;
		private String fullText;

		private class MyComponentListener implements ComponentListener
		{

			@Override
			public void componentHidden(ComponentEvent e)
			{
			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				updateText();
			}

			@Override
			public void componentShown(ComponentEvent e)
			{
			}

		}

		public ActiveContextJLabel()
		{
			super();
			setContext(null);
			setOpaque(true);
			addComponentListener(new MyComponentListener());
		}

		@SuppressWarnings("unused")
		public Context getContext()
		{
			return context;
		}

		public void setContext(Context context)
		{
			this.context = context;
			if (context == null)
				setFullText(" ");
			else
			{
				Transaction transaction = aletheiaJPanel.getPersistenceManager().beginTransaction();
				try
				{
					if (context.persists(transaction))
						setFullText(context.statementPathString(transaction));
					else
						setFullText("???");
				}
				finally
				{
					transaction.abort();
				}
			}
		}

		@SuppressWarnings("unused")
		public String getFullText()
		{
			return fullText;
		}

		private void setFullText(String fullText)
		{
			this.fullText = fullText;
			updateText();
		}

		private void updateText()
		{
			if ((getBounds() != null) && (getGraphics() != null))
			{
				double labelWidth = getBounds().getWidth();
				int textWidth = getGraphics().getFontMetrics().stringWidth(fullText);
				if (labelWidth < textWidth)
				{
					String ellipsis = "...";
					labelWidth -= getGraphics().getFontMetrics().stringWidth(ellipsis);
					int l = (int) (fullText.length() * labelWidth / textWidth);
					if (l >= 0)
						setText(ellipsis + fullText.substring(fullText.length() - l));
					else
						setText(fullText);
				}
				else
					setText(fullText);
			}
			else
				setText(fullText);
		}

	}

	private class MyStatementStateListener implements Statement.StateListener, RootContext.TopStateListener
	{
		private final Set<Context> ancestorContexts;
		private Context context;

		public MyStatementStateListener()
		{
			super();
			this.ancestorContexts = new HashSet<Context>();
			this.context = null;
		}

		@Override
		public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
		{
		}

		@Override
		public void statementAddedToContext(Transaction transaction, Context context, Statement statement)
		{
		}

		@Override
		public void statementDeletedFromContext(Transaction transaction, final Context context, Statement statement, Identifier identifier)
		{
			if (this.context != null && (this.context.equals(statement) || ancestorContexts.contains(statement)))
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						setActiveContext(context);
					}

				});
			}

		}

		public void updateActiveContext(Transaction transaction, Context activeContext)
		{
			if (activeContext != context)
			{
				for (Context ctx : ancestorContexts)
					ctx.removeStateListener(this);
				ancestorContexts.clear();
				if (activeContext != null)
				{
					Context ctx = activeContext;
					while (!(ctx instanceof RootContext))
					{
						ctx = ctx.getContext(transaction);
						if (ctx == null)
							break;
						ancestorContexts.add(ctx);
						ctx.addStateListener(this);
					}
				}
				context = activeContext;
			}
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
		}

		@Override
		public void rootContextAdded(Transaction transaction, RootContext rootContext)
		{
		}

		@Override
		public void rootContextDeleted(Transaction transaction, RootContext rootContext, Identifier identifier)
		{
			if (this.context != null && (this.context.equals(rootContext) || ancestorContexts.contains(rootContext)))
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{

					@Override
					public void run(Transaction closedTransaction)
					{
						setActiveContext(null);
					}

				});
			}
		}

	}

	/*
	 * Custom StyledEditorKit hacked for getting letter wrap. Code copied from
	 * http://java-sl.com/tip_html_letter_wrap.html
	 */
	private class MyEditorKit extends StyledEditorKit
	{
		private static final long serialVersionUID = 8946211978267903631L;

		@Override
		public ViewFactory getViewFactory()
		{
			final ViewFactory viewFactory = super.getViewFactory();
			return new ViewFactory()
			{

				@Override
				public View create(Element elem)
				{
					View v = viewFactory.create(elem);
					if (v instanceof ParagraphView)
					{
						return new ParagraphView(elem)
						{
							@Override
							protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r)
							{
								if (r == null)
								{
									r = new SizeRequirements();
								}
								float pref = layoutPool.getPreferredSpan(axis);
								float min = layoutPool.getMinimumSpan(axis);
								// Don't include insets, Box.getXXXSpan will include them.
								r.minimum = (int) min;
								r.preferred = Math.max(r.minimum, (int) pref);
								r.maximum = Integer.MAX_VALUE;
								r.alignment = 0.5f;
								return r;
							}

						};
					}
					else
						return v;
				}
			};
		}

	}

	private class MyTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = 3956155783120096042L;

		private final TransferHandler oldTransferHandler;

		private MyTransferHandler(TransferHandler oldTransferHandler)
		{
			this.oldTransferHandler = oldTransferHandler;
		}

		private boolean importText(JComponent comp, String text)
		{
			if (text.contains(" ") || text.isEmpty())
				text = '"' + text + '"';
			return oldTransferHandler.importData(comp, new StringSelection(text));
		}

		@Override
		public boolean importData(JComponent comp, Transferable t)
		{
			if (t.isDataFlavorSupported(StatementDataFlavor.instance))
			{
				try
				{
					Statement statement = (Statement) t.getTransferData(StatementDataFlavor.instance);
					Transaction transaction = getPersistenceManager().beginTransaction(100);
					try
					{
						return importText(comp, statement.statementPathString(transaction, getActiveContext()));
					}
					finally
					{
						transaction.abort();
					}
				}
				catch (Exception e)
				{
					return false;
				}
			}
			else if (t.isDataFlavorSupported(NamespaceDataFlavor.instance))
			{
				try
				{
					Namespace namespace = (Namespace) t.getTransferData(NamespaceDataFlavor.instance);
					return importText(comp, namespace.qualifiedName());
				}
				catch (Exception e)
				{
					return false;
				}
			}
			else if (t.isDataFlavorSupported(UUIDDataFlavor.instance))
			{
				try
				{
					UUID uuid = (UUID) t.getTransferData(UUIDDataFlavor.instance);
					return importText(comp, uuid.toString());
				}
				catch (Exception e)
				{
					return false;
				}

			}
			else if (t.isDataFlavorSupported(TermDataFlavor.instance))
			{
				try
				{
					Term term = (Term) t.getTransferData(TermDataFlavor.instance);
					Transaction transaction = getPersistenceManager().beginTransaction(100);
					try
					{
						Context context = getActiveContext();
						if (context == null)
							return importText(comp, term.toString());
						else
							return importText(comp, term.toString(context.variableToIdentifier(transaction)));
					}
					finally
					{
						transaction.abort();
					}
				}
				catch (Exception e)
				{
					return false;
				}
			}
			else
				return oldTransferHandler.importData(comp, t);
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
		{
			//@formatter:off
			if (Arrays.asList(transferFlavors).contains(StatementDataFlavor.instance) ||
					Arrays.asList(transferFlavors).contains(NamespaceDataFlavor.instance) ||
					Arrays.asList(transferFlavors).contains(UUIDDataFlavor.instance) )
				return true;
			//@formatter:on
			return oldTransferHandler.canImport(comp, transferFlavors);
		}

		@Override
		public void exportAsDrag(JComponent c, InputEvent e, int action)
		{
			oldTransferHandler.exportAsDrag(c, e, action);
		}

		@Override
		public void exportToClipboard(JComponent c, Clipboard clip, int action)
		{
			oldTransferHandler.exportToClipboard(c, clip, action);
			Transferable t = clip.getContents(null);
			try
			{
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					String s = (String) t.getTransferData(DataFlavor.stringFlavor);
					StringSelection sel = new StringSelection(filterMultiline(s));
					clip.setContents(sel, sel);
				}
			}
			catch (UnsupportedFlavorException | IOException e)
			{
			}
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return oldTransferHandler.getSourceActions(c);
		}

		@Override
		public Icon getVisualRepresentation(Transferable t)
		{
			// This method is not currently (Java 1.4) used by Swing
			return oldTransferHandler.getVisualRepresentation(t);
		}

	}

	private final AletheiaJPanel aletheiaJPanel;
	private final CliController controller;
	private final DefaultStyledDocument document;
	private final JTextPane textPane;
	private final JScrollPane scrollTextPane;
	private final AttributeSet defaultAttributeSet;
	private final AttributeSet defaultBAttributeSet;
	private final AttributeSet errAttributeSet;
	private final AttributeSet errBAttributeSet;
	private final AttributeSet multilinePromptAttributeSet;
	private final ReaderThread readerThread;
	private final ActiveContextJLabel activeContextJLabel;
	private final MyStatementStateListener statementStateListener;
	private final MyJSplitPane splitPane;
	private final CatalogJTree catalogJTree;
	private final PersistentJTreeLayerUI<CatalogJTree> catalogJTreeLayerUI;
	private final JScrollPane catalogJTreeScrollPane;
	private final CommandHistory commandHistory;
	private final BracketHighLightManager bracketHighLightManager;

	private int minimalCaretPosition;
	private boolean opened;
	private Context activeContext;
	private Command promptWhenDone;

	public CliJPanel(AletheiaJPanel aletheiaJPanel, CliController controller) throws InterruptedException
	{
		super();
		this.aletheiaJPanel = aletheiaJPanel;
		this.controller = controller;
		this.activeContext = null;
		setLayout(new BorderLayout());
		document = new DefaultStyledDocument();
		document.addDocumentListener(new MyDocumentListener());
		document.setDocumentFilter(new MyDocumentFilter());
		document.addUndoableEditListener(new MyUndoableEditListener());
		document.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
		textPane = new JTextPane();
		textPane.setEditorKit(new MyEditorKit());
		textPane.setDocument(document);
		textPane.setTransferHandler(new MyTransferHandler(textPane.getTransferHandler()));
		scrollTextPane = new JScrollPane(textPane);
		catalogJTree = new CatalogJTree(this);
		catalogJTreeLayerUI = new PersistentJTreeLayerUI<CatalogJTree>(aletheiaJPanel.getAletheiaJFrame(), catalogJTree);
		catalogJTreeScrollPane = new JScrollPane(catalogJTreeLayerUI.getJLayer());
		splitPane = new MyJSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTextPane, catalogJTreeScrollPane);
		splitPane.setResizeWeight(1);
		splitPane.setDividerLocationOrExpandWhenValid(1.0d);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
		textPane.addKeyListener(new MyKeyListener());
		textPane.addCaretListener(new MyCaretListener());
		defaultAttributeSet = new SimpleAttributeSet();
		defaultBAttributeSet = new SimpleAttributeSet(defaultAttributeSet);
		StyleConstants.setBold((MutableAttributeSet) defaultBAttributeSet, true);
		errAttributeSet = new SimpleAttributeSet(defaultAttributeSet);
		StyleConstants.setForeground((MutableAttributeSet) errAttributeSet, Color.red);
		errBAttributeSet = new SimpleAttributeSet(errAttributeSet);
		StyleConstants.setBold((MutableAttributeSet) errBAttributeSet, true);
		StyleConstants.setUnderline((MutableAttributeSet) errBAttributeSet, true);
		multilinePromptAttributeSet = new SimpleAttributeSet(defaultAttributeSet);
		StyleConstants.setForeground((MutableAttributeSet) multilinePromptAttributeSet, Color.lightGray);
		readerThread = new ReaderThread();
		readerThread.start();
		opened = true;
		minimalCaretPosition = 0;
		this.controller.addCliJPanel(this);
		activeContextJLabel = new ActiveContextJLabel();
		add(activeContextJLabel, BorderLayout.NORTH);
		this.statementStateListener = new MyStatementStateListener();
		aletheiaJPanel.getPersistenceManager().getListenerManager().getRootContextTopStateListeners().add(this.statementStateListener);
		this.commandHistory = new CommandHistory();
		Font font = FontManager.instance.defaultFont();
		setFont(font);
		textPane.setFont(font);
		activeContextJLabel.setFont(font);
		this.bracketHighLightManager = new BracketHighLightManager();
		controller.command(new Prompt(this));
		promptWhenDone = null;
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	public PersistenceManager getPersistenceManager()
	{
		return aletheiaJPanel.getPersistenceManager();
	}

	public CliController getController()
	{
		return controller;
	}

	public Context getActiveContext()
	{
		return activeContext;
	}

	public void setActiveContext(Context activeContext)
	{
		if (((activeContext == null) != (this.activeContext == null)) || (activeContext != null && !activeContext.equals(this.activeContext)))
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				if (activeContext != null)
				{
					activeContext = activeContext.refresh(transaction);
					if (activeContext == null)
						return;
				}
				this.activeContext = activeContext;
				updateActiveContextJLabel();
				if (activeContext != null)
					this.catalogJTree.setRootCatalog(activeContext.catalog());
				else
					this.catalogJTree.setRootCatalog(null);
				statementStateListener.updateActiveContext(transaction, activeContext);
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						textPane.requestFocusInWindow();
					}
				});
			}
			finally
			{
				transaction.abort();
			}
		}

	}

	private void updateActiveContextJLabel()
	{
		activeContextJLabel.setContext(activeContext);
	}

	public boolean isOpened()
	{
		return opened;
	}

	public PrintStream out()
	{
		return readerThread.getOut(defaultAttributeSet);
	}

	public PrintStream outB()
	{
		return readerThread.getOut(defaultBAttributeSet);
	}

	public PrintStream err()
	{
		return readerThread.getOut(errAttributeSet);
	}

	public PrintStream errB()
	{
		return readerThread.getOut(errBAttributeSet);
	}

	public void close() throws InterruptedException, IOException
	{
		opened = false;
		aletheiaJPanel.getPersistenceManager().getListenerManager().getRootContextTopStateListeners().remove(statementStateListener);
		readerThread.close();
		controller.removeCliJPanel(this);
		catalogJTree.close();
	}

	private synchronized void printString(String s, AttributeSet attributeSet)
	{
		String c = consumeCommand();
		if (!c.isEmpty())
			commandHistory.addAndPosition(c);
		try
		{
			moveCaretToEnd();
			document.insertString(document.getEndPosition().getOffset() - 1, s, attributeSet);
			if (attributeSet != defaultAttributeSet)
				textPane.setCharacterAttributes(defaultAttributeSet, true);
		}
		catch (BadLocationException e)
		{
			throw new Error(e);
		}
		updateMinimalCaretPosition();
	}

	private synchronized void moveCaretToMinimal()
	{
		textPane.setCaretPosition(minimalCaretPosition);
	}

	private synchronized void moveCaretToEnd()
	{
		textPane.setCaretPosition(document.getLength());
	}

	private synchronized void updateMinimalCaretPosition()
	{
		moveCaretToEnd();
		minimalCaretPosition = document.getEndPosition().getOffset() - 1;
	}

	protected void escape() throws InterruptedException
	{
		String s = consumeCommand(true);
		if (!s.isEmpty())
			commandHistory.addAndPosition(s);
		command(new EmptyCommand(this));
	}

	protected void command(String s) throws InterruptedException
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Command cmd = Command.parse(this, transaction, s);
			command(cmd);
			if (!(cmd instanceof TransactionalCommand))
				transaction.abort();
		}
		catch (CommandParseException e)
		{
			transaction.abort();
			command(new TraceException(this, e));
		}
		if (!s.isEmpty())
			commandHistory.addAndPosition(s);
	}

	public void command(Command command) throws InterruptedException
	{
		command(command, true);
	}

	public void command(Command command, boolean promptWhenDone) throws InterruptedException
	{
		if (promptWhenDone)
			this.promptWhenDone = command;
		controller.command(command);
	}

	public void updateFontSize()
	{
		Font font = FontManager.instance.defaultFont();
		setFont(font);
		textPane.setFont(font);
		activeContextJLabel.setFont(font);
		catalogJTree.updateFontSize();
	}

	public synchronized void clear()
	{
		try
		{
			minimalCaretPosition = 0;
			document.remove(0, document.getLength());
			updateMinimalCaretPosition();
		}
		catch (BadLocationException e)
		{
			throw new Error(e);
		}
		finally
		{
		}

	}

	private String getCommand()
	{
		try
		{
			return document.getText(minimalCaretPosition, document.getLength() - minimalCaretPosition);
		}
		catch (BadLocationException e)
		{
			throw new Error(e);
		}
	}

	private String filterMultiline(String s)
	{
		return multiLinePattern.matcher(s).replaceAll(" ");
	}

	private String getCommandMultilineFiltered()
	{
		return filterMultiline(getCommand()).trim();
	}

	private enum BHLMTokenType
	{
		OpenQuote, CloseQuote, OpenPar, ClosePar, OpenFun, CloseFun, Arrow, Nothing, Out, Star
	};

	private class BracketHighLightManager
	{
		private class HighLight
		{
			public final AttributeSet attributes;
			public final AttributeSet oldAttributes;

			public int begin;
			public int end;

			public HighLight(AttributeSet attributes, int begin, int end)
			{
				super();
				this.attributes = attributes;
				this.oldAttributes = document.getCharacterElement(begin).getAttributes().copyAttributes();
				this.begin = begin;
				this.end = end;
			}

			public void activate()
			{
				document.setCharacterAttributes(begin, end - begin, attributes, false);
			}

			public void deactivate()
			{
				document.setCharacterAttributes(begin, end - begin, oldAttributes, true);
				int pos = textPane.getCaretPosition();
				if (pos == end)
					textPane.setCharacterAttributes(oldAttributes, true);
			}

		}

		private final AttributeSet activeAS;
		private final AttributeSet errorAS;
		private final Set<HighLight> highLights;

		public BracketHighLightManager()
		{
			this.activeAS = new SimpleAttributeSet();
			StyleConstants.setForeground((MutableAttributeSet) this.activeAS, Color.BLUE);
			StyleConstants.setBold((MutableAttributeSet) this.activeAS, true);
			StyleConstants.setUnderline((MutableAttributeSet) this.activeAS, true);
			this.errorAS = new SimpleAttributeSet();
			StyleConstants.setForeground((MutableAttributeSet) this.errorAS, Color.RED);
			StyleConstants.setBold((MutableAttributeSet) this.errorAS, true);
			StyleConstants.setUnderline((MutableAttributeSet) this.errorAS, true);
			this.highLights = new HashSet<HighLight>();
		}

		public void textInserted(int offset, int length)
		{
			for (Iterator<HighLight> it = highLights.iterator(); it.hasNext();)
			{
				HighLight hl = it.next();
				if (hl.begin >= offset)
				{
					hl.begin += length;
					hl.end += length;
				}
				else if (hl.end >= offset)
					hl.end += length;
			}
		}

		public void textRemoved(int offset, int length)
		{
			for (Iterator<HighLight> it = highLights.iterator(); it.hasNext();)
			{
				HighLight hl = it.next();
				if (hl.begin >= offset)
				{
					hl.begin -= length;
					hl.end -= length;
				}
				else if (hl.end > offset)
					hl.end -= length;
			}
		}

		private HighLight createHighLight(AttributeSet attributeSet, int begin, int end)
		{
			HighLight hl = new HighLight(attributeSet, begin, end);
			hl.activate();
			highLights.add(hl);
			return hl;
		}

		public void clearHighLights()
		{
			for (Iterator<HighLight> it = highLights.iterator(); it.hasNext();)
			{
				HighLight hl = it.next();
				hl.deactivate();
				it.remove();
			}
		}

		private class Token
		{
			public final BHLMTokenType type;
			public final int offset;
			public final int begin;
			public final int end;

			public Token(BHLMTokenType type, int offset, int begin, int end)
			{
				super();
				this.type = type;
				this.offset = offset;
				this.begin = begin;
				this.end = end;
			}

			public int absBegin()
			{
				return offset + begin;
			}

			public int absEnd()
			{
				return offset + end;
			}

		}

		private class Cursor
		{
			private final String command;
			private final int offset;
			private int position;
			private int quotes;
			private Token token;

			public Cursor()
			{
				this.command = getCommand();
				this.offset = minimalCaretPosition;
				this.position = textPane.getCaretPosition() - offset;
				this.quotes = 0;
				int i = -1;
				while (true)
				{
					i = command.indexOf('"', i + 1);
					if ((i >= 0) && (i < position))
						quotes++;
					else
						break;
				}
				token = token();
			}

			public Token getToken()
			{
				return token;
			}

			private Token token()
			{
				int pos = position - 1;
				if (pos >= 0 && pos < command.length())
				{
					switch (command.charAt(pos))
					{
					case '"':
					{
						if (quotes % 2 != 0)
							return new Token(BHLMTokenType.OpenQuote, offset, pos, position);
						else
							return new Token(BHLMTokenType.CloseQuote, offset, pos, position);
					}
					case '(':
						return new Token(BHLMTokenType.OpenPar, offset, pos, position);
					case ')':
						return new Token(BHLMTokenType.ClosePar, offset, pos, position);
					case '<':
						return new Token(BHLMTokenType.OpenFun, offset, pos, position);
					case '>':
						if ((pos > 0) && (command.charAt(pos - 1) == '-'))
							return new Token(BHLMTokenType.Arrow, offset, pos - 1, position);
						else
							return new Token(BHLMTokenType.CloseFun, offset, pos, position);
					case '*':
						return new Token(BHLMTokenType.Star, offset, pos, position);
					default:
						return new Token(BHLMTokenType.Nothing, offset, pos, position);
					}
				}
				else
					return token = new Token(BHLMTokenType.Out, offset, position, position);
			}

			public Token forward()
			{
				if (position >= 0 && position < command.length())
				{
					if ((token.type == BHLMTokenType.OpenQuote) || (token.type == BHLMTokenType.CloseQuote))
						quotes++;
					do
					{
						position++;
						token = token();
					} while (token.type == BHLMTokenType.Nothing);
					return token;
				}
				else
					return new Token(BHLMTokenType.Out, offset, position, position);
			}

			public Token backward()
			{
				if (position > 0 && position <= command.length())
				{
					if ((token.type == BHLMTokenType.OpenQuote) || (token.type == BHLMTokenType.CloseQuote))
						quotes--;
					do
					{
						position--;
						token = token();
					} while (token.type == BHLMTokenType.Nothing);
					return token;
				}
				else
					return new Token(BHLMTokenType.Out, offset, position, position);
			}
		}

		public void update()
		{
			clearHighLights();
			Cursor cursor = new Cursor();
			Token firstToken = cursor.getToken();
			switch (firstToken.type)
			{
			case OpenQuote:
			{
				int p = 0;
				int f = 0;
				int a = 0;
				Token token;
				loop: while (true)
				{
					token = cursor.forward();
					switch (token.type)
					{
					case OpenPar:
						p++;
						break;
					case ClosePar:
						p--;
						break;
					case OpenFun:
						f++;
						a++;
						break;
					case CloseFun:
						f--;
						break;
					case Arrow:
						a--;
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p < 0) || (f < 0) || (a < 0))
						break;
				}
				boolean ok = (token.type == BHLMTokenType.CloseQuote) && (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				break;
			}
			case CloseQuote:
			{
				int p = 0;
				int f = 0;
				int a = 0;
				Token token;
				loop: while (true)
				{
					token = cursor.backward();
					switch (token.type)
					{
					case ClosePar:
						p++;
						break;
					case OpenPar:
						p--;
						break;
					case CloseFun:
						f++;
						a++;
						break;
					case OpenFun:
						f--;
						break;
					case Arrow:
						a--;
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p < 0) || (f < 0) || (a < 0))
						break;
				}
				boolean ok = (token.type == BHLMTokenType.OpenQuote) && (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				break;
			}
			case OpenPar:
			{
				int p = 1;
				int f = 0;
				int a = 0;
				Token token;
				loop: while (true)
				{
					token = cursor.forward();
					switch (token.type)
					{
					case OpenPar:
						p++;
						break;
					case ClosePar:
						p--;
						break;
					case OpenFun:
						f++;
						a++;
						break;
					case CloseFun:
						f--;
						break;
					case Arrow:
						a--;
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p <= 0) || (f < 0) || (a < 0))
						break;
				}
				boolean ok = (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				break;
			}
			case ClosePar:
			{
				int p = 1;
				int f = 0;
				int a = 0;
				Token token;
				loop: while (true)
				{
					token = cursor.backward();
					switch (token.type)
					{
					case ClosePar:
						p++;
						break;
					case OpenPar:
						p--;
						break;
					case CloseFun:
						f++;
						a++;
						break;
					case OpenFun:
						f--;
						break;
					case Arrow:
						a--;
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p <= 0) || (f < 0) || (a < 0))
						break;
				}
				boolean ok = (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				break;
			}
			case OpenFun:
			{
				int p = 0;
				int f = 1;
				int a = 1;
				Token token;
				Token tarrow = null;
				loop: while (true)
				{
					token = cursor.forward();
					switch (token.type)
					{
					case OpenPar:
						p++;
						break;
					case ClosePar:
						p--;
						break;
					case OpenFun:
						f++;
						a++;
						break;
					case CloseFun:
						f--;
						break;
					case Arrow:
						a--;
						if ((a == 0) && (tarrow == null))
						{
							tarrow = token;
							if (p != 0)
								break loop;
						}
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean ok = (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				if (tarrow != null)
					createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
				Token tstar = cursor.forward();
				if (!tstar.type.equals(BHLMTokenType.Star))
					tstar = null;
				if (tstar != null)
					createHighLight(as, tstar.absBegin(), tstar.absEnd());
				break;
			}
			case CloseFun:
			{
				int p = 0;
				int f = 1;
				int a = 1;
				Token token;
				Token tarrow = null;
				Token tstar = cursor.forward();
				if (!tstar.type.equals(BHLMTokenType.Star))
					tstar = null;
				cursor = new Cursor();
				loop: while (true)
				{
					token = cursor.backward();
					switch (token.type)
					{
					case ClosePar:
						p++;
						break;
					case OpenPar:
						p--;
						break;
					case CloseFun:
						f++;
						a++;
						break;
					case OpenFun:
						f--;
						break;
					case Arrow:
						a--;
						if ((a == 0) && (tarrow == null))
						{
							tarrow = token;
							if (p != 0)
								break loop;
						}
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean ok = (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				if (tarrow != null)
					createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
				if (tstar != null)
					createHighLight(as, tstar.absBegin(), tstar.absEnd());
				break;
			}
			case Arrow:
			{
				int p = 0;
				int f = 1;
				int a = 0;
				Token token;
				loop: while (true)
				{
					token = cursor.forward();
					switch (token.type)
					{
					case OpenPar:
						p++;
						break;
					case ClosePar:
						p--;
						break;
					case OpenFun:
						f++;
						a++;
						break;
					case CloseFun:
						f--;
						break;
					case Arrow:
						a--;
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean ok = (p == 0) && (f == 0) & (a == 0);
				Token tstar = cursor.forward();
				if (!tstar.type.equals(BHLMTokenType.Star))
					tstar = null;
				cursor = new Cursor();
				Token tokenRight = token;
				p = 0;
				f = 1;
				a = 0;
				loop: while (true)
				{
					token = cursor.backward();
					switch (token.type)
					{
					case ClosePar:
						p++;
						break;
					case OpenPar:
						p--;
						break;
					case CloseFun:
						f++;
						a++;
						break;
					case OpenFun:
						f--;
						break;
					case Arrow:
						a--;
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				ok = ok && (p == 0) && (f == 0) & (a == 0);
				AttributeSet as = ok ? activeAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				if (tokenRight.type != BHLMTokenType.Out)
					createHighLight(as, tokenRight.absBegin(), tokenRight.absEnd());
				if (tstar != null)
					createHighLight(as, tstar.absBegin(), tstar.absEnd());
				break;
			}
			case Star:
			{
				Token tstar = firstToken;
				firstToken = cursor.backward();
				if (firstToken.type.equals(BHLMTokenType.CloseFun))
				{
					int p = 0;
					int f = 1;
					int a = 1;
					Token token;
					Token tarrow = null;
					loop: while (true)
					{
						token = cursor.backward();
						switch (token.type)
						{
						case ClosePar:
							p++;
							break;
						case OpenPar:
							p--;
							break;
						case CloseFun:
							f++;
							a++;
							break;
						case OpenFun:
							f--;
							break;
						case Arrow:
							a--;
							if ((a == 0) && (tarrow == null))
							{
								tarrow = token;
								if (p != 0)
									break loop;
							}
							break;
						case Star:
							break;
						default:
							break loop;
						}
						if ((f <= 0) || (p < 0) || (a < 0))
							break;
					}
					boolean ok = (p == 0) && (f == 0) & (a == 0);
					AttributeSet as = ok ? activeAS : errorAS;
					createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
					if (token.type != BHLMTokenType.Out)
						createHighLight(as, token.absBegin(), token.absEnd());
					if (tarrow != null)
						createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
					if (tstar != null)
						createHighLight(as, tstar.absBegin(), tstar.absEnd());
				}
				else
				{
					boolean ok = false;
					AttributeSet as = ok ? activeAS : errorAS;
					createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
					if (tstar != null)
						createHighLight(as, tstar.absBegin(), tstar.absEnd());
				}
				break;
			}
			default:
				break;

			}
		}
	}

	public void lock(Collection<Transaction> owners)
	{
		catalogJTreeLayerUI.lock(owners);
	}

	protected void commandDone(Command command) throws InterruptedException
	{
		if (promptWhenDone == command)
		{
			promptWhenDone = null;
			controller.command(new Prompt(this));
		}
	}

	private String consumeCommand()
	{
		return consumeCommand(false);
	}

	private String consumeCommand(boolean forceNl)
	{
		String s = getCommandMultilineFiltered();
		if (forceNl || !s.isEmpty())
		{
			moveCaretToEnd();
			try
			{
				document.insertString(textPane.getCaretPosition(), "\n", defaultAttributeSet);
			}
			catch (BadLocationException e2)
			{
				throw new Error(e2);
			}
			bracketHighLightManager.clearHighLights();
		}
		return s;
	}

	public void exception(String message, Exception exception) throws InterruptedException
	{
		String s = consumeCommand();
		if (!s.isEmpty())
			commandHistory.addAndPosition(s);
		command(new TraceException(this, message, exception));
	}

	public void exception(Exception exception) throws InterruptedException
	{
		exception(null, exception);
	}

	public void message(String message) throws InterruptedException
	{
		String s = consumeCommand();
		if (!s.isEmpty())
			commandHistory.addAndPosition(s);
		command(new SimpleMessage(this, message));
	}

	public Statement getSelectedStatement()
	{
		return getAletheiaJPanel().getContextJTree().getSelectedStatement();
	}

}
