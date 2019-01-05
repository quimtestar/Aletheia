/*******************************************************************************
 * Copyright (c) 2019 Quim Testar
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
package aletheia.test.unsorted;

import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import aletheia.test.Test;

public class Test0028 extends Test
{

	@Override
	public void run() throws Exception
	{
		Path path = FileSystems.getDefault().getPath("/mnt/vlb0/quim/Aletheia/aletheiadb_temp/prova");
		FileStore fileStore = Files.getFileStore(path);
		long usableSpace = fileStore.getUsableSpace();
		System.out.println(usableSpace);
	}

}
