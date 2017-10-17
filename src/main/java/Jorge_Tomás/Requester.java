package Jorge_Tomás;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Scanner;



public class Requester {
    private Destination d;
    private ConnectionFactory cf;


    public Requester() throws NamingException {
        this.d = InitialContext.doLookup("jms/queue/QueueExample");
        this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
    }

    public void send(String request, String clientID) {

        try (JMSContext cntxt = this.cf.createContext("joao", "br1o+sa*")) {
            //set the client ID
            cntxt.setClientID(clientID);
            //create message producer
            JMSProducer sendToRequestQueue = cntxt.createProducer();
            //initialize textMessage
            TextMessage textMessage = cntxt.createTextMessage();
            //set the text with our request
            textMessage.setText(request);
            //create temporary queue for the reply
            Destination tempDest = cntxt.createTemporaryQueue();
            //create a consumer to listen to that queue
            JMSConsumer responseConsumer = cntxt.createConsumer(tempDest);
            //in order for recipient of the message to know the temporary queue to where he has to reply
            //we set the queue destination in the textMessage
            textMessage.setJMSReplyTo(tempDest);
            //sends the textMessage
            sendToRequestQueue.send(d,textMessage);
            //waits for the response
            String response = responseConsumer.receive().getBody(String.class);
            //prints the response
            System.out.println(response);
        } catch (JMSException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        try {
            Requester requester = new Requester();
            System.out.println("Insert your ID: ");
            Scanner sc = new Scanner(System.in);
            String clientID = sc.nextLine();
            while (true) {
                //build the request
                System.out.println("Insert query or insert 'quit' to quit: ");
                System.out.println("Ex: brand = bmw or price < 15000");
                sc = new Scanner(System.in);
                String input = sc.nextLine();

                if (!(input.indexOf("quit") < 0)) {
                    break;
                } else {
                    requester.send(input.toLowerCase(), clientID);
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

}