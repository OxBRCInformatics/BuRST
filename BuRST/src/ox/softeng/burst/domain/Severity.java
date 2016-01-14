package ox.softeng.burst.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Severity {
	
	@Id
	protected String severityName; 
	
	public Severity(){}
	
	public Severity(String severityName)
	{
		this.severityName = severityName;
	}

	public String getSeverityName() {
		return severityName;
	}

	public void setSeverityName(String severityName) {
		this.severityName = severityName;
	}
	
}
