package Jorge_Tomás;

/**
 * Created by jorgearaujo on 26/09/2017.
 */
import org.xml.sax.SAXException;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


public class Keeper implements MessageListener{
    private ConnectionFactory cf;
    private Destination d;
    private Topic tpc;
    public static List<Advertisements.Advert> adverts;

    public Keeper() throws NamingException {
        this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
        this.d = InitialContext.doLookup("jms/queue/QueueExample");
        this.tpc = InitialContext.doLookup("jms/topic/TopicExample");
    }

    public String receive() {
        try (JMSContext cntxt = this.cf.createContext("joao", "br1o+sa*")) {
            //set the client ID
            cntxt.setClientID("carsKeeper");
            //create topic consumer
            JMSConsumer receiveRequestQueue = cntxt.createConsumer(d);
            //create durable consumer
            JMSConsumer carsKeeperConsumer = cntxt.createDurableConsumer(this.tpc,"carsKeeper");
            //set mesasge listener
            receiveRequestQueue.setMessageListener(this);

            return carsKeeperConsumer.receiveBody(String.class);
        }
    }

    public void unmarshalXML(String xmlString) {
        if (adverts == null)
            adverts = new ArrayList<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Advertisements.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlString);
            synchronized (adverts) {
                adverts = ((Advertisements) unmarshaller.unmarshal(reader)).getAdvert();
            }

        } catch(JAXBException je) {
            System.err.println(je);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            JMSContext cntxt = this.cf.createContext("joao", "br1o+sa*");
            JMSProducer replyToTemporaryQueue = cntxt.createProducer();
            ArrayList<Advertisements.Advert> queryResult = new ArrayList<>();
            String finalResult = "";
            if (message instanceof TextMessage) {
                List<Advertisements.Advert> adverts;
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                System.out.println(messageText);
                String [] query = messageText.split(" ");
                if (query.length >= 3) {
                    queryResult = queryAdvert(query);
                }
            }

            if (!queryResult.isEmpty()) {
                for (Advertisements.Advert result : queryResult) {
                    finalResult = finalResult + result.toString() + "\n";
                }
            } else {
                finalResult = "No results";
            }
            replyToTemporaryQueue.send(message.getJMSReplyTo(), finalResult);

        } catch (JMSException e) {
            //Handle the exception appropriately
        }
    }

    public ArrayList<Advertisements.Advert> queryAdvert(String [] query) {
        ArrayList<Advertisements.Advert> queryResult = new ArrayList();
        List<Advertisements.Advert> adverts_aux;

        synchronized (adverts) {
            adverts_aux = adverts;
        }
        for (Advertisements.Advert advert : adverts_aux) {
            if (query[0].equals("brand")) {
                if (advert.getBrand() != null) {
                    String brand = advert.getBrand();
                    brand = brand.toLowerCase();
                    if (query[1].equals("=") || query[1].equals("==")) {
                        if (brand.equals(query[2])) {
                            queryResult.add(advert);
                        }
                    }
                }
            } else if (query[0].equals("model")) {
                if (advert.getModel() != null) {
                    String model = advert.getModel();
                    model = model.toLowerCase();
                    if (query[1].equals("=") || query[1].equals("==")) {
                        if (model.equals(query[2])) {
                            queryResult.add(advert);
                        }
                    }
                }
            } else if (query[0].equals("mileage")) {
                if (advert.getMileage() != null) {
                    Advertisements.Advert.Mileage mileage = advert.getMileage();
                    int mileageValue = mileage.getValue();
                    if (query[1].equals("=") || query[1].equals("==")) {
                        if (mileageValue == Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    } else if (query[1].equals(">")) {
                        if (mileageValue > Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    } else if (query[1].equals("<")) {
                        if (mileageValue < Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    }
                }
            } else if (query[0].equals("price")) {
                if (advert.getPrice() != null) {
                    Advertisements.Advert.Price price = advert.getPrice();
                    int priceValue = price.getValue();
                    if (query[1].equals("=") || query[1].equals("==")) {
                        if (priceValue == Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    } else if (query[1].equals(">")) {
                        if (priceValue > Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    } else if (query[1].equals("<")) {
                        if (priceValue < Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    }
                }
            } else if (query[0].equals("color")) {
                if (advert.getColor() != null) {
                    String color = advert.getColor();
                    color = color.toLowerCase();
                    if (query[1].equals("=") || query[1].equals("==")) {
                        if (color.equals(query[2])) {
                            queryResult.add(advert);
                        }
                    }
                }
            } else if ((query[0].equals("horsepower")) || (query[0].equals("hp"))) {
                if (advert.getHorsePower() != null) {
                    Advertisements.Advert.HorsePower horsePower = advert.getHorsePower();
                    int horsePowerValue = horsePower.getValue();
                    if (query[1].equals("=") || query[1].equals("==")) {
                        if (horsePowerValue == Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    } else if (query[1].equals(">")) {
                        if (horsePowerValue > Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    } else if (query[1].equals("<")) {
                        if (horsePowerValue < Integer.parseInt(query[2])) {
                            queryResult.add(advert);
                        }
                    }
                }
            }
        }

        return queryResult;
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

    public static void main(String[] args) throws NamingException {
        while (true) {
            Keeper keeper = new Keeper();
            //waits for an xmlString from the topic and for a request from the queue on the onMessage function
            String xmlString = keeper.receive();
            try
            {
                //if the XML is valid
                if (isValidXML(xmlString, "skeleton.xsd")) {
                    //we can unmarshal it
                    keeper.unmarshalXML(xmlString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
