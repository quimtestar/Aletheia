/*******************************************************************************
 * Copyright (c) 2016, 2019 Quim Testar.
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
package aletheia.graph;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;

import aletheia.utilities.MiscUtilities;

public class Graph
{
	private final String id;
	private final Collection<? extends Node> nodes;
	private final Collection<? extends Edge> edges;

	public Graph(String id, Collection<? extends Node> nodes, Collection<? extends Edge> edges)
	{
		this.id = id;
		this.nodes = nodes;
		this.edges = edges;
	}

	public Iterable<List<Node>> cycleDetect()
	{
		final Map<Node, Set<Node>> successors = new HashMap<>();
		for (Node n : nodes)
			successors.put(n, new HashSet<Node>());
		for (Edge e : edges)
		{
			Set<Node> suc = successors.get(e.getFrom());
			if (suc == null)
			{
				suc = new HashSet<>();
				successors.put(e.getFrom(), suc);
			}
			suc.add(e.getTo());
		}

		return new Iterable<>()
		{

			@Override
			public Iterator<List<Node>> iterator()
			{

				final Set<Node> pendingNodes = new HashSet<>(nodes);
				class DequeEntry
				{
					final Node node;
					final List<Node> path;

					DequeEntry(Node node, List<Node> cycle)
					{
						super();
						this.node = node;
						this.path = cycle;
					}

					List<Node> cycle()
					{
						int i = path.indexOf(node);
						if (i >= 0)
							return path.subList(i, path.size());
						else
							return null;
					}

				}
				final Deque<DequeEntry> deque = new ArrayDeque<>();
				return new Iterator<>()
				{
					List<Node> next = findNext();

					private List<Node> findNext()
					{
						while (true)
						{
							if (deque.isEmpty())
							{
								if (!pendingNodes.isEmpty())
									deque.offer(new DequeEntry(MiscUtilities.firstFromIterable(pendingNodes), new ArrayList<Node>()));
								else
									return null;
							}
							while (!deque.isEmpty())
							{
								DequeEntry se = deque.poll();
								List<Node> cycle = se.cycle();
								if (cycle != null)
									return cycle;
								else if (pendingNodes.remove(se.node))
								{
									Set<Node> suc = successors.get(se.node);
									List<Node> path = new ArrayList<>(se.path);
									path.add(se.node);
									for (Node s : suc)
										deque.offer(new DequeEntry(s, path));
								}
							}
						}
					}

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public List<Node> next()
					{
						List<Node> current = next;
						next = findNext();
						return current;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

				};
			}

		};
	}

	public void toGraphML(File f)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();

			org.w3c.dom.Document doc = impl.createDocument(null, null, null);
			Element eGraphml = doc.createElement("graphml");
			doc.appendChild(eGraphml);

			{
				// <key attr.name="label" attr.type="string" for="node"
				// id="label"/>
				Element eKeyLabel = doc.createElement("key");
				eKeyLabel.setAttribute("attr.name", "label");
				eKeyLabel.setAttribute("attr.type", "string");
				eKeyLabel.setAttribute("for", "node");
				eKeyLabel.setAttribute("id", "label");
				eGraphml.appendChild(eKeyLabel);
			}

			{
				// <key attr.name="lower" attr.type="boolean" for="edge"
				// id="lower"/>
				Element eKeyLabel = doc.createElement("key");
				eKeyLabel.setAttribute("attr.name", "lower");
				eKeyLabel.setAttribute("attr.type", "boolean");
				eKeyLabel.setAttribute("for", "edge");
				eKeyLabel.setAttribute("id", "lower");
				eGraphml.appendChild(eKeyLabel);
				Element eDefault = doc.createElement("default");
				eDefault.setTextContent("false");
				eKeyLabel.appendChild(eDefault);
			}

			{
				{
					// <key attr.name="r" attr.type="int" for="edge" id="r"/>
					Element eKeyLabel = doc.createElement("key");
					eKeyLabel.setAttribute("attr.name", "r");
					eKeyLabel.setAttribute("attr.type", "int");
					eKeyLabel.setAttribute("for", "edge");
					eKeyLabel.setAttribute("id", "r");
					eGraphml.appendChild(eKeyLabel);
				}

				{
					// <key attr.name="g" attr.type="int" for="edge" id="g"/>
					Element eKeyLabel = doc.createElement("key");
					eKeyLabel.setAttribute("attr.name", "g");
					eKeyLabel.setAttribute("attr.type", "int");
					eKeyLabel.setAttribute("for", "edge");
					eKeyLabel.setAttribute("id", "g");
					eGraphml.appendChild(eKeyLabel);
				}

				{
					// <key attr.name="b" attr.type="int" for="edge" id="b"/>
					Element eKeyLabel = doc.createElement("key");
					eKeyLabel.setAttribute("attr.name", "b");
					eKeyLabel.setAttribute("attr.type", "int");
					eKeyLabel.setAttribute("for", "edge");
					eKeyLabel.setAttribute("id", "b");
					eGraphml.appendChild(eKeyLabel);
				}
			}

			Element eGraph = doc.createElement("graph");
			eGraph.setAttribute("id", id);
			eGraph.setAttribute("edgedefault", "directed");
			eGraphml.appendChild(eGraph);

			for (Node node : nodes)
			{
				Element eNode = doc.createElement("node");
				eNode.setAttribute("id", node.getId());
				eGraph.appendChild(eNode);

				// <data key="label">Node1 label</data>
				Element eDataKeyLabel = doc.createElement("data");
				eDataKeyLabel.setAttribute("key", "label");
				eDataKeyLabel.setTextContent(node.getLabel());
				eNode.appendChild(eDataKeyLabel);
			}
			for (Edge edge : edges)
			{
				Element eEdge = doc.createElement("edge");
				eEdge.setAttribute("source", edge.getFrom().getId());
				eEdge.setAttribute("target", edge.getTo().getId());
				/*
				{
					Element eR = doc.createElement("data");
					eR.setAttribute("key", "r");
					eR.setTextContent(Integer.toString(red));
					eEdge.appendChild(eR);
				}
				
				{
					Element eG = doc.createElement("data");
					eG.setAttribute("key", "g");
					eG.setTextContent(Integer.toString(green));
					eEdge.appendChild(eG);
				}
				
				{
					Element eB = doc.createElement("data");
					eB.setAttribute("key", "b");
					eB.setTextContent(Integer.toString(blue));
					eEdge.appendChild(eB);
				}
				*/
				eGraph.appendChild(eEdge);
			}

			DOMSource domSource = new DOMSource(doc);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult sr = new StreamResult(f);
			transformer.transform(domSource, sr);
		}
		catch (TransformerException | ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}

	}

}
