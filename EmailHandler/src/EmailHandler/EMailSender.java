package EmailHandler;

/**
   A class that uses JavaMail API for sending email messages.
   @author Takuma Sato
*/
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EMailSender
{
   private final String HOST = "smtp.gmail.com";
   private final String PORT = "465";
   private final String PROVIDER = "smtps";
   private Session session;
   private SimpleAuthenticator auth;
   //Some indicaters used in the subject
   private final String PREFIX = "!+!";
   
   public EMailSender(SimpleAuthenticator auth){
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.port", PORT);
        properties.put("mail.smtp.auth", "true");
        this.session = Session.getInstance(properties, auth);
        this.auth = auth;
   }
   
   /**
    * Sends the message according to the parameters
    * The subject will contain the prefix "!+!" to indicate that the 
    * message was produced by this program. If encrypt and/or authent option 
    * is true, then it further modifies the subject indicate it.
    * @param from omit "@gmail.com"
    * @param to omit "@gmail.com"
    * @param subject plaintext
    * @param body plaintext
    * @param encrypt true if you want encryption
    * @param authent true if you want authentication
    * @return true if sent successfully
    */
   public boolean sendMessage(String from, String to, String subject, 
           String body, boolean encrypt, boolean authent){
        MimeMessage message = new MimeMessage(session);
        //Prepare To and From
        try{
            InternetAddress fromAddress = new InternetAddress(from);
            InternetAddress toAddress = new InternetAddress(to + "@gmail.com");
            message.setFrom(fromAddress);
            message.setRecipient(Message.RecipientType.TO,toAddress);
        }catch (AddressException e){
            System.err.println("Address exception while preparing addresses: " + e);
        }catch (MessagingException e){
            System.err.println("Messaging exception while preparing addresses: " + e);
        }
        //Prepare subject
        String modifiedSubject = PREFIX + from + " : " + subject;
        // send the message
        try{
            message.setSubject(modifiedSubject);
            message.setText(body);
            Transport transport = session.getTransport(PROVIDER);
            transport.connect(HOST, Integer.parseInt(PORT),auth.getName(),String.valueOf(auth.getPass()));
            transport.sendMessage(message, message.getAllRecipients());
            return true;
        }catch (MessagingException e){
            System.err.println("Messaging exception: " + e);
            return false;
        }
    }
}
