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
package aletheia.persistence.berkeleydb.proxies.parameteridentification;

import com.sleepycat.persist.model.Persistent;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;

@Persistent(proxyFor = FunctionParameterIdentification.class, version = 0)
public class FunctionParameterIdentificationProxy extends ParameterIdentificationProxy<FunctionParameterIdentification>
{
	private Identifier parameter;
	private ParameterIdentification domain;
	private ParameterIdentification body;

	@Override
	public void initializeProxy(FunctionParameterIdentification functionParameterIdentification)
	{
		super.initializeProxy(functionParameterIdentification);
		parameter = functionParameterIdentification.getParameter();
		domain = functionParameterIdentification.getDomain();
		body = functionParameterIdentification.getBody();
	}

	@Override
	public FunctionParameterIdentification convertProxy()
	{
		return new FunctionParameterIdentification(parameter, domain, body);
	}

}
