package hmrc.cds.dms;

import java.util.List;

public class ReportBean {

	private List<Environments> environments;

	public List<Environments> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<Environments> environments) {
		this.environments = environments;
	}

}

class Environments {
	private List<ServiceNames> serviceNames;

	public List<ServiceNames> getServiceNames() {
		return serviceNames;
	}

	public void setServiceNames(List<ServiceNames> serviceNames) {
		this.serviceNames = serviceNames;
	}

	
}

class ServiceNames {
	private List<OperationNames> operationNames;

	public List<OperationNames> getOperationNames() {
		return operationNames;
	}

	public void setOperationNames(List<OperationNames> operationNames) {
		this.operationNames = operationNames;
	}
}

class OperationNames {
	private String name;

	private String status;

	private String response;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return this.status;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponse() {
		return this.response;
	}
}
