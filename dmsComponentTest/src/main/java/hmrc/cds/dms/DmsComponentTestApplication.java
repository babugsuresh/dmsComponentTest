package hmrc.cds.dms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import com.ibm.wsdl.PortTypeImpl;

@SpringBootApplication
public class DmsComponentTestApplication implements CommandLineRunner {

	@Autowired
	private YAMLConfig myConfig;

	private static final String BASEDIR = null;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DmsComponentTestApplication.class);
		System.out.println("\n----JESUS is my GOD----");
		app.run();
	}

	public void run(String... args) throws UnsupportedOperationException, SOAPException, IOException, Exception {

		Yaml yaml = new Yaml();
		Reader yamlFile = new FileReader(
				"C:\\Users\\SURESHGollamudi\\git\\dmsComponentTest\\dmsComponentTest\\src\\main\\resources\\application.yml");

		@SuppressWarnings("unchecked")
		Map<String, Object> yamlMaps = (Map<String, Object>) yaml.load(yamlFile);

		System.out.println(yamlMaps.get("ReportDestination"));

		@SuppressWarnings("unchecked")
		final List<Map<String, Object>> systemMapping = (List<Map<String, Object>>) yamlMaps.get("SystemMapping");
		// System.out.println(systemMapping);
		// System.out.println(systemMapping.get(0).toString());
		// System.out.println(systemMapping.get(1).toString());

		@SuppressWarnings("unchecked")
		List<String> envs = (List<String>) yamlMaps.get("Environments");

		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();

		File wsdlDir = new File(BASEDIR, "src/main/resources/wsdls/99 Adapters");

		for (String env : envs) {
			String[] wsdlNames;
			wsdlNames = wsdlDir.list();
			for (String wsdlName : wsdlNames) {

				// Print the names of files and directories
				System.out.println("\nService wsdlName: " + wsdlName);
				File wsdlFile = new File(wsdlDir, wsdlName);

				String endpoint = null;
				String soapenvelope = null;
				// for (String env : envs) {

				System.out.println("\nenv running: " + env);

				String systemtocall = null;

				for (Map.Entry<String, String> entry : myConfig.SystemMapping.entrySet()) {
					String key = entry.getKey();
					if (wsdlName.equalsIgnoreCase(key.substring(2))) {
						String value = entry.getValue();
						systemtocall = value;
						// System.out.println("Matching found for: "+wsdlName);
					}

				}

				System.out.println("Env Executing for: " + env + ", System to call for " + wsdlName
						+ " WSDL Service is: " + systemtocall + ", index of environment: " + envs.indexOf(env));

				endpoint = getEndPoint(env, envs.indexOf(env), systemtocall, yamlMaps);

				String systemHostURI = systemtocall + "URI";
				endpoint = endpoint + yamlMaps.get(systemHostURI);

				System.out.println("endpoint: " + endpoint + ", systemHostURI: " + yamlMaps.get("systemHostURI"));

				List<Operation> operationList = getPortTypeOperations(wsdlFile.toURI().toString());

				System.out.println("Total Number of operations in " + wsdlName + " : " + operationList.size());

				for (Operation opname : operationList) {

					soapenvelope = getRequestEnvelopeToString(opname.getName());
					System.out.println("\nOperationName: " + opname.getName() + "\nis there a Request XML exists?: "
							+ !soapenvelope.equalsIgnoreCase("empty"));

					if (!soapenvelope.equalsIgnoreCase("empty")) {
						System.setProperty("java.net.useSystemProxies", "true");
						System.out.println("SOAPenvelopetoString: " + soapenvelope);

						System.out.println("SOAP endpoint to Call: " + endpoint);

						InputStream is = new ByteArrayInputStream(soapenvelope.getBytes());
						SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
						MimeHeaders headers = request.getMimeHeaders();

						headers.setHeader("Content-Type", "application/xml");
						request.saveChanges();

						SOAPMessage soapResponse = soapConnection.call(request, endpoint);

						// System.out.println(printSOAPResponse(soapResponse));

						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						Source sourceContent = soapResponse.getSOAPPart().getContent();
						System.out.print("\nResponse SOAP Message = ");
						StreamResult result = new StreamResult(System.out);
						transformer.transform(sourceContent, result);

						// System.out.println("Response Suresh: "+sourceContent);
					}

				}

			}

		}

	}

	// }

	private String getEndPoint(String env, int index, String system, Map<String, Object> yamlMaps) {
		String endpoint = null;

		String systemHost = system + "Hosts";
		// System.out.println("Called here" + yamlMaps.toString() + ", systemHost: " +
		// systemHost);

		@SuppressWarnings("unchecked")
		List<String> systemHosts = (List<String>) yamlMaps.get(systemHost);
		System.out.println("index: " + index + ", " + systemHosts);

		for (String host : systemHosts) {
			System.out.println("index: " + systemHosts.indexOf(host));
			if (index == systemHosts.indexOf(host)) {
				// System.out.println("Called here"+systemHosts.indexOf(host));
				System.out.println("hosts---: " + host);
				endpoint = host;
			}

		}

		return endpoint;

	}

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

	@SuppressWarnings("unchecked")
	private static List<Operation> getPortTypeOperations(String wsdlUrl) {
		List<Operation> operationList = new ArrayList<Operation>();

		try {
			WSDLFactory factory = WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();

			// pass the location/url to the reader for parsing and get list of operations
			Definition wsdlInstance = reader.readWSDL(wsdlUrl);

			@SuppressWarnings("unchecked")
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
