/*
    This class is to be able to decrypt and encrypt messages from the command line
    using AES, Blowfish, and RSA encryption.
*/
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.Security;
import java.util.Scanner;

public class EncryptionSuite
{

    private static final String ENCRYPTION_AES = "AES";
    private static final String ENCRYPTION_BLOWFISH = "Blowfish";
    private static final String ENCRYPTION_RSA = "RSA";
    private static final String SIGNATURE_SHA512_RSA = "SHA512WithRSAEncryption";
    private static final String PROVIDER = "BC";
    private String encryptionAlgorithm = "";
    private int keyLength = 256;
    private Key encryptionKey = null;
    private Key decryptionKey = null;

    public EncryptionSuite (String algorithmName) throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());
        this.encryptionAlgorithm = algorithmName;
        if (this.encryptionAlgorithm.equals(ENCRYPTION_AES))
            generateKey();
        else
            generateKeyPair();
    }

    public Key getEncryptionKey()
    {
        return this.encryptionKey;
    }

    public Key getDecryptionKey()
    {
        return this.decryptionKey;
    }

    public void setEncryptionKey(Key key)
    {
        this.encryptionKey = key;
    }

    public void setDecryptionKey(Key key)
    {
        this.decryptionKey = key;
    }
    
    /*
        Generates and returns a secret key based off the algorithm passed in and
        the size of the key requested.
    */
    private void generateKey() throws Exception
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(this.encryptionAlgorithm, PROVIDER);
        keyGenerator.init(this.keyLength);
        this.encryptionKey = keyGenerator.generateKey();
        this.decryptionKey = this.encryptionKey;
    }

    /*
        Generates a key pair of a private and public key based off the algorithm passed in.
    */
    public void generateKeyPair() throws Exception
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.encryptionAlgorithm, PROVIDER);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.encryptionKey = (Key)keyPair.getPublic();
        this.decryptionKey = (Key)keyPair.getPrivate();
    }

    public byte[] generateSignature(byte[] messageBytes) throws Exception
    {
        Signature signature = Signature.getInstance(SIGNATURE_SHA512_RSA, PROVIDER);
        signature.initSign((PrivateKey)this.decryptionKey);
        signature.update(messageBytes);
        return signature.sign();
    }

    public boolean verifySignature(byte[] signatureBytes, byte[] messageBytes) throws Exception
    {
        Signature signature = Signature.getInstance(SIGNATURE_SHA512_RSA, PROVIDER);
        signature.initVerify((PublicKey)this.encryptionKey);
        signature.update(messageBytes);
        return signature.verify(signatureBytes);
    }

    /*
        Encrypts or Decrypts an input stream based on the algorithm passed in and the
        mode determined.
    */
    public byte[] encryptOrDecrypt(byte[] inputBytes, int mode) throws Exception
    {
        Key key = null;
        if (mode == Cipher.ENCRYPT_MODE)
            key = this.encryptionKey;
        else
            key = this.decryptionKey;

        Cipher cipher = Cipher.getInstance(this.encryptionAlgorithm, PROVIDER);
        cipher.init(mode, key);

        byte[] outputBytes = new byte[cipher.getOutputSize(inputBytes.length)];
        int outputLength = cipher.update(inputBytes, 0, inputBytes.length, outputBytes, 0);
        cipher.doFinal(outputBytes, outputLength);
        return outputBytes;
    }
}
