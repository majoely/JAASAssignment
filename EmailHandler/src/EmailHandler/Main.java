package EmailHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author Takuma
 */
public class Main {
    
    
    
    public static void main(String[] args) {
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
    }
}
