package aletheia.parsergenerator;

public class LocationInterval
{
	public final Location start;
	public final Location stop;

	public LocationInterval(Location start, Location stop)
	{
		super();
		this.start = start;
		this.stop = stop;
	}

	public LocationInterval(Location location)
	{
		this(location, location);
	}

	public String position()
	{
		return start.position();
	}

	@Override
	public String toString()
	{
		return "[" + start + "]-[" + stop + "]";
	}
}