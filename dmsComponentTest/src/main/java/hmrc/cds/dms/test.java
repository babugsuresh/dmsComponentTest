package hmrc.cds.dms;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class test {

	public static void main(String[] args) throws IOException {
		
		String s = "GuaranteeManagementServiceInboundSOAP_v2.wsdl";
		if(s.contains("SOAP")) {
			System.out.println(s.substring(0, s.lastIndexOf(".")).replaceAll("\\SOAP.*?\\b", ""));
		}else {
			
			if (s.contains("Inbound")) {
				System.out.println(s.substring(0, s.lastIndexOf(".")).replaceAll("\\SOAP.*?\\b", ""));
			}
		}
		
	    

	}

}
