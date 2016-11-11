package ox.softeng.burst.domain.report;

import javax.persistence.*;

@Entity
@Table(name = "metadata", schema = "report", indexes = {
        @Index(columnList = "key", name = "index_key")
})
@SequenceGenerator(name = "metadataIdSeq", sequenceName = "report.metadata_id_seq", allocationSize = 1)
public class Metadata {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metadataIdSeq")
    protected Long id = null;
    protected String key;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", foreignKey = @ForeignKey(name = "fk_metadata_messages"))
    protected Message message;
    protected String value;


    public Metadata(String key, String value, Message message) {
        this.key = key;
        this.value = value;
        this.message = message;
    }

    public Metadata() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


