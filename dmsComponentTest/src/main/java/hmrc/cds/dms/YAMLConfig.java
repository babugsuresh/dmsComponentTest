package hmrc.cds.dms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//To load a set of related properties from a properties file

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YAMLConfig {	
	
	private List<String> Environment = new ArrayList<>();
    private String ReportDestination;
    private String PdsURI;
    private String XrsURI;
    private String TariffURI;
    private String IlmsURI;
    private List<String> PDSHosts = new ArrayList<>();
    private List<String> XRSHosts = new ArrayList<>();
    private List<String> TariffHosts = new ArrayList<>();
    private List<String> ILMSHosts = new ArrayList<>();
    Map<String,String> SystemMapping = new HashMap<String, String>();
    
	
    public Map<String, String> getSystemMapping() {
		return SystemMapping;
	}
	public void setSystemMapping(Map<String, String> systemMapping) {
		SystemMapping = systemMapping;
	}
	public List<String> getEnvironment() {
		return Environment;
	}
	public void setEnvironment(List<String> environment) {
		Environment = environment;
	}
	public String getReportDestination() {
		return ReportDestination;
	}
	public void setReportDestination(String reportDestination) {
		ReportDestination = reportDestination;
	}
	public String getPdsURI() {
		return PdsURI;
	}
	public void setPdsURI(String pdsURI) {
		PdsURI = pdsURI;
	}
	public String getXrsURI() {
		return XrsURI;
	}
	public void setXrsURI(String xrsURI) {
		XrsURI = xrsURI;
	}
	public String getTariffURI() {
		return TariffURI;
	}
	public void setTariffURI(String tariffURI) {
		TariffURI = tariffURI;
	}
	public String getIlmsURI() {
		return IlmsURI;
	}
	public void setIlmsURI(String ilmsURI) {
		IlmsURI = ilmsURI;
	}
	public List<String> getPDSHosts() {
		return PDSHosts;
	}
	public void setPDSHosts(List<String> pDSHosts) {
		PDSHosts = pDSHosts;
	}
	public List<String> getXRSHosts() {
		return XRSHosts;
	}
	public void setXRSHosts(List<String> xRSHosts) {
		XRSHosts = xRSHosts;
	}
	public List<String> getTariffHosts() {
		return TariffHosts;
	}
	public void setTariffHosts(List<String> tariffHosts) {
		TariffHosts = tariffHosts;
	}
	public List<String> getILMSHosts() {
		return ILMSHosts;
	}
	public void setILMSHosts(List<String> iLMSHosts) {
		ILMSHosts = iLMSHosts;
	}
	

}
