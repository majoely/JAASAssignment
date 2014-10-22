/**
 * Class that handles recieving messages from gmail
 * Only fetches those mails that are unread and has the prefix "!+!".
 */
package EmailHandler;


import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

public class EMailReciever
{
    //Gmail
    private static final String HOST = "imap.gmail.com";
    private static final String PROTOCOL = "mail.store.protocol";
    private static final String SERVER_NAME = "imaps";
    //Aut webmail
    //private static final String HOST = "pop.aut.ac.nz";
    //private static final String KEY = "mail.pop3.host";
    //private static final String SERVER = "pop3";
    private Session session;
    private Store store;
    private String reciverName;

    public EMailReciever() {
        Properties properties = System.getProperties();
        properties.setProperty(PROTOCOL, SERVER_NAME);
        session = Session.getDefaultInstance(properties,null);
    }
    
    /**
     * Get the email address with out the suffix "@gmail.com"
     * @return the account name
     */
    public String getReceiverName(){
        return this.reciverName;
    }
    
    /**
     * Start the session with a given account name and password
     * @param account name
     * @param password
     * @return true if successful
     */
    public boolean login(final String name, final char [] pass){
        this.reciverName = name;
        try{  
           store = session.getStore(SERVER_NAME);
        }
        catch (NoSuchProviderException e){  
            System.err.println("Mail provider not available: " + e);
            return false;
        }
        try {
            store.connect(HOST,reciverName,String.valueOf(pass));
            return true;
        } catch (MessagingException ex) {
            System.err.println("Login failed: " + ex);
            return false;
        }
    }
   
    /**
     * Retrieves the mails that are unread in the "inbox" folder which has
     * prefix of "!+!" that indicates that the message was from this program
     * @return HashMap of Subject:Body;
     */
    public HashMap<String,String> getMails(){
        HashMap<String,String> mails = new HashMap();
        Folder folder = null;
        try {
            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            //Find unread messages
            Message[] messages = folder.search(new FlagTerm(new Flags(Flag.SEEN),false));
            //Futher search for messages that has certain prefix
            final String PREFIX = "!+!";
            messages = folder.search(new SearchTerm() {
                @Override
                public boolean match(Message msg) {
                    try {
                        if(msg.getSubject().startsWith(PREFIX)){
                            return true;
                        }
                    } catch (MessagingException ex) {
                        System.err.print(ex);
                    }
                    return false;
                }
            }, messages);
            //Now put them in a message map
            for(Message m : messages){
                String body;
                Object content = m.getContent();
                if(content instanceof Multipart){
                    Multipart mp = (Multipart) m.getContent();
                    body = (String) mp.getBodyPart(0).getContent();
                }else{
                    body = m.getContent().toString();
                }
                mails.put(m.getSubject().replace(PREFIX, ""), body);
            }
        } catch (MessagingException ex) {
            System.err.print("Error retrieving mails: " + ex);
        } catch (IOException ex) {
            Logger.getLogger(EMailReciever.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return mails;
    }
    
    /**
     * Close the store
     */
    public void logout(){
        try{
            if (store != null)
               store.close();
         }
         catch (MessagingException e)
         {} // ignore
    }
    
   
   

}
