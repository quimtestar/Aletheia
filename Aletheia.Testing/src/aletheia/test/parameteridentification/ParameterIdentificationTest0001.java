/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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
package aletheia.test.parameteridentification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.parameteridentification.protocol.ParameterIdentificationProtocol;
import aletheia.test.Test;

public class ParameterIdentificationTest0001 extends Test
{

	@Override
	public void run() throws Exception
	{
		ParameterIdentificationProtocol pip = new ParameterIdentificationProtocol(0);
		ParameterIdentification pi = ParameterIdentification.parse("<a,b -> () <c> >");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pip.send(new DataOutputStream(baos), pi);
		baos.close();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ParameterIdentification pi_ = pip.recv(new DataInputStream(bais));
		System.out.println(pi_);
	}

}
