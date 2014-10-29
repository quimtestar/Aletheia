/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.network.LocalRouterSet.LocalRouter;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.primitive.FloatProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;

public class CumulationSet
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final PeerToPeerNode peerToPeerNode;
	private final LocalRouterSet localRouterSet;

	public static abstract class Cumulation<V extends Cumulation.Value<V>> implements Exportable
	{
		@ExportableEnumInfo(availableVersions = 0)
		public enum Type implements ByteExportableEnum<Type>
		{
			//@formatter:off
			ExactCount((byte) 0, new ExactCountCumulation.SubProtocol(0),0),
			ApproximateCount((byte) 1, new ApproximateCountCumulation.SubProtocol(0),0),
			//@formatter:on
			;

			private final byte code;
			private final SubProtocol<? extends Cumulation<?>> subProtocol;
			private final int valueSubProtocolVersion;

			private Type(byte code, SubProtocol<? extends Cumulation<?>> subProtocol, int valueSubProtocolVersion)
			{
				this.code = code;
				this.subProtocol = subProtocol;
				this.valueSubProtocolVersion = valueSubProtocolVersion;
			}

			@Override
			public Byte getCode(int version)
			{
				return code;
			}

			public SubProtocol<? extends Cumulation<?>> getSubProtocol()
			{
				return subProtocol;
			}

			public int getValueSubProtocolVersion()
			{
				return valueSubProtocolVersion;
			}

			@ProtocolInfo(availableVersions = 0)
			public static class Protocol extends ByteExportableEnumProtocol<Type>
			{
				public Protocol(int requiredVersion)
				{
					super(0, Type.class, 0);
					checkVersionAvailability(Protocol.class, requiredVersion);
				}
			}
		}

		private final Type type;

		private Cumulation(Type type)
		{
			super();
			this.type = type;
		}

		public Type getType()
		{
			return type;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cumulation<?> other = (Cumulation<?>) obj;
			if (type != other.type)
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "Cumulation [type=" + type + "]";
		}

		public abstract V terminalValue();

		protected abstract Cumulation.Value.SubProtocol<V> valueSubProtocol(int requiredVersion);

		@ProtocolInfo(availableVersions = 0)
		public static class Protocol extends ExportableProtocol<Cumulation<? extends Cumulation.Value<?>>>
		{
			private final Type.Protocol typeProtocol = new Type.Protocol(0);

			public Protocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(Protocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, Cumulation<?> cumulation) throws IOException
			{
				typeProtocol.send(out, cumulation.type);
				cumulation.getType().getSubProtocol().sendCumulation(out, cumulation);
			}

			@Override
			public Cumulation<?> recv(DataInput in) throws IOException, ProtocolException
			{
				Type type = typeProtocol.recv(in);
				Cumulation<?> cumulation = type.getSubProtocol().recv(in);
				if (cumulation.getType() != type)
					throw new Error();
				return cumulation;
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				Type type = typeProtocol.recv(in);
				type.getSubProtocol().skip(in);
			}

		}

		@ProtocolInfo(availableVersions = 0)
		public static abstract class SubProtocol<C extends Cumulation<?>> extends ExportableProtocol<C>
		{

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@SuppressWarnings("unchecked")
			public void sendCumulation(DataOutput out, Cumulation<?> c) throws IOException
			{
				send(out, (C) c);
			}

			@Override
			public void send(DataOutput out, C t) throws IOException
			{
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
			}

		}

		public static abstract class Value<V extends Value<V>> implements Exportable
		{
			private final CumulationSet.Cumulation<V> cumulation;

			private Value(CumulationSet.Cumulation<V> cumulation)
			{
				super();
				this.cumulation = cumulation;
			}

			public CumulationSet.Cumulation<V> getCumulation()
			{
				return cumulation;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = 1;
				result = prime * result + ((cumulation == null) ? 0 : cumulation.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Value<?> other = (Value<?>) obj;
				if (cumulation == null)
				{
					if (other.cumulation != null)
						return false;
				}
				else if (!cumulation.equals(other.cumulation))
					return false;
				return true;
			}

			@Override
			public String toString()
			{
				return "CumulationValue [cumulation=" + cumulation + "]";
			}

			public abstract V combine(V other);

			@SuppressWarnings("unchecked")
			public final V combineValue(Value<?> other)
			{
				return combine((V) other);
			}

			public abstract boolean closeEnough(V other);

			@SuppressWarnings("unchecked")
			public final boolean closeEnoughValue(Value<?> other)
			{
				return closeEnough((V) other);
			}

			@ProtocolInfo(availableVersions = 0)
			public static class Protocol extends ExportableProtocol<Value<?>>
			{
				private final CumulationSet.Cumulation.Protocol cumulationProtocol = new CumulationSet.Cumulation.Protocol(0);

				public Protocol(int requiredVersion)
				{
					super(0);
					checkVersionAvailability(Protocol.class, requiredVersion);
				}

				@Override
				public void send(DataOutput out, Value<?> value) throws IOException
				{
					Cumulation<?> cumulation = value.getCumulation();
					cumulationProtocol.send(out, cumulation);
					cumulation.valueSubProtocol(cumulation.getType().getValueSubProtocolVersion()).sendValue(out, value);
				}

				@Override
				public Value<?> recv(DataInput in) throws IOException, ProtocolException
				{
					CumulationSet.Cumulation<?> cumulation = cumulationProtocol.recv(in);
					Value<?> value = cumulation.valueSubProtocol(cumulation.getType().getValueSubProtocolVersion()).recv(in);
					if (!value.getCumulation().equals(cumulation))
						throw new Error();
					return value;
				}

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
					CumulationSet.Cumulation<?> cumulation = cumulationProtocol.recv(in);
					cumulation.valueSubProtocol(cumulation.getType().getValueSubProtocolVersion()).skip(in);
				}
			}

			@ProtocolInfo(availableVersions = 0)
			public abstract static class SubProtocol<V extends Value<V>> extends ExportableProtocol<V>
			{
				private final CumulationSet.Cumulation<V> cumulation;

				public SubProtocol(int requiredVersion, CumulationSet.Cumulation<V> cumulation)
				{
					super(0);
					checkVersionAvailability(SubProtocol.class, requiredVersion);
					this.cumulation = cumulation;
				}

				protected CumulationSet.Cumulation<V> getCumulation()
				{
					return cumulation;
				}

				@SuppressWarnings("unchecked")
				public void sendValue(DataOutput out, Value<?> v) throws IOException
				{
					send(out, (V) v);
				}

				@Override
				public void send(DataOutput out, V v) throws IOException
				{
				}

				@Override
				public abstract V recv(DataInput in) throws IOException, ProtocolException;

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
				}
			}

		}

	}

	public static abstract class CountCumulation<V extends CountCumulation.Value<V>> extends Cumulation<V>
	{

		private CountCumulation(Type type)
		{
			super(type);
		}

		@Override
		public String toString()
		{
			return "AbstractCountCumulation";
		}

		@Override
		public abstract V terminalValue();

		@Override
		protected abstract CountCumulation.Value.SubProtocol<V> valueSubProtocol(int requiredVersion);

		@ProtocolInfo(availableVersions = 0)
		public static abstract class SubProtocol<C extends CountCumulation<?>> extends Cumulation.SubProtocol<C>
		{

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			public abstract C recv(DataInput in) throws IOException, ProtocolException;

		}

		public abstract static class Value<V extends Value<V>> extends CumulationSet.Cumulation.Value<V>
		{
			private final int count;

			private Value(CumulationSet.CountCumulation<V> cumulation, int count)
			{
				super(cumulation);
				this.count = count;
			}

			@Override
			public CumulationSet.CountCumulation<V> getCumulation()
			{
				return (CumulationSet.CountCumulation<V>) super.getCumulation();
			}

			public int getCount()
			{
				return count;
			}

			@Override
			public String toString()
			{
				return "AbstractCountCumulationValue [count=" + count + "]";
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + count;
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj))
					return false;
				if (getClass() != obj.getClass())
					return false;
				Value<?> other = (Value<?>) obj;
				if (count != other.count)
					return false;
				return true;
			}

			@Override
			public abstract V combine(V other);

			@Override
			public abstract boolean closeEnough(V other);

			@ProtocolInfo(availableVersions = 0)
			public static abstract class SubProtocol<V extends Value<V>> extends CumulationSet.Cumulation.Value.SubProtocol<V>
			{
				private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

				public SubProtocol(int requiredVersion, CumulationSet.CountCumulation<V> cumulation)
				{
					super(0, cumulation);
					checkVersionAvailability(SubProtocol.class, requiredVersion);
				}

				@Override
				protected CumulationSet.CountCumulation<V> getCumulation()
				{
					return (CumulationSet.CountCumulation<V>) super.getCumulation();
				}

				protected abstract V recv(int count, DataInput in);

				@Override
				public final V recv(DataInput in) throws IOException, ProtocolException
				{
					int count = integerProtocol.recv(in);
					return recv(count, in);
				}

				@Override
				public void send(DataOutput out, V v) throws IOException
				{
					super.send(out, v);
					integerProtocol.send(out, v.getCount());
				}

				@Override
				public void skip(DataInput in) throws IOException, ProtocolException
				{
					super.skip(in);
					integerProtocol.skip(in);
				}

			}
		}

	}

	public static class ExactCountCumulation extends CountCumulation<ExactCountCumulation.Value>
	{

		public ExactCountCumulation()
		{
			super(Type.ExactCount);
		}

		@Override
		public String toString()
		{
			return "CountCumulation";
		}

		@Override
		public ExactCountCumulation.Value terminalValue()
		{
			return new ExactCountCumulation.Value(this, 1);
		}

		@Override
		protected ExactCountCumulation.Value.SubProtocol valueSubProtocol(int requiredVersion)
		{
			return new ExactCountCumulation.Value.SubProtocol(requiredVersion, this);
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends CountCumulation.SubProtocol<ExactCountCumulation>
		{

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			public ExactCountCumulation recv(DataInput in) throws IOException, ProtocolException
			{
				return new ExactCountCumulation();
			}

		}

		public static class Value extends CumulationSet.CountCumulation.Value<Value>
		{
			private Value(CumulationSet.ExactCountCumulation cumulation, int count)
			{
				super(cumulation, count);
			}

			@Override
			public CumulationSet.ExactCountCumulation getCumulation()
			{
				return (CumulationSet.ExactCountCumulation) super.getCumulation();
			}

			@Override
			public String toString()
			{
				return "CountValue [count=" + getCount() + "]";
			}

			@Override
			public Value combine(Value other)
			{
				return new Value(getCumulation(), getCount() + other.getCount());
			}

			@Override
			public boolean closeEnough(Value other)
			{
				return getCount() == other.getCount();
			}

			@ProtocolInfo(availableVersions = 0)
			public static class SubProtocol extends CumulationSet.CountCumulation.Value.SubProtocol<Value>
			{

				public SubProtocol(int requiredVersion, CumulationSet.ExactCountCumulation cumulation)
				{
					super(0, cumulation);
					checkVersionAvailability(SubProtocol.class, requiredVersion);
				}

				@Override
				protected CumulationSet.ExactCountCumulation getCumulation()
				{
					return (CumulationSet.ExactCountCumulation) super.getCumulation();
				}

				@Override
				protected Value recv(int count, DataInput in)
				{
					return new Value(getCumulation(), count);
				}

			}
		}

	}

	public static class ApproximateCountCumulation extends CountCumulation<ApproximateCountCumulation.Value>
	{
		private final float tolerance;

		private ApproximateCountCumulation(float tolerance)
		{
			super(Type.ApproximateCount);
			this.tolerance = tolerance;
		}

		public float getTolerance()
		{
			return tolerance;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Float.floatToIntBits(tolerance);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ApproximateCountCumulation other = (ApproximateCountCumulation) obj;
			if (Float.floatToIntBits(tolerance) != Float.floatToIntBits(other.tolerance))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "ApproximateCountCumulation [tolerance=" + tolerance + "]";
		}

		@Override
		public ApproximateCountCumulation.Value terminalValue()
		{
			return new ApproximateCountCumulation.Value(this, 1);
		}

		@Override
		protected ApproximateCountCumulation.Value.SubProtocol valueSubProtocol(int requiredVersion)
		{
			return new ApproximateCountCumulation.Value.SubProtocol(requiredVersion, this);
		}

		@ProtocolInfo(availableVersions = 0)
		public static class SubProtocol extends CountCumulation.SubProtocol<ApproximateCountCumulation>
		{
			private final FloatProtocol floatProtocol = new FloatProtocol(0);

			protected SubProtocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(SubProtocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, ApproximateCountCumulation t) throws IOException
			{
				super.send(out, t);
				floatProtocol.send(out, t.getTolerance());
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				super.skip(in);
				floatProtocol.skip(in);
			}

			@Override
			public ApproximateCountCumulation recv(DataInput in) throws IOException, ProtocolException
			{
				float tolerance = floatProtocol.recv(in);
				return new ApproximateCountCumulation(tolerance);
			}

		}

		public static class Value extends CumulationSet.CountCumulation.Value<Value>
		{
			private Value(CumulationSet.ApproximateCountCumulation cumulation, int count)
			{
				super(cumulation, count);
			}

			@Override
			public CumulationSet.ApproximateCountCumulation getCumulation()
			{
				return (CumulationSet.ApproximateCountCumulation) super.getCumulation();
			}

			public float getTolerance()
			{
				return getCumulation().getTolerance();
			}

			@Override
			public String toString()
			{
				return "ApproximateCountCumulationValue [getTolerance()=" + getTolerance() + ", getCount()=" + getCount() + "]";
			}

			@Override
			public Value combine(Value other)
			{
				return new Value(getCumulation(), getCount() + other.getCount());
			}

			private float toleratedLow()
			{
				return getCount() * (1 - getTolerance());
			}

			private float toleratedHigh()
			{
				return getCount() / (1 - getTolerance());
			}

			@Override
			public boolean closeEnough(Value other)
			{
				return equals(other) || (toleratedLow() <= other.getCount() && other.getCount() <= toleratedHigh());
			}

			@ProtocolInfo(availableVersions = 0)
			public static class SubProtocol extends CumulationSet.CountCumulation.Value.SubProtocol<Value>
			{

				public SubProtocol(int requiredVersion, CumulationSet.ApproximateCountCumulation cumulation)
				{
					super(0, cumulation);
					checkVersionAvailability(SubProtocol.class, requiredVersion);
				}

				@Override
				protected CumulationSet.ApproximateCountCumulation getCumulation()
				{
					return (CumulationSet.ApproximateCountCumulation) super.getCumulation();
				}

				@Override
				protected Value recv(int count, DataInput in)
				{
					return new Value(getCumulation(), count);
				}

			}
		}

	}

	private final static ExactCountCumulation exactCountCumulation = new ExactCountCumulation();
	private final static ApproximateCountCumulation approximateCountCumulation = new ApproximateCountCumulation(0.5f);

	private final static Set<Cumulation<?>> cumulations = Collections.unmodifiableSet(new HashSet<Cumulation<?>>(Arrays
			.<Cumulation<?>> asList(approximateCountCumulation)));

	public static Set<Cumulation<?>> getCumulations()
	{
		return cumulations;
	}

	private final ArrayList<Map<Cumulation<?>, Cumulation.Value<?>>> neighbourCumulationValues;
	private final ArrayList<Map<Cumulation<?>, Cumulation.Value<?>>> routerCumulationValues;
	private final ArrayList<NetworkPhase> lastNeighbourList;

	private class LocalRouterSetListener implements LocalRouterSet.Listener
	{

		@Override
		public void changed()
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{
					updateLocalRouterSet();
				}
			});
		}

	}

	public CumulationSet(PeerToPeerNode peerToPeerNode, LocalRouterSet localRouterSet)
	{
		super();
		this.peerToPeerNode = peerToPeerNode;
		this.localRouterSet = localRouterSet;
		this.neighbourCumulationValues = new ArrayList<Map<Cumulation<?>, Cumulation.Value<?>>>();
		this.routerCumulationValues = new ArrayList<Map<Cumulation<?>, Cumulation.Value<?>>>();
		this.lastNeighbourList = new ArrayList<NetworkPhase>();
		localRouterSet.addListener(new LocalRouterSetListener());
		updateLocalRouterSet();
	}

	private UUID getNodeUuid()
	{
		return peerToPeerNode.getNodeUuid();
	}

	private static <T> T getCumulationValue(ArrayList<Map<Cumulation<?>, T>> arrayList, int i, Cumulation<?> cumulation)
	{
		if (i >= arrayList.size())
			return null;
		Map<Cumulation<?>, T> map = arrayList.get(i);
		if (map == null)
			return null;
		return map.get(cumulation);
	}

	private static <T> T putCumulationValue(ArrayList<Map<Cumulation<?>, T>> arrayList, int i, Cumulation<?> cumulation, T value)
	{
		if (i >= arrayList.size())
		{
			arrayList.ensureCapacity(i + 1);
			while (arrayList.size() <= i)
				arrayList.add(null);
		}
		Map<Cumulation<?>, T> map = arrayList.get(i);
		if (map == null)
		{
			map = new HashMap<Cumulation<?>, T>();
			arrayList.set(i, map);
		}
		return map.put(cumulation, value);
	}

	public synchronized int getNeighbourCumulationValuesSize()
	{
		return neighbourCumulationValues.size();
	}

	@SuppressWarnings("unchecked")
	public synchronized <V extends Cumulation.Value<V>> V getNeighbourCumulationValue(int i, Cumulation<V> cumulation)
	{
		return (V) getCumulationValue(neighbourCumulationValues, i, cumulation);
	}

	private synchronized Cumulation.Value<?> putNeighbourCumulationValue(int i, Cumulation.Value<?> cumulationValue)
	{
		return putCumulationValue(neighbourCumulationValues, i, cumulationValue.getCumulation(), cumulationValue);
	}

	private synchronized Cumulation.Value<?> clearNeighbourCumulationValue(int i, Cumulation<?> cumulation)
	{
		logger.debug(getNodeUuid() + " clearNeighbourCumulationValue() -> i: " + i + " cumulation:" + cumulation);
		return putCumulationValue(neighbourCumulationValues, i, cumulation, null);
	}

	public synchronized int getRouterCumulationValuesSize()
	{
		return routerCumulationValues.size();
	}

	@SuppressWarnings("unchecked")
	public synchronized <V extends Cumulation.Value<V>> V getRouterCumulationValue(int i, Cumulation<V> cumulation)
	{
		return (V) getCumulationValue(routerCumulationValues, i, cumulation);
	}

	public synchronized Collection<Cumulation.Value<?>> routerCumulationValues(final int i, Collection<Cumulation<?>> cumulations)
	{
		return new BufferedList<>(new BijectionCollection<>(new Bijection<Cumulation<?>, Cumulation.Value<?>>()
		{

			@Override
			public Cumulation.Value<?> forward(Cumulation<?> cumulation)
			{
				return getRouterCumulationValue(i, cumulation);
			}

			@Override
			public Cumulation<?> backward(Cumulation.Value<?> value)
			{
				return value.getCumulation();
			}
		}, cumulations));
	}

	private synchronized Cumulation.Value<?> putRouterCumulationValue(int i, Cumulation.Value<?> cumulationValue)
	{
		logger.debug(getNodeUuid() + " putRouterCumulationValue() -> i: " + i + " cumulationValue:" + cumulationValue);
		Cumulation.Value<?> old = putCumulationValue(routerCumulationValues, i, cumulationValue.getCumulation(), cumulationValue);
		if (!cumulationValue.equals(old))
			synchronized (localRouterSet)
			{
				for (NetworkPhase neighbour : localRouterSet.neighbourCollection(i + 1))
				{
					logger.debug(getNodeUuid() + " putRouterCumulationValue() -> " + " neighbour: " + neighbour.getPeerNodeUuid()
							+ " neighbour.updateRouterCumulationValue(" + i + ", " + cumulationValue + ")");
					neighbour.updateRouterCumulationValue(i, cumulationValue);
				}
				if (i > 0)
				{
					NetworkPhase neighbour = localRouterSet.getNeighbour(i - 1);
					if (neighbour != null)
					{
						logger.debug(getNodeUuid() + " putRouterCumulationValue() -> " + " neighbour: " + neighbour.getPeerNodeUuid()
								+ " neighbour.updateNeighbourCumulationValue(" + cumulationValue + ")");
						neighbour.updateNeighbourCumulationValue(cumulationValue);
					}
				}
			}
		return old;
	}

	private synchronized Cumulation.Value<?> clearRouterCumulationValue(int i, Cumulation<?> cumulation)
	{
		logger.debug(getNodeUuid() + " clearRouterCumulationValue() -> i: " + i + " cumulation:" + cumulation);
		Cumulation.Value<?> old = putCumulationValue(routerCumulationValues, i, cumulation, null);
		if (old != null)
			synchronized (localRouterSet)
			{
				for (NetworkPhase neighbour : localRouterSet.neighbourCollection(i + 1))
				{
					logger.debug(getNodeUuid() + " clearRouterCumulationValue() -> " + " neighbour: " + neighbour.getPeerNodeUuid()
							+ " neighbour.removeRouterCumulationValue(" + i + ", " + cumulation + ")");
					neighbour.removeRouterCumulationValue(i, cumulation);
				}
				if (i > 0)
				{
					NetworkPhase neighbour = localRouterSet.getNeighbour(i - 1);
					if (neighbour != null)
					{
						Cumulation.Value<?> value = cumulation.terminalValue();
						logger.debug(getNodeUuid() + " clearRouterCumulationValue() -> " + " neighbour: " + neighbour.getPeerNodeUuid()
								+ " neighbour.updateNeighbourCumulationValue(" + value + ")");
						neighbour.updateNeighbourCumulationValue(value);
					}
				}
			}
		return old;
	}

	private synchronized NetworkPhase getLastNeighbour(int i)
	{
		if (i >= lastNeighbourList.size())
			return null;
		return lastNeighbourList.get(i);
	}

	private synchronized NetworkPhase putLastNeighbour(int i, NetworkPhase neighbour)
	{
		if (i >= lastNeighbourList.size())
		{
			lastNeighbourList.ensureCapacity(i + 1);
			while (lastNeighbourList.size() <= i)
				lastNeighbourList.add(null);
		}
		return lastNeighbourList.set(i, neighbour);
	}

	private synchronized void updateNeighbourCumulationValue(int i, Cumulation.Value<?> cumulationValue)
	{
		Cumulation.Value<?> old = getNeighbourCumulationValue(i, cumulationValue.getCumulation());
		if (!cumulationValue.equals(old))
		{
			synchronized (localRouterSet)
			{
				putNeighbourCumulationValue(i, cumulationValue);
				Cumulation.Value<?> otherValue;
				if (i < localRouterSet.lastNeighbourIndex())
					otherValue = getRouterCumulationValue(i + 1, cumulationValue.getCumulation());
				else
					otherValue = cumulationValue.getCumulation().terminalValue();
				if (otherValue != null)
				{
					Cumulation.Value<?> combined = otherValue.combineValue(cumulationValue);
					updateRouterCumulationValue(i, combined);
				}
			}
		}
	}

	public synchronized void updateNeighbourCumulationValue(NetworkPhase networkPhase, Cumulation.Value<?> cumulationValue)
	{
		logger.debug(getNodeUuid() + " updateNeighbourCumulationValue() -> networkPhase: " + networkPhase.getPeerNodeUuid() + " cumulationValue:"
				+ cumulationValue);
		if (cumulations.contains(cumulationValue.getCumulation()))
			synchronized (localRouterSet)
			{
				int index = localRouterSet.neighbourIndex(networkPhase);
				if (index >= 0)
				{
					logger.debug(getNodeUuid() + " updateNeighbourCumulationValue() -> " + "neighbour.updateNeighbourCumulationValue(" + index + ", "
							+ cumulationValue + ")");
					updateNeighbourCumulationValue(index, cumulationValue);
				}
			}
	}

	private synchronized void updateRouterCumulationValue(int i, Cumulation.Value<?> cumulationValue)
	{
		logger.debug(getNodeUuid() + " updateRouterCumulationValue() -> i: " + i + " cumulationValue:" + cumulationValue);
		synchronized (localRouterSet)
		{
			while (true)
			{
				Cumulation.Value<?> old = getRouterCumulationValue(i, cumulationValue.getCumulation());
				if (old != null && old.closeEnoughValue(cumulationValue))
					break;
				putRouterCumulationValue(i, cumulationValue);
				i--;
				if (i < 0)
					break;
				Cumulation.Value<?> otherValue = getNeighbourCumulationValue(i, cumulationValue.getCumulation());
				if (otherValue != null)
					cumulationValue = cumulationValue.combineValue(otherValue);
				else
				{
					LocalRouter localRouter = localRouterSet.getRouter(i);
					if (localRouter != null && !localRouter.isEmpty())
						break;
				}
			}
		}
	}

	private synchronized void removeRouterCumulationValue(int i, Cumulation<?> cumulation)
	{
		logger.debug(getNodeUuid() + " removeNeighbourCumulationValue() -> i: " + i + " cumulation:" + cumulation);
		synchronized (localRouterSet)
		{
			while (true)
			{
				clearRouterCumulationValue(i, cumulation);
				i--;
				if (i < 0)
					break;
				Cumulation.Value<?> otherValue = getNeighbourCumulationValue(i, cumulation);
				if (otherValue != null)
				{
					updateRouterCumulationValue(i, cumulation.terminalValue().combineValue(otherValue));
					break;
				}
				else
				{
					LocalRouter localRouter = localRouterSet.getRouter(i);
					if (localRouter != null && !localRouter.isEmpty())
						break;
				}
			}
		}
	}

	public synchronized void updateRouterCumulationValue(NetworkPhase neighbour, int i, Cumulation.Value<?> cumulationValue)
	{
		logger.debug(getNodeUuid() + " updateRouterCumulationValue() -> networkPhase: " + neighbour.getPeerNodeUuid() + " i: " + i + " cumulationValue:"
				+ cumulationValue);
		if (cumulations.contains(cumulationValue.getCumulation()))
			synchronized (localRouterSet)
			{
				LocalRouter localRouter = localRouterSet.getRouter(i);
				if (localRouter != null && localRouter.getNeighbours().contains(neighbour))
					updateRouterCumulationValue(i, cumulationValue);
			}
	}

	public synchronized void removeRouterCumulationValue(NetworkPhase neighbour, int i, Cumulation<?> cumulation)
	{
		logger.debug(getNodeUuid() + " removeRouterCumulationValue() -> networkPhase: " + neighbour.getPeerNodeUuid() + " i: " + i + " cumulation:"
				+ cumulation);
		if (cumulations.contains(cumulation))
			synchronized (localRouterSet)
			{
				LocalRouter localRouter = localRouterSet.getRouter(i);
				if (localRouter != null && localRouter.getNeighbours().contains(neighbour))
				{
					removeRouterCumulationValue(i, cumulation);
					NetworkPhase neighbour_ = localRouter.randomNeighbour();
					if (neighbour_ != null)
					{
						logger.debug(getNodeUuid() + " updateLocalRouterSet() -> neighbour: " + neighbour_.getNodeUuid() + " requestRouterCumulationValue(" + i
								+ ")");
						neighbour_.requestRouterCumulationValue(i);
					}
				}
			}
	}

	private synchronized void updateLocalRouterSet()
	{
		synchronized (localRouterSet)
		{
			logger.debug(getNodeUuid() + " updateLocalRouterSet()");
			int lastNeighbourIndex = localRouterSet.lastNeighbourIndex();
			List<NetworkPhase> neighbours = localRouterSet.getNeighbours();
			for (int i = 0; i < neighbours.size(); i++)
			{
				logger.debug(getNodeUuid() + " updateLocalRouterSet() -> i: " + i);
				NetworkPhase lastNeighbour = getLastNeighbour(i);
				NetworkPhase neighbour = neighbours.get(i);
				putLastNeighbour(i, neighbour);
				if ((neighbour != null) && !neighbour.equals(lastNeighbour))
				{
					logger.debug(getNodeUuid() + " updateLocalRouterSet() -> new neighbour: " + neighbour.getPeerNodeUuid());
					for (Cumulation<?> cumulation : cumulations)
					{
						{
							Cumulation.Value<?> value;
							if (i >= lastNeighbourIndex)
								value = cumulation.terminalValue();
							else
								value = getRouterCumulationValue(i + 1, cumulation);
							if (value != null)
							{
								logger.debug(getNodeUuid() + " updateLocalRouterSet() -> " + "neighbour.updateNeighbourCumulationValue(" + value + ")");
								neighbour.updateNeighbourCumulationValue(value);
							}
						}
						for (int j = 0; j < i; j++)
						{
							Cumulation.Value<?> cumulationValue = getRouterCumulationValue(j, cumulation);
							if (cumulationValue != null)
							{
								logger.debug(getNodeUuid() + " updateLocalRouterSet() -> " + "neighbour.updateRouterCumulationValue(" + j + ", "
										+ cumulationValue + ")");
								neighbour.updateRouterCumulationValue(j, cumulationValue);
							}
						}
					}
				}
				else if (neighbour == null)
				{
					if (lastNeighbour != null)
					{
						logger.debug(getNodeUuid() + " updateLocalRouterSet() -> removed neighbour: " + lastNeighbour.getPeerNodeUuid());
						for (Cumulation<?> cumulation : cumulations)
							clearNeighbourCumulationValue(i, cumulation);
					}
					LocalRouter router = localRouterSet.getRouter(i);
					if (router == null)
					{
						logger.debug(getNodeUuid() + " updateLocalRouterSet() -> no router");
						for (Cumulation<?> cumulation : cumulations)
						{
							Cumulation.Value<?> value = getRouterCumulationValue(i + 1, cumulation);
							if (value == null)
								removeRouterCumulationValue(i, cumulation);
							else
								updateRouterCumulationValue(i, value);
						}
					}
					else
					{
						logger.debug(getNodeUuid() + " updateLocalRouterSet() -> router");
						NetworkPhase neighbour_ = router.randomNeighbour();
						if (neighbour_ != null)
						{
							logger.debug(getNodeUuid() + " updateLocalRouterSet() -> neighbour: " + neighbour_.getPeerNodeUuid()
									+ " requestRouterCumulationValue(" + i + ")");
							neighbour_.requestRouterCumulationValue(i);
						}
					}
				}
			}
		}
	}

	public synchronized void requestRouterCumulationValuesMessage(NetworkPhase neighbour, int index)
	{
		logger.debug(getNodeUuid() + " requestRouterCumulationValuesMessage() -> neighbour: " + neighbour.getPeerNodeUuid() + " index:" + index);
		for (Cumulation<?> cumulation : cumulations)
		{
			Cumulation.Value<?> cumulationValue = getRouterCumulationValue(index, cumulation);
			if (cumulationValue != null)
			{
				logger.debug(getNodeUuid() + " updateLocalRouterSet() -> " + "neighbour.updateRouterCumulationValue(" + index + ", " + cumulationValue + ")");
				neighbour.updateRouterCumulationValue(index, cumulationValue);
			}
		}
	}

	public static boolean isCumulationSupported(Cumulation<?> cumulation)
	{
		return cumulations.contains(cumulation);
	}

	public synchronized <V extends Cumulation.Value<V>> V getCumulationValue(Cumulation<V> cumulation)
	{
		if (!isCumulationSupported(cumulation))
			throw new UnsupportedOperationException();
		V value = getRouterCumulationValue(0, cumulation);
		if (value == null)
			return cumulation.terminalValue();
		return value;
	}

	public <V extends CountCumulation.Value<V>> int getCountCumulationValue(CountCumulation<V> cumulation)
	{
		return getCumulationValue(cumulation).getCount();
	}

	public int getExactCountCumulationValue()
	{
		return getCountCumulationValue(exactCountCumulation);
	}

	public int getApproximateCountCumulationValue()
	{
		return getCountCumulationValue(approximateCountCumulation);
	}

}
