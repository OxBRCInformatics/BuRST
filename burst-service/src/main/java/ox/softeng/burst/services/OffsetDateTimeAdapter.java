package ox.softeng.burst.services;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class OffsetDateTimeAdapter
        extends XmlAdapter<Date, OffsetDateTime> {

    public OffsetDateTime unmarshal(Date v) throws Exception {
        return OffsetDateTime.ofInstant(v.toInstant(), ZoneId.systemDefault());
    }

    public Date marshal(OffsetDateTime v) throws Exception {
        return Date.from(v.toInstant());
    }
 
}