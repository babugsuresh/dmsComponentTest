package hmrc.cds.dms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import com.ibm.wsdl.PortTypeImpl;

@SpringBootApplication
public class DmsComponentTestApplication implements CommandLineRunner {

	private static final String BASEDIR = null;
	// private static Object //TODO;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DmsComponentTestApplication.class);
		System.out.println("\n----JESUS is my GOD----");
		app.run();
	}

	public void run(String... args) throws UnsupportedOperationException, SOAPException, IOException, Exception {

		logger.debug("This is a debug message");
		logger.info("This is an info message");
		logger.warn("This is a warn message");
		logger.error("This is an error message");

		System.out.println("\n----JESUS is my GOD----");

		Yaml yaml = new Yaml();
		Reader yamlFile = new FileReader("src/main/resources/application.yml");

		@SuppressWarnings("unchecked")
		Map<String, Object> yamlMaps = (Map<String, Object>) yaml.load(yamlFile);

		// System.out.println(yamlMaps.get("ReportDestination"));

		@SuppressWarnings("unchecked")
		final List<Map<String, Object>> systemMapping = (List<Map<String, Object>>) yamlMaps.get("SystemMapping");
		// System.out.println(systemMapping);
		// System.out.println(systemMapping.get(0).toString());
		// System.out.println(systemMapping.get(1).toString());

		@SuppressWarnings("unchecked")
		List<String> envs = (List<String>) yamlMaps.get("Environments");

		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();

		File directory = new File(BASEDIR, "src/main/resources/wsdls/99 Assembled adapters 3.2.8.11");
		File wsdlDir = null;
		File file;
		String[] wsdlNames;
		String endpoint = null;
		String soapenvelope = null;
		String systemtocall = null;
		boolean loopBack = false;

		List<OperationName> operationNames = new ArrayList<OperationName>();
		List<ServiceName> serviceNames = new ArrayList<ServiceName>();
		List<ReportBean> rb = new ArrayList<ReportBean>();

		for (String env : envs) {

			Iterator<File> iter = FileUtils.iterateFiles(directory, new String[] { "wsdl" }, true);

			while (iter.hasNext()) {
				file = (File) iter.next();
				System.out.println("\nWSDL Found: " + file.getName());
				// System.out.println("file Path: " + file.getPath());
				// System.out.println("file dir: " + file.getParent());
				wsdlDir = new File(BASEDIR, file.getParent());

				// String[] wsdlNames;
				wsdlNames = wsdlDir.list();
				for (String wsdlName : wsdlNames) {

					if (wsdlName.endsWith(".wsdl")) {
						File wsdlFile = new File(wsdlDir, wsdlName);

						outerloop: for (Map<String, Object> featureService : systemMapping) {
							for (Map.Entry<String, Object> entry : featureService.entrySet()) {
								// System.out.println("Key: " + entry.getKey());
								String key = entry.getKey();
								if (wsdlName.equalsIgnoreCase(key)) {
									String value = (String) entry.getValue();
									systemtocall = value;
									loopBack = false;
									break outerloop;

								} else {
									loopBack = true;
								}
							}
						}

						if (!loopBack) {
							System.out.println("Env Executing for: " + env + "\nSystem to call for " + wsdlName
									+ "\nWSDL Service is: " + systemtocall + "\nindex of environment: "
									+ envs.indexOf(env));

							endpoint = getEndPoint(env, envs.indexOf(env), systemtocall, yamlMaps);
							if (!(endpoint == null)) {
								String systemHostURI = systemtocall + "URI";
								endpoint = endpoint + yamlMaps.get(systemHostURI);

								System.out.println("Final Endpoint URL for: " + systemtocall + " = " + endpoint);

								List<Operation> operationList = getPortTypeOperations(wsdlFile.toURI().toString());

								System.out.println(
										"Total Number of operations in " + wsdlName + " : " + operationList.size());

								// List<OperationName> operationNames = new ArrayList<OperationName>();

								for (Operation opname : operationList) {
									//System.out.println("List of Operations: " +opname.getName() );
									//System.out.println("---Started Looking for Request XMLs---");

									soapenvelope = getRequestEnvelopeToString(opname.getName());

									if (!soapenvelope.equalsIgnoreCase("empty")) {

										System.out.println("Request XML available for Operation: " + opname.getName());
										System.setProperty("java.net.useSystemProxies", "true");
										System.out.println("SOAP Request Payload: " + soapenvelope);

										System.out.println("SOAP endpoint to Call: " + endpoint);

										InputStream is = new ByteArrayInputStream(soapenvelope.getBytes());
										SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
										MimeHeaders headers = request.getMimeHeaders();

										headers.setHeader("Content-Type", "application/xml");
										request.saveChanges();

										SOAPMessage soapResponse = soapConnection.call(request, endpoint);

										// System.out.println(printSOAPResponse(soapResponse));
										// ByteArrayOutputStream streamOut = new ByteArrayOutputStream();

										StringWriter sw = new StringWriter();

										try {
											TransformerFactory transformerFactory = TransformerFactory.newInstance();
											Transformer transformer = transformerFactory.newTransformer();
											Source sourceContent = soapResponse.getSOAPPart().getContent();

											System.out.print("\nResponse SOAP Message = ");
											System.out.print("\n");
											// StreamResult result = new StreamResult(System.out);
											StreamResult result = new StreamResult(sw);
											transformer.transform(sourceContent, result);

										} catch (TransformerException e) {
											throw new RuntimeException(e);
										}

										OperationName operationName = new OperationName();
										operationName.setOperationName(opname.getName());

										if (!soapResponse.getSOAPBody().hasFault()) {
											operationName.setStatus("Running");
										} else {
											operationName.setStatus("  Down ");
										}
										operationName.setResponse(sw.toString());

										operationNames.add(operationName);
										System.out.println("operationName Added successfully"+operationName);

										// System.out.println("\noperationNames: " + operationNames);

										ServiceName services = new ServiceName();
										services.setSystemName(systemtocall);
										services.setServiceName(wsdlName);
										services.setOperationNames(operationNames);
										serviceNames.add(services);
										System.out.println("serviceName Added successfully"+services);

										ReportBean reportBean = new ReportBean();
										reportBean.setEnvName(env);
										reportBean.setServiceNames(serviceNames);
										rb.add(reportBean);
										System.out.println("ReportBean Added successfully"+reportBean);

									}else {
										System.out.println("No Request XML is provided for: "+opname.getName());
									}

								}
							} else {
								System.out.println("No External System End Points are Configured for: " + systemtocall);
							}

						} else {
							System.out.println("No External System is configured for WSDL: " + wsdlName);
						}
					}

				}

			}

		}
		System.out.println("LOGGGGGGGGGGGGGGG");
		System.out.println("operationNames: " + operationNames);
		System.out.println("serviceNames: " + serviceNames);
		System.out.println("ReportBean: " + rb);

		generateHTMLfile(rb);

	}

	private void generateHTMLfile(List<ReportBean> rb) throws IOException {
		
		System.out.println("JESUS is MY LORD");

		File htmlTemplateFile = new File("src/main/resources/Report2.html");

		String htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");

		String title = "New Page";
		
		String body = "";

		String tag1 = "<section>  <h1>";
		String tag2 = "</h1><div class=\"tbl-header\">\r\n"
				+ "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\r\n" + "      <thead>\r\n"
				+ "        <tr><b>\r\n" + "          <th><b>System Name<b/></th>\r\n"
				+ "          <th><b>Service Name<b/></th>\r\n" + "          <th><b>Operation Name<b/></th>\r\n"
				+ "          <th><b>Status<b/></th>\r\n" + "          <th><b>Response XML<b/></th>\r\n"
				+ "        </tr>\r\n" + "      </thead>\r\n" + "    </table>\r\n" + "  </div>\r\n"
				+ "  <div class=\"tbl-content\">\r\n"
				+ "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\r\n" + "      <tbody>";

		String tag3 = "</tbody>\r\n" + "    </table>\r\n" + "  </div>\r\n" + "</section>";

		String tag4 = "<tr><td>";
		String tag5 = "</td></tr>";
		String tag6 = "</td><td>";
		String green = "<span class=\"label success\">";
		String red = "<span class=\"label danger\">";
		String blue = "<span class=\"label label-info\">";
		String tag7 = "</span>";

		String tag8 = "<button type=\"button\" class=\"collapsible\">";
		String tag9 = "</button>";
		String tag10 = "<div class=\"content\">";
		String tag11 = "</div>";

		// To Make response button to be clickable
		String js = "<script>\r\n" + "var coll = document.getElementsByClassName(\"collapsible\");\r\n" + "var i;\r\n"
				+ "\r\n" + "for (i = 0; i < coll.length; i++) {\r\n"
				+ "  coll[i].addEventListener(\"click\", function() {\r\n"
				+ "    this.classList.toggle(\"active\");\r\n" + "    var content = this.nextElementSibling;\r\n"
				+ "    if (content.style.display === \"block\") {\r\n" + "      content.style.display = \"none\";\r\n"
				+ "    } else {\r\n" + "      content.style.display = \"block\";\r\n" + "    }\r\n" + "  });\r\n"
				+ "}\r\n" + "</script>";

		// <button type="button" class="collapsible">Response</button>

		// tag8+Response+tage9

		// tag8+op.getResponse()+tag9

		

		StringBuilder builder = new StringBuilder();

		for (ReportBean r : rb) {
			String toPublish = "";
			String dataToLoad = "";
			StringBuilder localBuilder = new StringBuilder();
			for (ServiceName sn : r.getServiceNames()) {
				for (OperationName op : sn.getOperationNames()) {
					String sname = sn.getServiceName().substring(0, sn.getServiceName().lastIndexOf(".")).replaceAll("\\SOAP.*?\\b", "");
					String color = null;
					if(op.getStatus().equalsIgnoreCase("Running")) {
						color = green + op.getStatus() + tag7;
					}else {
						color = red + op.getStatus() + tag7;
					}
					//String cc = "<div class=\"content\">"+op.getResponse()+"</div>";
					String cc = "<div class=\"content\">Testing</div>";
					dataToLoad = tag4 + sn.getSystemName() + tag6 + sname + tag6 + op.getOperationName()
							+ tag6 + color + tag6 + tag8 + "Response" + "</button>" + cc + tag5;
					localBuilder.append(dataToLoad);
				}
			}

			toPublish = localBuilder.toString();
			toPublish = tag1 + r.getEnvName() + tag2 + toPublish + tag3;
			builder.append(toPublish);
		}

		String finalDataToPublish = builder.toString();

		body = finalDataToPublish + js;

		htmlString = htmlString.replace("$title", title);
		htmlString = htmlString.replace("$body", body);
		File newHtmlFile = new File("src/main/resources/new.html");
		FileUtils.writeStringToFile(newHtmlFile, htmlString, "UTF-8");

	}

	// }

	private String getEndPoint(String env, int index, String system, Map<String, Object> yamlMaps) {
		String endpoint = null;

		String systemHost = system + "Hosts";
		// System.out.println("Called here" + yamlMaps.toString() + ", systemHost: " +
		// systemHost);

		@SuppressWarnings("unchecked")
		List<String> systemHosts = (List<String>) yamlMaps.get(systemHost);
		// System.out.println("index: " + index + ", " + systemHosts);

		if (systemHosts == null || systemHosts.isEmpty()) {
			return endpoint;
		} else {
			if (!(systemHosts == null) || !(systemHosts.isEmpty())) {
				for (String host : systemHosts) {
					// System.out.println("index: " + systemHosts.indexOf(host));
					if (index == systemHosts.indexOf(host)) {
						// System.out.println("Called here"+systemHosts.indexOf(host));
						// System.out.println("hosts---: " + host);
						endpoint = host;
					}

				}
				System.out.println("No of Endpoints configured for system : " + system + "=" + systemHosts.size());
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
