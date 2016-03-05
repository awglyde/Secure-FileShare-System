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

    private static final String PROVIDER = "BC";
    private String encryptionAlgorithm = "";

    public EncryptionSuite (String algorithmName)
    {
        Security.addProvider(new BouncyCastleProvider());
        this.encryptionAlgorithm = algorithmName;
    }

    /*
        Generates and returns a secret key based off the algorithm passed in and
        the size of the key requested.
    */
    public Key generateKey(int keyLength) throws Exception
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(this.encryptionAlgorithm, PROVIDER);
        keyGenerator.init(keyLength);
        return keyGenerator.generateKey();
    }

    /*
        Generates a key pair of a private and public key based off the algorithm passed in.
    */
    public KeyPair generateKeyPair() throws Exception
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.encryptionAlgorithm, PROVIDER);
        return keyPairGenerator.generateKeyPair();
    }

    public byte[] generateSignature(byte[] messageBytes, PrivateKey privateKey) throws Exception
    {
        Signature signature = Signature.getInstance(this.encryptionAlgorithm, PROVIDER);
        signature.initSign(privateKey);
        signature.update(messageBytes);
        return signature.sign();
    }

    public boolean verifySignature(byte[] signatureBytes, byte[] messageBytes, PublicKey publicKey) throws Exception
    {
        Signature signature = Signature.getInstance(this.encryptionAlgorithm, PROVIDER);
        signature.initVerify(publicKey);
        signature.update(messageBytes);
        return signature.verify(signatureBytes);
    }

    /*
        Encrypts or Decrypts an input stream based on the algorithm passed in and the
        mode determined.
    */
    public byte[] encryptOrDecrypt(byte[] inputBytes, Key key, int mode) throws Exception
    {

        Cipher cipher = Cipher.getInstance(this.encryptionAlgorithm, PROVIDER);
        cipher.init(mode, key);

        byte[] outputBytes = new byte[cipher.getOutputSize(inputBytes.length)];
        int outputLength = cipher.update(inputBytes, 0, inputBytes.length, outputBytes, 0);
        cipher.doFinal(outputBytes, outputLength);
        return outputBytes;
    }
}
