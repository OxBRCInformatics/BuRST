/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 James Welch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ox.softeng.burst.xml;

import ox.softeng.burst.util.SeverityEnum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="message")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageDTO implements Serializable{

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    private OffsetDateTime dateTimeCreated;
    @XmlElement(required=true)
    private String details;
    @XmlElement(name = "metadata")
    private List<MetadataDTO> metadata;
    @XmlElement(required = true)
    private SeverityEnum severity;
    @XmlElement(required=true)
    private String source;
    @XmlElement
    private String title;
    @XmlElement( name="topic",required=true)
    private List<String> topics;

    public MessageDTO(){
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addMetadata(MetadataDTO md) {
        metadata.add(md);
    }

    public void addMetadata(String key, String value) {
        metadata.add(new MetadataDTO(key, value));
    }

    public MessageDTO addToMetadata(String key, String value) {
        this.metadata.add(new MetadataDTO(key, value));
        return this;
    }

    public void addTopic(String topic) {
        topics.add(topic);
    }

    public OffsetDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }


    public void setDateTimeCreated(OffsetDateTime dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<MetadataDTO> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MetadataDTO> metadata) {
        this.metadata = metadata;
    }

    public SeverityEnum getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageDTO{\n");
        sb.append("dateTimeCreated=").append(dateTimeCreated);
        sb.append(",\n details='").append(details).append('\'');
        sb.append(",\n metadata=").append(metadata);
        sb.append(",\n severity=").append(severity);
        sb.append(",\n source='").append(source).append('\'');
        sb.append(",\n title='").append(title).append('\'');
        sb.append(",\n topics=").append(topics);
        sb.append("\n}");
        return sb.toString();
    }

}
