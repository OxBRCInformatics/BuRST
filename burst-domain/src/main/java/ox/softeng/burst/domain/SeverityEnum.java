package ox.softeng.burst.domain;

// Taken from:
// https://logging.apache.org/log4j/2.0/log4j-core/apidocs/org/apache/logging/log4j/core/net/Severity.html

import org.apache.commons.lang3.text.WordUtils;

public enum SeverityEnum {
    DEBUG, INFORMATIONAL, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERGENCY;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name());
    }
}
