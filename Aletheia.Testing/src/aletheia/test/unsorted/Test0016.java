package aletheia.test.unsorted;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.protocol.parameteridentification.ParameterIdentificationProtocol;
import aletheia.test.Test;

public class Test0016 extends Test
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
