/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.awt.KeyboardFocusManager;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.text.AbstractDocument;
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
import javax.swing.undo.CannotUndoException;

import org.apache.logging.log4j.Logger;

import aletheia.gui.app.AletheiaJFrame;
import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.app.FontManager;
import aletheia.gui.catalogjtree.CatalogJTree;
import aletheia.gui.cli.command.AbstractCommandFactory;
import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.Command.CommandParseException;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.aux.EmptyCommand;
import aletheia.gui.cli.command.gui.Prompt;
import aletheia.gui.cli.command.gui.SimpleMessage;
import aletheia.gui.cli.command.gui.TraceException;
import aletheia.gui.common.DraggableJScrollPane;
import aletheia.gui.common.FocusBorderManager;
import aletheia.gui.common.PassphraseDialog;
import aletheia.gui.common.PersistentJTreeLayerUI;
import aletheia.gui.common.datatransfer.NamespaceDataFlavor;
import aletheia.gui.common.datatransfer.StatementDataFlavor;
import aletheia.gui.common.datatransfer.TermDataFlavor;
import aletheia.gui.common.datatransfer.TermParameterIdentificationDataFlavor;
import aletheia.gui.common.datatransfer.UUIDDataFlavor;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.prooffinder.ProofFinder;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.gui.MyJSplitPane;

public class CliJPanel extends JPanel implements CommandSource
{
	private static final long serialVersionUID = -2211989098955644681L;
	private static final Logger logger = LoggerManager.instance.logger();
	private static final String multiLinePrompt = "\u00bb";

	private static final AttributeSet defaultAttributeSet = new SimpleAttributeSet();
	private static final AttributeSet defaultBAttributeSet = new SimpleAttributeSet(defaultAttributeSet);

	static
	{
		StyleConstants.setBold((MutableAttributeSet) defaultBAttributeSet, true);
	}

	private static final AttributeSet errAttributeSet = new SimpleAttributeSet(defaultAttributeSet);

	static
	{
		StyleConstants.setForeground((MutableAttributeSet) errAttributeSet, Color.red);
	}

	private static final AttributeSet errBAttributeSet = new SimpleAttributeSet(errAttributeSet);

	static
	{
		StyleConstants.setBold((MutableAttributeSet) errBAttributeSet, true);
		StyleConstants.setUnderline((MutableAttributeSet) errBAttributeSet, true);
	}

	private static final AttributeSet multiLinePromptAttributeSet = new SimpleAttributeSet(defaultAttributeSet);

	static
	{
		StyleConstants.setForeground((MutableAttributeSet) multiLinePromptAttributeSet, Color.lightGray);
	}

	private static final Object flushCommandBufferAttribute = new Object();

	private static final SimpleAttributeSet outPAttributeSet = new SimpleAttributeSet(defaultAttributeSet);

	static
	{
		outPAttributeSet.addAttribute(flushCommandBufferAttribute, true);
	}

	private static class CommandHistory
	{
		private final static int sizeLimit = 1500;
		private final static int sizePurge = 1000;

		private final ArrayList<String> commandList;
		private int position;

		public CommandHistory()
		{
			commandList = new ArrayList<>();
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

	private void insertCommandStringToDocument(String command) throws BadLocationException
	{
		document.insertString(minimalCaretPosition, command, defaultAttributeSet);
		int i = -1;
		while (true)
		{
			i = command.indexOf("\n" + multiLinePrompt, i + 1);
			if (i < 0)
				break;
			document.setCharacterAttributes(minimalCaretPosition + i + 1, multiLinePrompt.length(), multiLinePromptAttributeSet, true);
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
						String s = getCommand().trim();
						if (!s.isEmpty() && (commandHistory.atEnd() || !s.equals(commandHistory.current())))
							commandHistory.add(s);
						document.remove(minimalCaretPosition, document.getLength() - minimalCaretPosition);
						insertCommandStringToDocument(commandHistory.decrease());
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
						String s = getCommand().trim();
						if (!s.isEmpty() && (commandHistory.atEnd() || !s.equals(commandHistory.current())))
							commandHistory.add(s);
						document.remove(minimalCaretPosition, document.getLength() - minimalCaretPosition);
						insertCommandStringToDocument(commandHistory.increase());
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
			case KeyEvent.VK_C:
			{
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
				{
					if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0)
						controller.cancelActiveCommand("by user");
					else
					{
						textPane.copy();
						e.consume();
					}
				}
				break;
			}
			case KeyEvent.VK_V:
			{
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
				{
					textPane.paste();
					e.consume();
				}
				break;
			}
			case KeyEvent.VK_SPACE:
			{
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
					try
					{
						completion();
					}
					catch (InterruptedException e1)
					{
						logger.error(e1.getMessage(), e1);
					}
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
			if ((e.getKeyChar() == '\n' || e.getKeyChar() == '\r') && !e.isAltDown() && !e.isControlDown() && !e.isAltGraphDown())
			{
				synchronized (CliJPanel.this)
				{
					bracketHighLightManager.clearHighLights();
					try
					{
						if (!e.isShiftDown())
						{
							String s = getCommand().trim();
							updateMinimalCaretPosition();
							command(s);
						}
						else
						{
							try
							{
								document.insertString(textPane.getCaretPosition(), "\n" + multiLinePrompt + " ", multiLinePromptAttributeSet);
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

			}
			else if (e.getKeyChar() == KeyEvent.VK_CANCEL && (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0)
				controller.cancelActiveCommand("by user");
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
			if (!inserting)
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
						length += i + 1;
					if (offset + length > document.getLength())
						length = document.getLength() - offset;
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
			if (e.getEdit() instanceof AbstractDocument.DefaultDocumentEvent)
			{
				AbstractDocument.DefaultDocumentEvent edit = (AbstractDocument.DefaultDocumentEvent) e.getEdit();
				if (edit.getOffset() < minimalCaretPosition)
					try
					{
						e.getEdit().undo();
					}
					catch (CannotUndoException ex)
					{
					}
			}
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

		private final Charset charSet = StandardCharsets.UTF_8;
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
			channelMap = new HashMap<>();
			channels = new ArrayList<>();
			attributeSets = new ArrayList<>();
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
						String s = new String(buf, 0, size, charSet);
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
				out = new PrintStream(new MultiplexedOutputStream(outputStream, chan), false, charSet);
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
			setForeground(new Color(0x400040));
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
			this.ancestorContexts = new HashSet<>();
			this.context = null;
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
								try
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
								}
								catch (Error e)
								{
									logger.error(e, e);
								}
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
						Context activeContext = getActiveContext();
						String path = statement.statementPathString(transaction, activeContext);
						if (!path.isEmpty() || statement.equals(activeContext))
							return importText(comp, path);
						else
						{
							if (statement.getIdentifier() != null)
							{
								if ((activeContext != null) && (statement instanceof Context))
								{
									List<? extends Statement> absPath = activeContext.statementPath(transaction, (Context) statement);
									boolean found = false;
									for (Statement st : absPath)
										if (statement.getIdentifier().equals(st.getIdentifier()))
										{
											found = true;
											break;
										}
									if (found)
										return importText(comp, statement.getUuid().toString());
								}
								return importText(comp, statement.getIdentifier().qualifiedName());
							}
							else
								return importText(comp, statement.getUuid().toString());
						}

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
					ParameterIdentification parameterIdentification = null;
					if (t.isDataFlavorSupported(TermParameterIdentificationDataFlavor.instance))
						parameterIdentification = (ParameterIdentification) t.getTransferData(TermParameterIdentificationDataFlavor.instance);
					Transaction transaction = getPersistenceManager().beginTransaction(100);
					try
					{
						Context context = getActiveContext();
						if (context == null)
							return importText(comp, term.toString(parameterIdentification));
						else
							return importText(comp, term.toString(transaction, context, parameterIdentification));
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
				if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					String s = (String) t.getTransferData(DataFlavor.stringFlavor);
					StringSelection sel = new StringSelection(s);
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
	private final FocusBorderManager textPaneFocusBorderManager;
	private final ReaderThread readerThread;
	private final PrintStream out;
	private final PrintStream outB;
	private final PrintStream outP;
	private final PrintStream err;
	private final PrintStream errB;
	private final ActiveContextJLabel activeContextJLabel;
	private final MyStatementStateListener statementStateListener;
	private final MyJSplitPane splitPane;
	private CatalogJTree catalogJTree;
	private PersistentJTreeLayerUI<CatalogJTree> catalogJTreeLayerUI;
	private DraggableJScrollPane catalogJTreeDraggableJScrollPane;
	private boolean dragging;
	private FocusBorderManager catalogJTreeFocusBorderManager;
	private final CommandHistory commandHistory;
	private final BracketHighLightManager bracketHighLightManager;

	private int minimalCaretPosition;
	private boolean opened;
	private Context activeContext;
	private Command promptWhenDone;
	private StringBuffer commandBuffer;
	private int commandBufferCaretOffset;
	private PrintWriter consolePrintWriter;
	private boolean inserting = false;

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
		textPaneFocusBorderManager = new FocusBorderManager(scrollTextPane, textPane);
		catalogJTree = new CatalogJTree(this);
		catalogJTreeLayerUI = new PersistentJTreeLayerUI<>(aletheiaJPanel.getAletheiaJFrame(), catalogJTree);
		catalogJTreeDraggableJScrollPane = new DraggableJScrollPane(catalogJTreeLayerUI.getJLayer(), catalogJTree);
		dragging = false;
		catalogJTreeFocusBorderManager = new FocusBorderManager(catalogJTreeDraggableJScrollPane, catalogJTree);
		splitPane = new MyJSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTextPane, catalogJTreeDraggableJScrollPane);
		splitPane.setResizeWeight(1);
		splitPane.setDividerLocationOrCollapseWhenValid(1.0d);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
		textPane.addKeyListener(new MyKeyListener());
		textPane.addCaretListener(new MyCaretListener());
		textPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		textPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		readerThread = new ReaderThread();
		readerThread.start();
		out = readerThread.getOut(defaultAttributeSet);
		outB = readerThread.getOut(defaultBAttributeSet);
		outP = readerThread.getOut(outPAttributeSet);
		err = readerThread.getOut(errAttributeSet);
		errB = readerThread.getOut(errBAttributeSet);
		opened = true;
		minimalCaretPosition = 0;
		this.controller.addCliJPanel(this);
		activeContextJLabel = new ActiveContextJLabel();
		add(activeContextJLabel, BorderLayout.NORTH);
		this.statementStateListener = new MyStatementStateListener();
		aletheiaJPanel.getPersistenceManager().getListenerManager().getRootContextTopStateListeners().add(this.statementStateListener);
		this.commandHistory = new CommandHistory();
		Font font = aletheiaJPanel.getFontManager().defaultFont();
		setFont(font);
		textPane.setFont(font);
		activeContextJLabel.setFont(font);
		this.bracketHighLightManager = new BracketHighLightManager();
		controller.command(new Prompt(this));
		promptWhenDone = null;
		commandBuffer = new StringBuffer();
		commandBufferCaretOffset = 0;
		consolePrintWriter = null;
		setActiveContext(initialActiveContext(getPersistenceManager()));
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	public FontManager getFontManager()
	{
		return getAletheiaJPanel().getFontManager();
	}

	public void textPaneRequestFocus()
	{
		textPane.requestFocus();
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return aletheiaJPanel.getPersistenceManager();
	}

	public CliController getController()
	{
		return controller;
	}

	@Override
	public PrintStream getOut()
	{
		return out;
	}

	@Override
	public PrintStream getOutB()
	{
		return outB;
	}

	@Override
	public PrintStream getOutP()
	{
		return outP;
	}

	@Override
	public PrintStream getErr()
	{
		return err;
	}

	@Override
	public PrintStream getErrB()
	{
		return errB;
	}

	@Override
	public Context getActiveContext()
	{
		return activeContext;
	}

	public JScrollPane getScrollTextPane()
	{
		return scrollTextPane;
	}

	public synchronized CatalogJTree getCatalogJTree()
	{
		return catalogJTree;
	}

	public synchronized DraggableJScrollPane getCatalogJTreeDraggableJScrollPane()
	{
		return catalogJTreeDraggableJScrollPane;
	}

	public synchronized void setCatalogJTreeDraggableJScrollPane(DraggableJScrollPane catalogJTreeDraggableJScrollPane)
	{
		this.catalogJTreeDraggableJScrollPane = catalogJTreeDraggableJScrollPane;
	}

	public synchronized boolean isDragging()
	{
		return dragging;
	}

	public synchronized void setDragging(boolean dragging)
	{
		this.dragging = dragging;
		if (catalogJTreeDraggableJScrollPane != null)
			catalogJTreeDraggableJScrollPane.setDragging(dragging);
		if (catalogJTree != null)
			catalogJTree.setDragEnabled(!dragging);

	}

	@Override
	public synchronized void setActiveContext(Context activeContext)
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
				getAletheiaJPanel().getContextJTree().setActiveContext(activeContext);
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

	public void close() throws InterruptedException, IOException
	{
		opened = false;
		aletheiaJPanel.getPersistenceManager().getListenerManager().getRootContextTopStateListeners().remove(statementStateListener);
		textPaneFocusBorderManager.close();
		readerThread.close();
		controller.removeCliJPanel(this);
		synchronized (this)
		{
			catalogJTree.close();
			catalogJTreeLayerUI.close();
			catalogJTreeFocusBorderManager.close();
			if (consolePrintWriter != null)
				consolePrintWriter.close();
		}
	}

	private synchronized void insertString(int offset, String s, AttributeSet attributeSet)
	{
		try
		{
			inserting = true;
			document.insertString(offset, s, attributeSet);
			inserting = false;
		}
		catch (BadLocationException e)
		{
			logger.error(String.format("Could not insert string \"%s\" to CLI document at position %d", s, offset), e);
		}
	}

	private synchronized void printString(String s, AttributeSet attributeSet)
	{
		if (consolePrintWriter != null)
		{
			consolePrintWriter.print(s);
			consolePrintWriter.flush();
		}
		commandBufferCaretOffset += moveCaretToEnd();
		commandBuffer.append(getCommand(true));
		insertString(document.getEndPosition().getOffset() - 1, s, attributeSet);
		updateMinimalCaretPosition();
		if (attributeSet.containsAttribute(flushCommandBufferAttribute, true))
		{
			insertString(document.getEndPosition().getOffset() - 1, commandBuffer.toString(), attributeSet);
			commandBuffer = new StringBuffer();
			moveCaretToEnd(commandBufferCaretOffset);
			commandBufferCaretOffset = 0;
		}
		if (attributeSet != defaultAttributeSet)
			textPane.setCharacterAttributes(defaultAttributeSet, true);
	}

	private synchronized void moveCaretToMinimal()
	{
		textPane.setCaretPosition(minimalCaretPosition);
	}

	private synchronized int moveCaretToEnd(int offset)
	{
		int offset_ = textPane.getCaretPosition() - document.getLength();
		try
		{
			textPane.setCaretPosition(document.getLength() + offset);
		}
		catch (IllegalArgumentException e)
		{
			textPane.setCaretPosition(document.getLength());
		}
		catch (NullPointerException e)
		{
			//Workaround
			logger.error(e.getMessage(), e);
		}
		return offset_;
	}

	private synchronized int moveCaretToEnd()
	{
		return moveCaretToEnd(0);
	}

	private synchronized void updateMinimalCaretPosition()
	{
		moveCaretToEnd();
		minimalCaretPosition = document.getEndPosition().getOffset() - 1;
	}

	protected void escape() throws InterruptedException
	{
		String s = getCommand().trim();
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
		if (!s.isEmpty())
			commandHistory.addAndPosition(s);
		command(new EmptyCommand(this));
	}

	private static String replaceMultiLinePromptWithSpaces(String s)
	{
		return s.replace(multiLinePrompt, Collections.nCopies(multiLinePrompt.length(), " ").stream().collect(Collectors.joining()));
	}

	protected Command command(String s) throws InterruptedException
	{
		synchronized (this)
		{
			if (consolePrintWriter != null)
				consolePrintWriter.println(s);
		}
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Command cmd = Command.parse(this, transaction, replaceMultiLinePromptWithSpaces(s));
			command(cmd);
			if (!(cmd instanceof TransactionalCommand))
				transaction.abort();
			return cmd;
		}
		catch (CommandParseException e)
		{
			transaction.abort();
			Command cmd = new TraceException(this, e);
			command(cmd);
			return cmd;
		}
		finally
		{
			if (!s.isEmpty())
				commandHistory.addAndPosition(s);
		}
	}

	@Override
	public void command(Command command) throws InterruptedException
	{
		command(command, true);
	}

	public synchronized void command(Command command, boolean promptWhenDone) throws InterruptedException
	{
		if (promptWhenDone)
			this.promptWhenDone = command;
		controller.command(command);
	}

	public synchronized void updateFontSize()
	{
		Font font = getFontManager().defaultFont();
		setFont(font);
		textPane.setFont(font);
		activeContextJLabel.setFont(font);
		catalogJTree.updateFontSize();
	}

	@Override
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

	private synchronized String getCommand(boolean remove)
	{
		if (minimalCaretPosition > document.getLength())
			return "";
		try
		{
			String text = document.getText(minimalCaretPosition, document.getLength() - minimalCaretPosition);
			if (remove && minimalCaretPosition < document.getLength())
				document.remove(minimalCaretPosition, document.getLength() - minimalCaretPosition);
			return text;
		}
		catch (BadLocationException e)
		{
			throw new Error(e);
		}
	}

	private String getCommand()
	{
		return getCommand(false);
	}

	private enum BHLMTokenType
	{
		OpenQuote, CloseQuote, OpenPar, ClosePar, OpenFun, CloseFun, Comma, Arrow, Default, Blank, Out, Star
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

		private final static boolean BOLD = false;
		private final static boolean UNDERLINE = true;
		private final Color OK_COLOR = Color.BLUE;
		private final Color WARNING_COLOR = Color.CYAN.darker().darker();
		private final Color ERROR_COLOR = Color.RED;

		private final SimpleAttributeSet okAS;
		private final SimpleAttributeSet warningAS;
		private final SimpleAttributeSet errorAS;
		private final Set<HighLight> highLights;

		public BracketHighLightManager()
		{
			this.okAS = new SimpleAttributeSet();
			this.warningAS = new SimpleAttributeSet();
			this.errorAS = new SimpleAttributeSet();
			StyleConstants.setForeground(this.okAS, OK_COLOR);
			StyleConstants.setForeground(this.warningAS, WARNING_COLOR);
			StyleConstants.setForeground(this.errorAS, ERROR_COLOR);
			if (BOLD)
			{
				StyleConstants.setBold(this.okAS, true);
				StyleConstants.setBold(this.warningAS, true);
				StyleConstants.setBold(this.errorAS, true);
			}
			if (UNDERLINE)
			{
				StyleConstants.setUnderline(this.okAS, true);
				StyleConstants.setUnderline(this.warningAS, true);
				StyleConstants.setUnderline(this.errorAS, true);
			}
			this.highLights = new HashSet<>();
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
						if ((pos + 1 < command.length()) && (command.charAt(pos + 1) == '-'))
							return new Token(BHLMTokenType.Default, offset, pos, position);
						else
							return new Token(BHLMTokenType.OpenFun, offset, pos, position);
					case '>':
						if ((pos - 1 >= 0) && (command.charAt(pos - 1) == '-'))
							return new Token(BHLMTokenType.Arrow, offset, pos - 1, position);
						else
							return new Token(BHLMTokenType.CloseFun, offset, pos, position);
					case ',':
						return new Token(BHLMTokenType.Comma, offset, pos, position);
					case '*':
						return new Token(BHLMTokenType.Star, offset, pos, position);
					case ' ':
					case '\t':
						return new Token(BHLMTokenType.Blank, offset, pos, position);
					default:
						return new Token(BHLMTokenType.Default, offset, pos, position);
					}
				}
				else
					return token = new Token(BHLMTokenType.Out, offset, position, position);
			}

			public Token forward()
			{
				return forward(true);
			}

			public Token forward(boolean skipdefaults)
			{
				if (position >= 0 && position < command.length())
				{
					if ((token.type == BHLMTokenType.OpenQuote) || (token.type == BHLMTokenType.CloseQuote))
						quotes++;
					do
					{
						position++;
						token = token();
					} while (token.type == BHLMTokenType.Blank || (skipdefaults && token.type == BHLMTokenType.Default));
					return token;
				}
				else
					return new Token(BHLMTokenType.Out, offset, position, position);
			}

			public Token backward()
			{
				return backward(true);
			}

			public Token backward(boolean skipdefaults)
			{
				if (position > 0 && position <= command.length())
				{
					if ((token.type == BHLMTokenType.OpenQuote) || (token.type == BHLMTokenType.CloseQuote))
						quotes--;
					do
					{
						position--;
						token = token();
					} while (token.type == BHLMTokenType.Blank || (skipdefaults && token.type == BHLMTokenType.Default));
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
					case Comma:
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p < 0) || (f < 0) || (a < 0))
						break;
				}
				boolean warning = (token.type == BHLMTokenType.CloseQuote) && (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
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
					case Comma:
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p < 0) || (f < 0) || (a < 0))
						break;
				}
				boolean warning = (token.type == BHLMTokenType.OpenQuote) && (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
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
					case Comma:
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p <= 0) || (f < 0) || (a < 0))
						break;
				}
				boolean warning = (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
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
					case Comma:
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((p <= 0) || (f < 0) || (a < 0))
						break;
				}
				boolean warning = (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
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
				ArrayList<Token> commas = new ArrayList<>();
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
					case Comma:
						if ((f == 1) && tarrow == null)
							commas.add(token);
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean warning = (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				if (tarrow != null)
					createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
				for (Token comma : commas)
					createHighLight(as, comma.absBegin(), comma.absEnd());

				while (true)
				{
					Token tstar = cursor.forward(false);
					if (!tstar.type.equals(BHLMTokenType.Star))
						break;
					createHighLight(as, tstar.absBegin(), tstar.absEnd());
				}
				break;
			}
			case CloseFun:
			{
				int p = 0;
				int f = 1;
				int a = 1;
				Token token;
				Token tarrow = null;
				ArrayList<Token> commas = new ArrayList<>();
				ArrayList<Token> stars = new ArrayList<>();
				for (Token tstar = cursor.forward(false); tstar.type.equals(BHLMTokenType.Star); tstar = cursor.forward(false))
					stars.add(tstar);
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
					case Comma:
						if ((f == 1) && tarrow != null)
							commas.add(token);
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean warning = (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				if (tarrow != null)
					createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
				for (Token comma : commas)
					createHighLight(as, comma.absBegin(), comma.absEnd());
				for (Token star : stars)
					createHighLight(as, star.absBegin(), star.absEnd());
				break;
			}
			case Arrow:
			{
				int p = 0;
				int f = 1;
				int a = 0;
				Token token;
				ArrayList<Token> commas = new ArrayList<>();
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
					case Comma:
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean warning = (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				ArrayList<Token> stars = new ArrayList<>();
				for (Token tstar = cursor.forward(false); tstar.type.equals(BHLMTokenType.Star); tstar = cursor.forward(false))
					stars.add(tstar);
				if (!ok)
				{
					AttributeSet as = errorAS;
					createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
					if (token.type != BHLMTokenType.Out)
						createHighLight(as, token.absBegin(), token.absEnd());
					for (Token star : stars)
						createHighLight(as, star.absBegin(), star.absEnd());
				}
				else
				{
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
						case Comma:
							if (f == 1)
								commas.add(token);
							break;
						case Star:
							break;
						default:
							break loop;
						}
						if ((f <= 0) || (p < 0) || (a < 0))
							break;
					}
					warning = (p == 0) && (f == 0) && (a >= 0);
					ok = warning && (a == 0);
					AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
					createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
					if (token.type != BHLMTokenType.Out)
						createHighLight(as, token.absBegin(), token.absEnd());
					if (tokenRight.type != BHLMTokenType.Out)
						createHighLight(as, tokenRight.absBegin(), tokenRight.absEnd());
					for (Token comma : commas)
						createHighLight(as, comma.absBegin(), comma.absEnd());
					for (Token star : stars)
						createHighLight(as, star.absBegin(), star.absEnd());
				}
				break;
			}
			case Comma:
			{
				int p = 0;
				int f = 1;
				int a = 1;
				Token token;
				Token tarrow = null;
				ArrayList<Token> commas = new ArrayList<>();
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
					case Comma:
						if ((f == 1) && tarrow == null)
							commas.add(token);
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				boolean warning = (p == 0) && (f == 0) && (a >= 0);
				boolean ok = warning && (a == 0);
				ArrayList<Token> stars = new ArrayList<>();
				for (Token tstar = cursor.forward(false); tstar.type.equals(BHLMTokenType.Star); tstar = cursor.forward(false))
					stars.add(tstar);
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
					case Comma:
						if (f == 1)
							commas.add(token);
						break;
					case Star:
						break;
					default:
						break loop;
					}
					if ((f <= 0) || (p < 0) || (a < 0))
						break;
				}
				warning = warning && (p == 0) && (f == 0) && (a >= 0);
				ok = ok && warning && (a == 0);
				AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
				createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
				if (token.type != BHLMTokenType.Out)
					createHighLight(as, token.absBegin(), token.absEnd());
				if (tokenRight.type != BHLMTokenType.Out)
					createHighLight(as, tokenRight.absBegin(), tokenRight.absEnd());
				if (tarrow != null)
					createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
				for (Token comma : commas)
					createHighLight(as, comma.absBegin(), comma.absEnd());
				for (Token star : stars)
					createHighLight(as, star.absBegin(), star.absEnd());
				break;
			}
			case Star:
			{
				ArrayList<Token> stars = new ArrayList<>();
				for (; firstToken.type.equals(BHLMTokenType.Star); firstToken = cursor.forward(false))
					;
				firstToken = firstToken.type.equals(BHLMTokenType.Out) ? cursor.getToken() : cursor.backward(false);
				for (; firstToken.type.equals(BHLMTokenType.Star); firstToken = cursor.backward(false))
					stars.add(firstToken);
				if (firstToken.type.equals(BHLMTokenType.CloseFun))
				{
					int p = 0;
					int f = 1;
					int a = 1;
					Token token;
					Token tarrow = null;
					ArrayList<Token> commas = new ArrayList<>();
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
						case Comma:
							if ((f == 1) && tarrow != null)
								commas.add(token);
							break;
						case Star:
							break;
						default:
							break loop;
						}
						if ((f <= 0) || (p < 0) || (a < 0))
							break;
					}
					boolean warning = (p == 0) && (f == 0) && (a >= 0);
					boolean ok = warning && (a == 0);
					AttributeSet as = ok ? okAS : warning ? warningAS : errorAS;
					createHighLight(as, firstToken.absBegin(), firstToken.absEnd());
					if (token.type != BHLMTokenType.Out)
						createHighLight(as, token.absBegin(), token.absEnd());
					if (tarrow != null)
						createHighLight(as, tarrow.absBegin(), tarrow.absEnd());
					for (Token comma : commas)
						createHighLight(as, comma.absBegin(), comma.absEnd());
					for (Token star : stars)
						createHighLight(as, star.absBegin(), star.absEnd());
				}
				break;
			}
			default:
				break;

			}
		}
	}

	@Override
	public synchronized void lock(Collection<? extends Transaction> owners)
	{
		catalogJTreeLayerUI.lock(owners);
	}

	protected synchronized void commandDone(Command command, Exception e) throws InterruptedException
	{
		if (promptWhenDone == command || e != null)
		{
			promptWhenDone = null;
			controller.command(new Prompt(this));
		}
	}

	public void exception(String message, Exception exception) throws InterruptedException
	{
		command(new TraceException(this, message, exception));
	}

	public void exception(Exception exception) throws InterruptedException
	{
		exception(null, exception);
	}

	public void message(String message) throws InterruptedException
	{
		command(new SimpleMessage(this, message));
	}

	public Statement getSelectedStatement()
	{
		return getAletheiaJPanel().getContextJTree().getSelectedStatement();
	}

	protected void waitCursor(boolean wait)
	{
		aletheiaJPanel.waitCursor(wait);
	}

	@Override
	public void signatureRequestJTreeSelectStatement(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement)
	{
		aletheiaJPanel.getSignatureRequestJTree().selectStatement(unpackedSignatureRequest, statement);
	}

	@Override
	public void signatureRequestJTreeSelectUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest)
	{
		aletheiaJPanel.getSignatureRequestJTree().selectUnpackedSignatureRequest(unpackedSignatureRequest);
	}

	@Override
	public PeerToPeerNode getPeerToPeerNode()
	{
		return aletheiaJPanel.getAletheiaJFrame().getPeerToPeerNode();
	}

	@Override
	public void putSelectStatement(Statement statement)
	{
		aletheiaJPanel.getContextJTree().putSelectStatement(statement);
	}

	@Override
	public void putSelectStatement(Transaction transaction, Statement statement)
	{
		aletheiaJPanel.getContextJTree().putSelectStatement(transaction, statement);
	}

	@Override
	public void putSelectContextConsequent(Context context)
	{
		aletheiaJPanel.getContextJTree().putSelectContextConsequent(context);
	}

	@Override
	public void putSelectContextConsequent(Transaction transaction, Context context)
	{
		aletheiaJPanel.getContextJTree().putSelectContextConsequent(transaction, context);
	}

	@Override
	public ProofFinder getProofFinder()
	{
		return aletheiaJPanel.getProofFinder();
	}

	@Override
	public void expandAllContexts(Context context)
	{
		aletheiaJPanel.getContextJTree().expandAllContexts(context);
	}

	@Override
	public void nodeStructureReset(Context context)
	{
		aletheiaJPanel.getContextJTree().nodeStructureReset(context);
	}

	@Override
	public void resetGui()
	{
		aletheiaJPanel.resetGui();
	}

	@Override
	public void collapseAll(Context context)
	{
		aletheiaJPanel.getContextJTree().collapseAll(context);
	}

	@Override
	public void expandGroup(Context context, Namespace prefix)
	{
		aletheiaJPanel.getContextJTree().expandGroup(context, prefix);
	}

	@Override
	public void expandSubscribedContexts(Context context)
	{
		aletheiaJPanel.getContextJTree().expandSubscribedContexts(context);
	}

	@Override
	public void expandUnprovedContexts(Context context)
	{
		aletheiaJPanel.getContextJTree().expandUnprovedContexts(context);
	}

	@Override
	public AletheiaJFrame openExtraFrame(String extraTitle)
	{
		return aletheiaJPanel.getAletheiaJFrame().openExtraFrame(extraTitle);
	}

	@Override
	public void setExtraTitle(String extraTitle)
	{
		aletheiaJPanel.getOwnerFrame().setExtraTitle(extraTitle);
	}

	@Override
	public void exit()
	{
		getAletheiaJPanel().getOwnerFrame().exit();
	}

	@Override
	public char[] passphrase(boolean confirm)
	{
		PassphraseDialog dialog = new PassphraseDialog(aletheiaJPanel.getOwnerFrame(), confirm);
		return dialog.getPassphrase();
	}

	@Override
	public boolean confirmDialog(String text)
	{
		int option = JOptionPane.showConfirmDialog(aletheiaJPanel.getOwnerFrame(),
				MiscUtilities.wrapText((text != null ? text + "\n" : "") + "Are you sure you want to continue?", 80));
		return option == JOptionPane.OK_OPTION;
	}

	@Override
	public synchronized void consoleFile(File file) throws FileNotFoundException
	{
		if (consolePrintWriter != null)
			consolePrintWriter.close();
		if (file == null)
			consolePrintWriter = null;
		else
			consolePrintWriter = new PrintWriter(new FileOutputStream(file, true), true);
	}

	@Override
	public void restart()
	{
		getAletheiaJPanel().getAletheiaJFrame().restart();
	}

	public synchronized void resetCatalogJTree() throws InterruptedException
	{
		Namespace selected = catalogJTree.getSelectedPrefix();
		catalogJTree.close();
		catalogJTreeFocusBorderManager.close();
		catalogJTree = new CatalogJTree(this);
		if (activeContext != null)
			this.catalogJTree.setRootCatalog(activeContext.catalog());
		else
			this.catalogJTree.setRootCatalog(null);
		if (selected != null)
			selectPrefix(selected);
		catalogJTreeLayerUI.close();
		catalogJTreeLayerUI = new PersistentJTreeLayerUI<>(aletheiaJPanel.getAletheiaJFrame(), catalogJTree);
		catalogJTreeDraggableJScrollPane = new DraggableJScrollPane(catalogJTreeLayerUI.getJLayer(), catalogJTree);
		catalogJTreeDraggableJScrollPane.setDragging(dragging);
		catalogJTreeFocusBorderManager = new FocusBorderManager(catalogJTreeDraggableJScrollPane, catalogJTree);
		double dl = splitPane.getProportionalDividerLocation();
		splitPane.setRightComponent(catalogJTreeDraggableJScrollPane);
		splitPane.setDividerLocationOrCollapse(dl);
	}

	public void selectPrefix(final Namespace prefix)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (CliJPanel.this)
				{
					catalogJTree.selectPrefix(prefix, false);
					catalogJTree.scrollPrefixToVisible(prefix);
				}
			}

		});
	}

	private Context initialActiveContext(PersistenceManager persistenceManager, Transaction transaction)
	{
		CloseableCollection<RootContext> rootContexts = persistenceManager.rootContexts(transaction).values();
		if (rootContexts.size() != 1)
			return null;
		else
			return MiscUtilities.firstFromCloseableIterable(persistenceManager.rootContexts(transaction).values());
	}

	private Context initialActiveContext(PersistenceManager persistenceManager)
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			return initialActiveContext(persistenceManager, transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized void completion() throws InterruptedException
	{
		if (promptWhenDone != null)
			return;
		AbstractCommandFactory.CompletionSet completionSet = null;
		try
		{
			String command = getCommand().substring(0, textPane.getCaretPosition() - minimalCaretPosition);
			completionSet = Command.completionSet(this, command);
		}
		catch (Exception e)
		{
		}
		if (completionSet != null)
		{
			if (completionSet.size() > 1)
			{
				int textPaneWidth = textPane.getWidth() / textPane.getFontMetrics(textPane.getFont()).charWidth(' ');
				List<List<String>> columns = null;
				List<Integer> widths = null;
				for (int n = 1; n <= completionSet.size(); n++)
				{
					List<List<String>> columns_ = new ArrayList<>();
					List<Integer> widths_ = new ArrayList<>();
					for (int i = 0; i < n; i++)
					{
						columns_.add(new ArrayList<String>());
						widths_.add(0);
					}
					int columnSize = (completionSet.size() - 1) / n + 1;
					int i = 0;
					for (AbstractCommandFactory.CompletionSet.Completion completion : completionSet)
					{
						String contents = completion.getContents();
						columns_.get(i).add(contents);
						if (contents.length() > widths_.get(i))
							widths_.set(i, contents.length());
						if (columns_.get(i).size() >= columnSize)
							i++;
					}
					int totalWidth = 0;
					for (int w : widths_)
						totalWidth += ((w + 10) / 8) * 8;
					if (totalWidth >= textPaneWidth)
						break;
					columns = columns_;
					widths = widths_;
				}
				StringBuilder builder = new StringBuilder();
				for (int i = 0;; i++)
				{
					if (i % columns.size() == 0)
					{
						if (i / columns.size() < columns.get(i % columns.size()).size())
							builder.append("\n");
						else
							break;
					}
					if (i / columns.size() < columns.get(i % columns.size()).size())
					{
						String completion = columns.get(i % columns.size()).get(i / columns.size());
						builder.append(completion);
						builder.append(new String(new char[((widths.get(i % columns.size()) + 10) / 8) * 8 - completion.length()]).replace('\0', ' '));
					}
				}
				message(builder.toString());
				String commonPrefix = completionSet.commonPrefix();
				if (commonPrefix.startsWith(completionSet.getQueried()))
				{
					String append = commonPrefix.substring(completionSet.getQueried().length());
					try
					{
						document.insertString(textPane.getCaretPosition(), append, defaultAttributeSet);
					}
					catch (BadLocationException e)
					{
					}
				}
			}
			else if (completionSet.size() == 1)
			{
				AbstractCommandFactory.CompletionSet.Completion completion = completionSet.first();
				if (completion.getContents().startsWith(completionSet.getQueried()))
				{
					String append = completion.getContents().substring(completionSet.getQueried().length());
					try
					{
						document.insertString(textPane.getCaretPosition(), append, defaultAttributeSet);
						if (textPane.getCaretPosition() >= document.getLength()
								|| !completion.getPost().equals(document.getText(textPane.getCaretPosition(), 1)))
							document.insertString(textPane.getCaretPosition(), completion.getPost(), defaultAttributeSet);
						else
							textPane.setCaretPosition(textPane.getCaretPosition() + completion.getPost().length());
					}
					catch (BadLocationException e)
					{
					}
				}
			}
		}

	}

	public void setExpandBySelection(boolean expandBySelection)
	{
		getCatalogJTree().setExpandBySelection(expandBySelection);
	}

}
