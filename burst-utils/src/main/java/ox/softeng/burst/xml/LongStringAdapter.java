/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 James Welch
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
