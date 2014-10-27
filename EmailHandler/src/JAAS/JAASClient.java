package JAAS;

/**
   A class that demonstrates how JAAS can be used to authenticate a
   user and authorize whether the user can execute a PrivilegedAction
   To test the authentication feature run by command line statement:
   
     java -Djava.security.auth.login.config==JAASExample.config JAASExampleClient

   See the class SimpleLoginModule for valid user name and password.
   To test the authorization feature for executing a privileged
   action first create the following JAR files (use JDK\bin\jar):

     jar -cvf Simple.jar simplejaasmodule/SimpleLoginModule.class simplejaasmodule/SimplePrincipal.class

   and:

     jar -cvf JAASExample.jar JAASExampleClient.class JAASExampleCallbackHandler.class

   Then specify the JAASExample.policy security policy file and set
   the CLASSPATH so that the JAR files can be loaded:

     java -classpath JAAS.jar:Simple.jar -Djava.security.manager -Djava.security.policy=JAAS.policy -Djava.security.auth.login.config=JAAS.config build/JAAS.JAASClient

   @author Andrew Ensor
*/
import EmailHandler.EMailReciever;
import EmailHandler.EMailSender;
import EmailHandler.SimpleAuthenticator;
//import java.io.FileWriter;
//import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class JAASClient implements PrivilegedAction<Boolean>
{
   // PrivilegedAction run method that contains security-sensitive
   // operation and has any specified generic return type
   public Boolean run()
   {  /*try
      {  FileWriter fw = new FileWriter("localfile.txt");
         fw.write("JAAS says hello on local drive");
         fw.close();
         return true; // worked
      }
      catch (IOException e)
      {  System.err.println("IO Exception while writing file: " + e);
         return false; // failed
      }*/
        try {
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Enter your email address (omit @gmail.com) :");
            String accountName = keyboard.nextLine();
            System.out.println("Enter your password");
            char[] pass = keyboard.nextLine().toCharArray();
            //Log in happens through the constructer
            EMailSender sender = new EMailSender(new SimpleAuthenticator(accountName, pass));
            //Send test email
            System.out.println("Who are you sending to?(omit @gmail.com):");
            String reciverName = keyboard.nextLine();
            String subject = "Testing Email Handler";
            String messageContent = "Unicorn on a rainbow rainbow";
            //Currently encryption/hash option is not available ):
            System.out.println("Sending message....");
            sender.sendMessage(accountName, reciverName, subject, messageContent,
                    false, false);
            System.out.println("Sent!!");
            //Recieve message
            System.out.println("Recieving message...");
            EMailReciever receiver = new EMailReciever();
            receiver.login(accountName, pass);
            //This method will return all message with the subject starting with "!+!"
            //in a format HashMap<subject,content>
            HashMap<String, String> mails = receiver.getMails(); 
            //Display messages
            for(String sub : mails.keySet()){
                System.out.println(sub + " : " + mails.get(sub));
            }
            //Erase password
            Arrays.fill(pass,' ');
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
   }
   
   public static void main(String[] args)
   {  PrivilegedAction<Boolean> privilegedAction
         = new JAASClient();
      CallbackHandler callbackHandler=new JAASCallbackHandler();
      try
      {  System.out.println("Creating login context");
         LoginContext lc
            = new LoginContext("SimpleLogin", callbackHandler);
         System.out.println("Logging in");
         lc.login();
         Subject subject = lc.getSubject(); // authenticated subject
         System.out.println("Executing security-sensitive operation");
         // execute the privileged action as specified subject with
         // no protection domains taken from current thread's context
         boolean success = (Boolean)Subject.doAsPrivileged(subject,
            privilegedAction, null);
         System.out.println("Operation " +(success?"was":"not")
            + " successful");
         System.out.println("Logging out");
         lc.logout();
      }
      catch (LoginException e)
      {  System.err.println("Login Exception: " + e);
        e.printStackTrace();
      }
      catch (SecurityException e)
      {  System.err.println("Security Exception: " + e);
        e.printStackTrace();
      }
   }
}
