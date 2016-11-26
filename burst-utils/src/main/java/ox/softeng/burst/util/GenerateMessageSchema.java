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
package ox.softeng.burst.util;

import ox.softeng.burst.xml.MessageDTO;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

public class GenerateMessageSchema 
{
    public static void main(String[] args) throws IOException, JAXBException {
        GenerateMessageSchema gms = new GenerateMessageSchema();

        JAXBContext jaxbContext = JAXBContext.newInstance(MessageDTO.class);
        MySchemaOutputResolver sor = gms.new MySchemaOutputResolver();
        jaxbContext.generateSchema(sor);

        System.out.println(sor.getSchema());
    }

    private class MySchemaOutputResolver extends SchemaOutputResolver {
        private StringWriter stringWriter = new StringWriter();

        public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException  {
            StreamResult result = new StreamResult(stringWriter);
            result.setSystemId(suggestedFileName);
            return result;
        }

        public String getSchema() {
            return stringWriter.toString();
        }

    };


}
