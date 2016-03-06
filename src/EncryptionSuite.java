/*
    This class is to be able to decrypt and encrypt messages from the command line
    using AES, Blowfish, and RSA encryption.
*/
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;
import java.security.MessageDigest;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.Security;
import java.security.SecureRandom;
import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.Base64;
import java.util.Base64.Encoder;


public class EncryptionSuite
{

    public static final String ENCRYPTION_AES = "AES";
    public static final String ENCRYPTION_BLOWFISH = "Blowfish";
    public static final String ENCRYPTION_RSA = "RSA";
    public static final String SIGNATURE_SHA512_RSA = "SHA512WithRSAEncryption";
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final String PROVIDER = "BC";
	public static final int encrypt = Cipher.ENCRYPT_MODE;
	public static final int decrypt = Cipher.DECRYPT_MODE;
    private String algorithmName = "";
    private int rsaKeyLength = 2048;
    private int aesKeyLength = 128;
    private Key encryptionKey = null;
    private Key decryptionKey = null;

    public EncryptionSuite (String algorithmName, Key publicKey, Key privateKey) throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());
        this.algorithmName = algorithmName;
        this.setEncryptionKey(publicKey);
        this.setDecryptionKey(privateKey);
    }

    public EncryptionSuite (String algorithmName, Key symmetricKey) throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());
        this.algorithmName = algorithmName;
        this.setEncryptionKey(symmetricKey);
        this.setDecryptionKey(symmetricKey);
    }

    public EncryptionSuite (String algorithmName) throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());
        this.algorithmName = algorithmName;
        if (this.algorithmName.equals(ENCRYPTION_AES))
            generateKey();
        else
            generateKeyPair();
    }

	public EncryptionSuite () throws Exception
	{
        Security.addProvider(new BouncyCastleProvider());
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

	public String getAlgorithmName()
	{
		return this.algorithmName;
	}

	public void setAlgorithmName(String algorithmName)
	{
		this.algorithmName = algorithmName;
	}


    /*
        Generates and returns a secret key based off the algorithm passed in and
        the size of the key requested.
    */
    private void generateKey() throws Exception
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(this.algorithmName, PROVIDER);
        keyGenerator.init(this.aesKeyLength);
        this.encryptionKey = keyGenerator.generateKey();
        this.decryptionKey = this.encryptionKey;
    }

    /*
        Generates a key pair of a private and public key based off the algorithm passed in.
    */
    public void generateKeyPair() throws Exception
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.algorithmName, PROVIDER);
        keyPairGenerator.initialize(rsaKeyLength);
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

    public byte[] hashString(String string) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);

        md.update(string.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] digest = md.digest();
        return digest;
    }

    public byte[] generateSalt() throws Exception
    {
        SecureRandom prng = new SecureRandom();
        byte[] salt = new byte[11];
        prng.nextBytes(salt);
        return salt;
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

        Cipher cipher = Cipher.getInstance(this.algorithmName, PROVIDER);
        cipher.init(mode, key);

        byte[] outputBytes = new byte[cipher.getOutputSize(inputBytes.length)];
        int outputLength = cipher.update(inputBytes, 0, inputBytes.length, outputBytes, 0);
        cipher.doFinal(outputBytes, outputLength);
        return outputBytes;
    }

    public Cipher getCipher(int mode) throws Exception
    {
        Key key = null;
        if (mode == Cipher.ENCRYPT_MODE)
            key = this.encryptionKey;
        else
            key = this.decryptionKey;

		// ADD INITIALZIATION VECTOR
        Cipher cipher = Cipher.getInstance(this.algorithmName, PROVIDER);
        cipher.init(mode, key);

		return cipher;
    }

    public String encryptionKeyToString()
    {
        String stringKey = "";
        Encoder encoder = Base64.getEncoder();

        if (this.encryptionKey != null)
            stringKey = encoder.encodeToString(this.encryptionKey.getEncoded());
        return stringKey;
    }

	public Envelope getEncryptedMessage(Envelope message) throws Exception
	{
		SealedObject encMessage = new SealedObject(message, this.getCipher(encrypt));
		Envelope wrappedEncMessage = new Envelope("ENCRYPTEDENV"+this.algorithmName);
		wrappedEncMessage.addObject(encMessage);

		return wrappedEncMessage;
	}

	public Envelope getDecryptedMessage(Envelope encMessage) throws Exception
	{
		if (encMessage.getMessage().equals("ENCRYPTEDENV"+this.algorithmName))
		{
			SealedObject encResponse = (SealedObject)encMessage.getObjContents().get(0);
			return (Envelope)encResponse.getObject(this.getCipher(decrypt));
		}
		else
		{
			return null;
		}
	}
}
