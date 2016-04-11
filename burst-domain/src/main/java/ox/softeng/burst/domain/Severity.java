package ox.softeng.burst.domain;

import javax.persistence.*;

@Entity
@Table(schema="Subscription")
public class Severity {

	@Id
	@Enumerated(EnumType.STRING)
	private SeverityEnum severity;

	public Severity(SeverityEnum severityEnum) {
		this.severity = severityEnum;
	}

	public Severity()
	{
		this.severity = null;
	}
	
	public SeverityEnum getSeverity() {
		return severity;
	}

	public void setSeverity(SeverityEnum severity) {
		this.severity = severity;
	}
	
	
}
