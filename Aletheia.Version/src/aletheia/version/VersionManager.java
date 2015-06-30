package aletheia.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VersionManager
{
	private final static VersionManager instance;
	static
	{
		try
		{
			instance = new VersionManager();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private final String version;

	private VersionManager() throws IOException
	{
		InputStream is = ClassLoader.getSystemResourceAsStream("aletheia/version/version.txt");
		if (is == null)
			version = "*NULL*";
		else
		{
			try
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				version = br.readLine();
			}
			finally
			{
				is.close();
			}
		}
	}

	public static String getVersion()
	{
		return instance.version;
	}

}
