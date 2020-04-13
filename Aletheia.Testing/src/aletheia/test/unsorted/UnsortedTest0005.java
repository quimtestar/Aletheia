package aletheia.test.unsorted;

import aletheia.test.Test;
import aletheia.version.VersionManager;

public class UnsortedTest0005 extends Test
{

	@Override
	public void run() throws Exception
	{
		String version = VersionManager.getVersion();
		System.out.println(version);
	}

}
