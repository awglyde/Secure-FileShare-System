/*
    This class is to be able to decrypt and encrypt messages from the command line
    using AES, Blowfish, and RSA encryption.
*/
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.crypto.Mac;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.security.MessageDigest;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.Security;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;

public class EncryptionSuite
{

    public static final String PASSWORD_INFO = "A user password must have the following: \n" +
                                                "\t1. At least one digit. \n" +
                                                "\t2. At least one lowercase letter. \n" +
                                                "\t3. At least one uppercase letter. \n" +
                                                "\t4. At least a special character: @, #, $, %, ^, &, +, = \n" +
                                                "\t5. No whitespace \n" +
                                                "\t6. At least 8 characters in length.\n";

    public static final String ENCRYPTION_AES = "AES";
    public static final String ENCRYPTION_BLOWFISH = "Blowfish";
    public static final String ENCRYPTION_RSA = "RSA";
    public static final String SIGNATURE_SHA512_RSA = "SHA512WithRSAEncryption";
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final String PROVIDER = "BC";
	public static final String HMAC_ALGORITHM = "HmacSHA1";

	public static final int encrypt = Cipher.ENCRYPT_MODE;
	public static final int decrypt = Cipher.DECRYPT_MODE;

    private String algorithmName = "";
    private int rsaKeyLength = 4096;
    private int aesKeyLength = 128;
    private Key encryptionKey = null;
    private Key decryptionKey = null;

    public EncryptionSuite (String publicConfig, String privateConfig) throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());
        this.algorithmName = EncryptionSuite.ENCRYPTION_RSA;

        if(publicConfig != null || !publicConfig.equals(""))
            this.encryptionKey = (Key)loadPublicKey(publicConfig);

        if(privateConfig != null || !privateConfig.equals(""))
            this.decryptionKey = (Key)loadPrivateKey(privateConfig);


        // if both keys are null, generate new keys and save them to the correct files
        if((this.encryptionKey == null && this.decryptionKey == null)
            && !(privateConfig.equals("")))
        {
            generateKeyPair();
            saveKey(publicConfig, this.encryptionKey);
            saveKey(privateConfig, this.decryptionKey);
        }
    }

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
		SecureRandom prng = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(this.algorithmName, PROVIDER);
        keyGenerator.init(this.aesKeyLength, prng);
        this.encryptionKey = keyGenerator.generateKey();
        this.decryptionKey = this.encryptionKey;
    }

    /*
        Generates a key pair of a private and public key based off the algorithm passed in.
    */
    public void generateKeyPair() throws Exception
    {
		SecureRandom prng = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.algorithmName, PROVIDER);
        keyPairGenerator.initialize(rsaKeyLength, prng);
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

	public byte[] generateHmac(byte[] messageBytes) throws Exception
	{
	    Mac mac = Mac.getInstance(HMAC_ALGORITHM);
	    mac.init(this.encryptionKey);

	    // byte[] digest = mac.doFinal(messageBytes);
		// return digest;
	    return mac.doFinal(messageBytes);
	}

    public byte[] hashString(String string) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);

        md.update(string.getBytes("UTF-8"));
        byte[] digest = md.digest();
        return digest;
    }

    public byte[] hashBytes(byte[] bytes) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);

        md.update(bytes); // Change this to "UTF-16" if needed
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

    public byte[] saltAndHashPassword(String password, byte[] tempSalt) throws Exception
    {
        // Concatenate salt and password
        String pwAndSalt = password+new String(tempSalt, "UTF-8");
        return this.hashBytes(pwAndSalt.getBytes());
    }

    public boolean verifyUserPassword(String password, byte[] saltedPwHash, byte[] salt) throws Exception
    {
        if (password == null || saltedPwHash == null || salt == null)
            return false;
        else
            return Arrays.equals(this.saltAndHashPassword(password, salt), saltedPwHash);
    }

    public boolean verifyChallenge(byte[] challenge, byte[] completedChallenge) throws Exception
    {
        if (Arrays.equals(this.hashBytes(challenge), completedChallenge))
            return true;
        else
            return false;
    }

    public Cipher getCipher(int mode) throws Exception
    {
        // Make a new secure pseudo random number generator
		SecureRandom prng = new SecureRandom();

        Key key = null;
        if (mode == Cipher.ENCRYPT_MODE)
            key = this.encryptionKey;
        else
            key = this.decryptionKey;

        // Create a new cipher
        Cipher cipher = Cipher.getInstance(this.algorithmName, PROVIDER);
        // Init with a prng to ensure the encryptions are random every time
		cipher.init(mode, key, prng);

		return cipher;
    }

    // Prints encryption key formatted in Hex as a string
    public String encryptionKeyToString() throws Exception
    {
        String encoded = "";

        if (this.encryptionKey != null)
            encoded = DatatypeConverter.printHexBinary(this.encryptionKey.getEncoded());

        return encoded;

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

    private void saveKey(String file, Key key) throws Exception
    {
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(key.getEncoded());
        fout.close();
    }

    private PrivateKey loadPrivateKey(String file) throws Exception
    {
        PrivateKey privateKey = null;
        File f = new File(file);
        if(f.exists())
        {
            byte[] keyBytes = new byte[(int)f.length()];
            FileInputStream finp = new FileInputStream(f);
            finp.read(keyBytes);
            finp.close();
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        }
        return privateKey;
    }

    private PublicKey loadPublicKey(String file) throws Exception
    {
        PublicKey publicKey = null;
        File f = new File(file);
        if(f.exists())
        {
            byte[] keyBytes = new byte[(int)f.length()];
            FileInputStream finp = new FileInputStream(f);
            finp.read(keyBytes);
            finp.close();
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        }
        return publicKey;
    }

    public static boolean verifyPassword(String username, String password)
    {
        boolean valid = true;

        /*
            A user password must have the following:
                1. At least one digit.
                2. At least one lowercase letter.
                3. At least one uppercase letter.
                4. At least a special character: @, #, $, %, ^, &, +, =
                5. No whitespace
                6. At least 8 characters in length.

            A user password cannot be equal to your username and cannot equal
            "password" as well.
        */
        Pattern validPassword = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        Matcher match = validPassword.matcher(password);

        // if no match is found, then the password isn't created
        if(!match.find() || password.equals(username) || password.equals("password"))
            valid = false;

        return valid;
    }
}
