package ox.softeng.burst.xml;

/**
 * @since 10/11/2016
 */
public class MetadataDTO {
    protected String key;
    protected String value;

    public MetadataDTO() {

    }

    public MetadataDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        return sb.toString();
    }
}
