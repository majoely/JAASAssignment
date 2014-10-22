package EmailHandler;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Really simple authenticater for loggin in to gmail account.
 * @author Takuma Sato
 */
public class SimpleAuthenticator extends Authenticator
{
   private PasswordAuthentication authentication;
   private final String name;
   private final char[] pass;
   
   public SimpleAuthenticator(String name, char[] pass)
   {  
       this.name = name;
       this.pass = pass;
       this.authentication = new PasswordAuthentication(name, String.valueOf(pass));
   }

   @Override
   public PasswordAuthentication getPasswordAuthentication()
   {  
      return authentication;
   }

   /**
    * @return account name with out "@gmail.com"
    */
    public String getName() {
        return this.name;
    }
    
    /**
     * Testing purpose
     * @return password in char[]
     */
    public char[] getPass(){
        return pass;
    }
}
