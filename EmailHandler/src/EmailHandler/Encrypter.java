package EmailHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

/**
 * A class that does AES encryption of encryption and decryption.
 * Ideally the secret bytes for session key should be generated each 
 * time using PBE.
 * Need to create key store instead of using normal file.
 * @author Takuma Sato
 */
public class Encrypter {

    private static final String PUBLIC_FILE_NAME = "public.key";
    private static final String PRIVATE_FILE_NAME = "private.key";
    private static final String ALGORITHM = "RSA";
    private static int KEY_LENGTH = 2048;
    private static KeyPair keyPair;
    
    private static final byte[] KEY_BYTES = {
        51, 50, 7, -19, 120, 111, -110, 52, 9, -21, -6, -15, -95, 117, 36, -89
    };
    private static final byte[] IV_BYTES = {
        1, -2, 3, -4, 5, -6, 7, -8, 9, -10, 11, -12, 13, -14, 15, -16
    };
    
    private static SecretKeySpec key;
    private static IvParameterSpec initVector;
    
    /**
     * generates "random" session key
     */
    private static void generateSessionKey(){
        key = new SecretKeySpec(KEY_BYTES, "AES");
        initVector = new IvParameterSpec(IV_BYTES);
    }
    
    /**
     * Check if the private key file exists
     * @return true if registered
     */
    public static boolean isRegistered() {
        File keyFile = new File(PUBLIC_FILE_NAME);
        if(keyFile.exists() && keyFile.isFile()){
            return true;
        }
        return false;
    }
    
    /**
     * Decrypt a given text with this machines private key
     * @param encryptedText
     * @return session key in a String format
     */
    public static String decrypt(String encryptedText){
        byte[] text = encryptedText.getBytes();
        byte[] dectyptedText = null;
        try {
          // get an RSA cipher object and print the provider
          final Cipher cipher = Cipher.getInstance(ALGORITHM);
          // decrypt the text using the private key
          if(keyPair == null){
              loadKeyPair();
          }
          cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
          dectyptedText = cipher.doFinal(text);                 
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException ex) {
        }
        return new String(dectyptedText);
    }
    
    /**
     * Encrypt a plaintext with a session key used for a particular receiver
     * @param rawText
     * @param to with out "@gmai.com"
     * @return encrypted text
     */
    public static String encrptWithSessionKey(String rawText, String to) {
        generateSessionKey();
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key,initVector);
            byte[] plainText = rawText.getBytes();
            byte[] cipherText = cipher.doFinal(plainText);
            encryptedText = (new BASE64Encoder().encode(cipherText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | InvalidAlgorithmParameterException | 
                IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encryptedText;
    }
    
    /**
     * Decrypt a plaintext with a session key used for a particular sender.
     * @param rawText
     * @param from
     * @return decrypted text
     */
    public static String decrptWithSessionKey(String rawText, String from) {
        generateSessionKey();
        byte[] decipheredText = null;
        try {
            byte[] cipherText = new BASE64Decoder().decodeBuffer(rawText);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key,initVector);
            decipheredText = cipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | InvalidAlgorithmParameterException | 
                IllegalBlockSizeException | BadPaddingException | IOException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String(decipheredText);
    }
    
    /**
     * Extract the Public key from a Base64 encoded string
     * @param encodedPublicKey
     * @return public key
     */
    private static PublicKey recoverPublicKey(String encodedPublicKey){
        PublicKey publicKey = null;
        try {
            byte[] decoded = new BASE64Decoder().decodeBuffer(encodedPublicKey);
            publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(
                    new X509EncodedKeySpec(decoded));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return publicKey;
    }
    
    /**
     * Generate a new Key pair and stores them securely using X509Encode
     * @see http://snipplr.com/view/18368/
     * @throws IOException 
     */
    public static void saveKeyPair() throws IOException {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
            kpg.initialize(KEY_LENGTH);
            keyPair = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                        publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(PUBLIC_FILE_NAME);
        fos.write(x509EncodedKeySpec.getEncoded());
        fos.close();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                        privateKey.getEncoded());
        fos = new FileOutputStream(PRIVATE_FILE_NAME);
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        fos.close();
    }
    
    /**
     * Loads the key pair from a file using X509EncodedKeySpec
     * @see http://snipplr.com/view/18368/
     */
    public static void loadKeyPair(){
        FileInputStream fis = null;
        try {
            // Read Public Key.
            File filePublicKey = new File(PUBLIC_FILE_NAME);
            fis = new FileInputStream(PUBLIC_FILE_NAME);
            byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
            fis.read(encodedPublicKey);
            fis.close();
            // Read Private Key.
            File filePrivateKey = new File(PRIVATE_FILE_NAME);
            fis = new FileInputStream(PRIVATE_FILE_NAME);
            byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
            fis.read(encodedPrivateKey);
            fis.close();
            // Generate KeyPair.
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                            encodedPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                            encodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            keyPair = new KeyPair(publicKey, privateKey);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(fis != null)
                    fis.close();
            } catch (IOException ex) {
                Logger.getLogger(Encrypter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
}
