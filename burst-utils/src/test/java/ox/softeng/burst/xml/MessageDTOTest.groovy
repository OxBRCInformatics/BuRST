package ox.softeng.burst.xml

import ox.softeng.burst.util.SeverityEnum
import spock.lang.Shared
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * @since 04/05/2016
 */
class MessageDTOTest extends Specification {

    @Shared
    Path xmlPath
    @Shared
    Unmarshaller unmarshaller;

    def setup() {
        xmlPath = Paths.get("src/test/resources/xml");
        if (!Files.exists(xmlPath)) xmlPath = Paths.get("burst-domain/src/test/resources/xml");

        try {
            unmarshaller = JAXBContext.newInstance(MessageDTO.class).createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace()
        }
    }

    void "loading 1.0.1 xml messages 1"() {

        when:
        Path p = xmlPath.resolve("1.0.1/instance1.xml")
        MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(p.toFile()));

        then:
        messageDto != null
        messageDto.dateTimeCreated == OffsetDateTime.of(2006, 5, 4, 18, 13, 51, 0, ZoneOffset.UTC)
        messageDto.severity == SeverityEnum.ERROR
        messageDto.source == 'System 1'
        messageDto.details == 'There is a problem here'
        messageDto.topics == ['topic 1', 'topic 2']
        messageDto.metadata.size() == 2
    }

    void "loading 1.0.1 xml messages 2"() {

        when:
        Path p = xmlPath.resolve("1.0.1/instance2.xml")
        MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(p.toFile()));

        then:
        messageDto != null
        messageDto.dateTimeCreated == OffsetDateTime.of(2006, 5, 4, 18, 13, 51, 0, ZoneOffset.UTC)
        messageDto.severity == SeverityEnum.WARNING
        messageDto.source == 'System 2'
        messageDto.details == 'There is a problem here'
        messageDto.topics == ['topic 3', 'topic 2']
        messageDto.metadata.size() == 0
    }

    void "loading 1.0.1 xml messages 3"() {

        when:
        Path p = xmlPath.resolve("1.0.1/instance3.xml")
        MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(p.toFile()));

        then:
        messageDto != null
        messageDto.dateTimeCreated == OffsetDateTime.of(2006, 5, 4, 18, 13, 51, 0, ZoneOffset.UTC)
        messageDto.severity == SeverityEnum.NOTICE
        messageDto.source == 'System 1'
        messageDto.title == 'Test message'
        messageDto.details == 'There is a problem here'
        messageDto.topics == ['topic 1', 'topic 2', 'topic 3']
        messageDto.metadata.size() == 1
    }

    void "loading old xml messages 1"() {

        when:
        Path p = xmlPath.resolve("old/instance1.xml")
        MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(p.toFile()));

        then:
        messageDto != null
        messageDto.dateTimeCreated == OffsetDateTime.of(2006, 5, 4, 18, 13, 51, 0, ZoneOffset.UTC)
        messageDto.severity == SeverityEnum.ERROR
        messageDto.source == 'System 1'
        messageDto.details == 'There is a problem here'
        messageDto.topics == ['topic 1', 'topic 2']
        messageDto.metadata.size() == 2
    }

    void "loading old xml messages 2"() {

        when:
        Path p = xmlPath.resolve("old/instance2.xml")
        MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(p.toFile()));

        then:
        messageDto != null
        messageDto.dateTimeCreated == OffsetDateTime.of(2006, 5, 4, 18, 13, 51, 0, ZoneOffset.UTC)
        messageDto.severity == SeverityEnum.WARNING
        messageDto.source == 'System 2'
        messageDto.details == 'There is a problem here'
        messageDto.topics == ['topic 3', 'topic 2']
        messageDto.metadata.size() == 0
    }

    void "loading old xml messages 3"() {

        when:
        Path p = xmlPath.resolve("old/instance3.xml")
        MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(p.toFile()));

        then:
        messageDto != null
        messageDto.dateTimeCreated == OffsetDateTime.of(2006, 5, 4, 18, 13, 51, 0, ZoneOffset.UTC)
        messageDto.severity == SeverityEnum.NOTICE
        messageDto.source == 'System 1'
        messageDto.details == 'There is a problem here'
        messageDto.topics == ['topic 1', 'topic 2', 'topic 3']
        messageDto.metadata.size() == 1
    }
}
