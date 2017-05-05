import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

String defPattern = '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'

String service = System.getProperty('burst.service') ?: 'all'

def logDir = new File('.', 'logs').canonicalFile
if (!logDir) logDir.mkdirs()

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = defPattern
    }
    filter(ThresholdFilter) {
        level = INFO
    }
}

appender("FILE", FileAppender) {
    file = "${logDir}/burst-${service}-service.log"
    append = false
    encoder(PatternLayoutEncoder) {
        pattern = defPattern
    }
}

root(INFO, ['STDOUT', 'FILE'])

logger('ox.softeng', DEBUG)
//logger('ox.softeng.burst.service.message.RabbitService', INFO)
//logger('ox.softeng.burst.service.report.ReportScheduler', TRACE)
//logger('ox.softeng.burst.service.report.ReportService', TRACE)
//logger('ox.softeng.burst.service.report.EmailService', TRACE)
//logger('org.hibernate.SQL', DEBUG)
//logger 'org.hibernate.type', TRACE