package Jorge_Tomás;


import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Created by jorgearaujo on 26/09/2017.
 */


public class TopicSender {
    private ConnectionFactory cf;
    private Topic tpc;


    public TopicSender() throws NamingException {
        this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");

        //lookup for Topic
        this.tpc = (Topic) InitialContext.doLookup("jms/topic/TopicExample");

    }

    public void sendToTopic(String msg) throws NamingException {

        try (JMSContext cntx = this.cf.createContext("joao", "br1o+sa*")) {

            //topic producer
            JMSProducer prod = cntx.createProducer();
            //send message to topic
            prod.send(tpc,msg);
        }
    }

}