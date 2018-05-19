/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.test;

import java.io.Console;
import java.io.File;
import java.util.prefs.BackingStoreException;

import aletheia.preferences.NodeAletheiaPreferences;
import aletheia.preferences.RootAletheiaPreferences;

public class TestingAletheiaPreferences extends NodeAletheiaPreferences
{
	private final static String NODE_PATH = "Testing";

	private final static String DB_FILE_NAME = "db_file_name";

	public final static TestingAletheiaPreferences instance = new TestingAletheiaPreferences();

	private TestingAletheiaPreferences()
	{
		super(RootAletheiaPreferences.instance, NODE_PATH);
	}

	public File getDbFile()
	{
		String dbFileName = getPreferences().get(DB_FILE_NAME, null);
		if (dbFileName == null)
			return null;
		return new File(dbFileName);
	}

	public void setDbFile(File dbFile)
	{
		if (dbFile != null)
			getPreferences().put(DB_FILE_NAME, dbFile.getAbsolutePath());
		else
			getPreferences().remove(DB_FILE_NAME);
	}

	public void configure() throws BackingStoreException
	{
		Console console = System.console();
		if (console == null)
			throw new RuntimeException("Can't access to console");
		String dbFileName = getDbFile() != null ? getDbFile().toString() : "";
		dbFileName = console.readLine("Db file path [%s]:", dbFileName).trim();
		setDbFile((dbFileName != null && !dbFileName.isEmpty()) ? new File(dbFileName) : getDbFile());
	}

	public static class Configure
	{
		public static void main(String[] args) throws Exception
		{
			TestingAletheiaPreferences.instance.configure();
		}

	}

}
