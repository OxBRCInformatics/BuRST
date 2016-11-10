@XmlJavaTypeAdapters(
        {
                @XmlJavaTypeAdapter(type = OffsetDateTime.class,
                                    value = OffsetDateTimeAdapter.class),
                @XmlJavaTypeAdapter(type = LongString.class,
                                    value = LongStringAdapter.class),
        }
)
/**
 * @since 11/05/2016
 */
        package ox.softeng.burst.xml;

import com.rabbitmq.client.LongString;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.OffsetDateTime;