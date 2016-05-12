package ox.softeng.burst.xml;

import com.rabbitmq.client.LongString;
import com.rabbitmq.client.impl.LongStringHelper;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @since 11/05/2016
 */
public class LongStringAdapter extends XmlAdapter<String, LongString> {
    @Override
    public LongString unmarshal(String v) throws Exception {
        return LongStringHelper.asLongString(v);
    }

    @Override
    public String marshal(LongString v) throws Exception {
        return v.toString();
    }
}
