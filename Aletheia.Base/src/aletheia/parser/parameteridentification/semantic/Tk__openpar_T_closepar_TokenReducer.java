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
package aletheia.parser.parameteridentification.semantic;

import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.TrivialProductionTokenPayloadReducer;

@AssociatedProduction(left = "Tk", right =
{ "openpar", "T", "closepar" })
public class Tk__openpar_T_closepar_TokenReducer extends TrivialProductionTokenPayloadReducer<Void, ParameterIdentification>
{
	public Tk__openpar_T_closepar_TokenReducer()
	{
		super(1);
	}

}
