package ox.softeng.burst.util;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import ox.softeng.burst.services.MessageMsg;

public class GenerateMessageSchema 
{
	public static void main(String[] args) throws IOException, JAXBException
	{
		GenerateMessageSchema gms = new GenerateMessageSchema();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(MessageMsg.class);
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
