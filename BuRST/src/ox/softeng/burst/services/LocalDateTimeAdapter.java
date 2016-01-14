package ox.softeng.burst.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;
 
public class LocalDateTimeAdapter 
    extends XmlAdapter<Date, LocalDateTime>{
 
    public LocalDateTime unmarshal(Date v) throws Exception {
    	return LocalDateTime.ofInstant(v.toInstant(), ZoneId.systemDefault());
    }
 
    public Date marshal(LocalDateTime v) throws Exception {
    	return Date.from(v.atZone(ZoneId.systemDefault()).toInstant());
    }
 
}