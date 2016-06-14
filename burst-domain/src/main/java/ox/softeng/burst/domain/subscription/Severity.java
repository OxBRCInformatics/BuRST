package ox.softeng.burst.domain.subscription;

import ox.softeng.burst.domain.SeverityEnum;

import javax.persistence.*;

@Entity
@Table(name = "severity", schema = "subscription")
@NamedQuery(name = "severity.getSeverity", query = "select s from Severity s where s.severity = :severityEnum")
public class Severity {

    @Id
    @Enumerated(EnumType.STRING)
    private SeverityEnum severity;

    public Severity(SeverityEnum severityEnum) {
        this.severity = severityEnum;
    }

    public Severity() {
        this.severity = null;
    }

    public SeverityEnum getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    static Severity from(String severity) {
        return from(SeverityEnum.valueOf(severity));
    }

    static Severity from(SeverityEnum severity) {
        return new Severity(severity);
    }
}
