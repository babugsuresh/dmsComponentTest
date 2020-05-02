package hmrc.cds.dms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Stream;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ibm.wsdl.PortTypeImpl;

@SpringBootApplication
public class DmsComponentTestApplication implements CommandLineRunner {

	public static Map<String, String> serviceTyeps;
	static {
		serviceTyeps = new HashMap<>();
		serviceTyeps.put("calculator_1.wsdl", "PDS");
		serviceTyeps.put("numberconversion_1.wsdl", "XRS");
		serviceTyeps.put("Test1", "ILMS");
		serviceTyeps.put("Test1", "Tariff");
	}

	@Autowired
	private YAMLConfig myConfig;

	private static final String BASEDIR = null;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DmsComponentTestApplication.class);
		System.out.println("\n----JESUS is my GOD----");
		app.run();
	}

	public void run(String... args) throws UnsupportedOperationException, SOAPException, IOException, Exception {
		/*
		 * System.out.println("using environment: " + myConfig.getEnvironment());
		 * System.out.println("name: " + myConfig.getPdsURI());
		 * System.out.println("servers: " + myConfig.getTariffURI());
		 */

		List<String> envs = myConfig.getEnvironment();

		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();

		File wsdlDir = new File(BASEDIR, "src/main/resources/wsdls/99 Adapters");

		String[] wsdlNames;
		wsdlNames = wsdlDir.list();
		for (String wsdlName : wsdlNames) {

			// Print the names of files and directories
			System.out.println(wsdlName);
			File wsdlFile = new File(wsdlDir, wsdlName);

			String endpoint = null;
			String soapenvelope = null;
			for (String env : envs) {
				System.out.println("env Executing for ---: " + env);

				String systemtocall = null;

				for (Map.Entry<String, String> entry : serviceTyeps.entrySet()) {
					if (entry.getKey().equalsIgnoreCase(wsdlName)) {
						systemtocall = entry.getValue();
						System.out.println("systemtocall: " + systemtocall);

					}
				}

				if (systemtocall.equalsIgnoreCase("PDS")) {
					endpoint = getPdsendpoint(env, envs.indexOf(env));
					endpoint = endpoint+myConfig.getPdsURI();

				} else if (systemtocall.equalsIgnoreCase("ILMS")) {
					endpoint = getIlamsendpoint(env, envs.indexOf(env));
					endpoint = endpoint+myConfig.getIlmsURI();

				} else if (systemtocall.equalsIgnoreCase("Tariff")) {
					endpoint = getTariffendpoint(env, envs.indexOf(env));
					endpoint = endpoint+myConfig.getTariffURI();

				} else if (systemtocall.equalsIgnoreCase("XRS")) {
					endpoint = getXrsendpoint(env, envs.indexOf(env));
					endpoint = endpoint+myConfig.getXrsURI();

				}

				List<Operation> operationList = getPortTypeOperations(wsdlFile.toURI().toString());

				// ListIterator<Operation> listIterator = operationList.listIterator();
				System.out.println("operationList Length:" + operationList.size());

				for (Operation opname : operationList) {
					System.out.println("\nOperationName: " + opname.getName());
					soapenvelope = getRequestEnvelopeToString(opname.getName());
					System.setProperty("java.net.useSystemProxies", "true");
					//System.out.println("SOAPenvelopetoString: " + soapenvelope);

					// call soap here
					
					System.out.println("SOAP endpoint: " + endpoint);
					
					InputStream is = new ByteArrayInputStream(soapenvelope.getBytes());
				    SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
				    MimeHeaders headers = request.getMimeHeaders();
				    
				    headers.setHeader("Content-Type", "application/xml");
				    request.saveChanges();
				    

				    SOAPMessage soapResponse = soapConnection.call(request, endpoint);

				   //System.out.println(printSOAPResponse(soapResponse));
				   
				   TransformerFactory transformerFactory = TransformerFactory.newInstance();
				    Transformer transformer = transformerFactory.newTransformer();
				    Source sourceContent = soapResponse.getSOAPPart().getContent();
				    System.out.print("\nResponse SOAP Message = ");
				    StreamResult result = new StreamResult(System.out);
				    transformer.transform(sourceContent, result);
					
				    //System.out.println("Response Suresh: "+sourceContent);
					
				}

				

			}

		}

	}
	
	
//	private static void printSOAPResponse(SOAPMessage soapResponse) throws Exception {
//	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
//	    Transformer transformer = transformerFactory.newTransformer();
//	    Source sourceContent = soapResponse.getSOAPPart().getContent();
//	    System.out.print("\nResponse SOAP Message = ");
//	    StreamResult result = new StreamResult(System.out);
//	    transformer.transform(sourceContent, result);
//	}

	private String getPdsendpoint(String env, int index) {
		String endpoint = null;
		List<String> pdshosts = myConfig.getPDSHosts();
		for (String pdshost : pdshosts) {
			if (index == pdshosts.indexOf(pdshost)) {
				//System.out.println("pdshost---: " + pdshost);
				endpoint = pdshost;
			}

		}
		return endpoint;
	}

	private String getXrsendpoint(String env, int index) {
		String endpoint = null;
		List<String> xrahosts = myConfig.getXRSHosts();
		for (String xrshost : xrahosts) {
			if (index == xrahosts.indexOf(xrshost)) {
				//System.out.println("xrshost---: " + xrshost);
				endpoint = xrshost;
			}

		}
		return endpoint;
	}

	private String getTariffendpoint(String env, int index) {
		String endpoint = null;
		List<String> tariffhosts = myConfig.getTariffHosts();
		for (String tariffhost : tariffhosts) {
			if (index == tariffhosts.indexOf(tariffhost)) {
				//System.out.println("tariffhost---: " + tariffhost);
				endpoint = tariffhost;
			}

		}
		return endpoint;
	}

	private String getIlamsendpoint(String env, int index) {
		String endpoint = null;
		List<String> ilmshosts = myConfig.getILMSHosts();
		for (String ilmshost : ilmshosts) {
			if (index == ilmshosts.indexOf(ilmshost)) {
				//System.out.println("ilmshost---: " + ilmshost);
				endpoint = ilmshost;
			}

		}
		return endpoint;
	}

	/*
	 * private String getSoapEnvelope() {
	 * 
	 * File wsdlDir = new File(BASEDIR, "src/main/resources/wsdls/99 Adapters");
	 * 
	 * String[] wsdlNames; wsdlNames = wsdlDir.list(); String envelope = "empty";
	 * 
	 * for (String wsdlName : wsdlNames) {
	 * 
	 * // Print the names of files and directories System.out.println(wsdlName);
	 * File wsdlFile = new File(wsdlDir, wsdlName);
	 * 
	 * List<Operation> operationList =
	 * getPortTypeOperations(wsdlFile.toURI().toString());
	 * 
	 * ListIterator<Operation> listIterator = operationList.listIterator();
	 * 
	 * while (listIterator.hasNext()) {
	 * System.out.println(listIterator.next().getName());
	 * 
	 * // Get me matching envelope as a string envelope =
	 * getRequestEnvelopeToString(listIterator.next().getName());
	 * 
	 * } }
	 * 
	 * return envelope; }
	 */

	private String getRequestEnvelopeToString(String operation) {

		File requestsDir = new File(BASEDIR, "src/main/resources/requests");

		String[] requestsnames;
		requestsnames = requestsDir.list();
		String requestenvelope = "empty";

		for (String requestsname : requestsnames) {

			if (requestsname.equalsIgnoreCase(operation + ".xml")) {
				File requestFile = new File(requestsDir, requestsname);
				requestenvelope = readLineByLine(requestFile.toString());
			}
		}

		return requestenvelope;
	}

	private static List<Operation> getPortTypeOperations(String wsdlUrl) {
		List<Operation> operationList = new ArrayList();

		try {
			WSDLFactory factory = WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();

			// pass the location/url to the reader for parsing and get list of operations
			Definition wsdlInstance = reader.readWSDL(wsdlUrl);

			Map<String, PortTypeImpl> defMap = wsdlInstance.getAllPortTypes();
			Collection<PortTypeImpl> collection = defMap.values();
			for (PortTypeImpl portType : collection) {
				operationList.addAll(portType.getOperations());
			}

		} catch (WSDLException e) {
			System.out.println("get wsdl operation fail.");
			e.printStackTrace();
		}
		return operationList;
	}

	private static String readLineByLine(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}

}
