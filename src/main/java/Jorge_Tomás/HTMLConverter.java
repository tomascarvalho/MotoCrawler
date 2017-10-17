package Jorge_Tomás;

import org.xml.sax.SAXException;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.*;

/**
 * Created by jorgearaujo on 12/10/2017.
 */
public class HTMLConverter {

    private ConnectionFactory cf;
    private Destination d;
    private Topic tpc;


    public HTMLConverter() throws NamingException {
        this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
        //lookup for the topic
        this.tpc = InitialContext.doLookup("jms/topic/TopicExample");
    }

    public String receive() {

        try (JMSContext cntxt = this.cf.createContext("joao", "br1o+sa*")) {

            //set the client ID
            cntxt.setClientID("htmlSummaryCreator");

            //create durable consumer
            JMSConsumer htmlSummaryConsumer = cntxt.createDurableConsumer(this.tpc, "htmlSummaryCreator");
            //receive the xml
            return htmlSummaryConsumer.receiveBody(String.class);
        }
    }


    //function to tranform the xml fie into HTML
    public static File transformXML(String xmlFilename) throws TransformerException, FileNotFoundException {

        //XSL template to do the conversion
        Source xslFile  =  new StreamSource("stylesheet.xsl");
        //xml file to be converted
        Source xmlFile =  new StreamSource(xmlFilename);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        //output file
        OutputStream htmlFile = new FileOutputStream("docs/index.html");
        //apply the transformation
        Transformer transform = transformerFactory.newTransformer(xslFile);
        transform.transform(xmlFile, new StreamResult(htmlFile));
        //return the html transformed file
        File transformed = new File("htmlSummary.html");
        System.out.println("Html filed created");
        return transformed;
    }

    //validates the XML against the XSD
    public static boolean isValidXML(String xml, String xsd)
    {
        File schemaFile = new File(xsd);
        Source xmlFile = new StreamSource(new StringReader(xml));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            System.out.println("XML is valid");
            return true;
        } catch (SAXException e) {
            System.out.println("XML is NOT valid because:" + e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return false;
    }


    public static void main(String[] args) throws TransformerException, IOException, NamingException {
        while(true) {

            //receive the XML String
            HTMLConverter receiver = new HTMLConverter();
            String xmlString = receiver.receive();
            if (isValidXML(xmlString, "skeleton.xsd")) {
                //write it to a file
                File file = new File ("file.xml");
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(xmlString);
                out.close();

                //transfor to html
                File f = transformXML("file.xml");
                //open html with browser
                System.out.println("Trying to open your browser. If this crashes, you can still open the hmtl manually");
                //Desktop.getDesktop().browse(f.toURI());

            }
        }

    }
}
