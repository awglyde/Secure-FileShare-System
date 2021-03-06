import java.security.Key;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import javax.xml.bind.DatatypeConverter;

public class Session
{
    EncryptionSuite hmacKey;
    EncryptionSuite aesSessionKey;
    EncryptionSuite targetPubKey;
    byte[] nonce;
    int sequenceNum = -1;
    String userName = "";
    Integer authCode = -1;

    public Session()
    {
    }

    public boolean verifyAuthCode(Integer authCodeInput)
    {
        return this.authCode.equals(authCodeInput);
    }

    public void setAuthCode(Integer authCode)
    {
        this.authCode = authCode;
    }

    public void setUser(String userName)
    {
        this.userName = userName;
    }

    public String getUser()
    {
        return this.userName;
    }

    public void setSequenceNum(int sequenceNum)
    {
        this.sequenceNum = sequenceNum;
    }

    public int getSequenceNum()
    {
        return this.sequenceNum;
    }

    public int incrementSequenceNum()
    {
        this.sequenceNum = this.sequenceNum + 1;

        return this.sequenceNum;
    }

    public boolean verifySequenceNumber(int newSequenceNum)
    {
        return this.sequenceNum + 1 == newSequenceNum;
    }

    public void setNonce(byte[] nonce)
    {
        this.nonce = nonce;
    }

    public byte[] getNonce()
    {
        return this.nonce;
    }

    public void setHmacKey(Key hmacKey) throws Exception
    {
        this.hmacKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES, hmacKey);
    }

    public void setHmacKey() throws Exception
    {
        this.hmacKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);
    }

    public void setAESKey() throws Exception
    {
        this.aesSessionKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);
    }

    public void setAESKey(Key sessionKey) throws Exception
    {
        this.aesSessionKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES, sessionKey);
    }

    public void setTargetKey(Key targetPubKey) throws Exception
    {
        this.targetPubKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, targetPubKey);
    }

    public EncryptionSuite getTargetKey()
    {
        return this.targetPubKey;
    }

    public EncryptionSuite getHmacKey()
    {
        return this.hmacKey;
    }

    public EncryptionSuite getAESKey()
    {
        return this.aesSessionKey;
    }

    public Envelope getEncryptedMessageTargetKey(Envelope message) throws Exception
    {
        return this.targetPubKey.getEncryptedMessage(message);
    }

    public Envelope getDecryptedMessage(Envelope encryptedMessage) throws Exception
    {
        return this.aesSessionKey.getDecryptedMessage(encryptedMessage);
    }

    public Envelope getEncryptedMessage(Envelope message) throws Exception
    {
        return this.aesSessionKey.getEncryptedMessage(message);
    }

    public byte[] generateHmac(byte[] messageBytes) throws Exception
    {
        return this.hmacKey.generateHmac(messageBytes);
    }

    public byte[] generateHmac(Envelope message) throws Exception
    {
        byte[] messageBytes = this.getBytes(message);

        return this.generateHmac(messageBytes);
    }

    public byte[] getBytes(Object message) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        out.writeObject(message);

        return baos.toByteArray();
    }

    public Object getObjectFromBytes(byte[] bytes) throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }

    public boolean verifyHmac(byte[] hmacMesssage, byte[] messageBytes) throws Exception
    {
        return this.hmacKey.verifyHmac(hmacMesssage, messageBytes);
    }

    public byte[] completeChallenge() throws Exception
    {
        return this.aesSessionKey.hashBytes(this.nonce);
    }

    public Envelope clientSequenceNumberHandler(Envelope message)
    {
        int newSequenceNum = (int)message.removeObject(0);

        if (!this.verifySequenceNumber(newSequenceNum))
        {
            System.out.println("Sequence number out of order! Session may be compromised.");

            return new Envelope("FAIL");
        }
        this.setSequenceNum(newSequenceNum);
        this.incrementSequenceNum();

        return message;
    }

    public Envelope serverSequenceNumberHandler(Envelope message)
    {
        Envelope oldMessage = message;

        if (message.getObjContents().get(0) != null)
        {
            int sequenceNum = (int)message.removeObject(0);
            if (this.getSequenceNum() == -1 || this.verifySequenceNumber(sequenceNum))
            {
                this.setSequenceNum(sequenceNum);
                this.incrementSequenceNum();

                return message;
            }
            else
            {
                System.out.println("Sequence number out of order! Session may be compromised.");

                return new Envelope("FAIL");
            }
        }

        return new Envelope("FAIL");
    }

    public Envelope clientHmacVerify(Envelope message) throws Exception
    {
        byte[] hmac = (byte[])message.removeObject(message.getObjContents().size() - 1);

        if (!this.verifyHmac(hmac, this.getBytes(message)))
        {
            System.out.println("HMAC not verified! Session may be compromised.");

            return new Envelope("FAIL");
        }

        return message;
    }

    public Envelope serverHmacVerify(Envelope message) throws Exception
    {
        byte[] hmac = (byte[])message.removeObject(message.getObjContents().size() - 1);

        if (!this.verifyHmac(hmac, this.getBytes(message)))
        {
            System.out.println("HMAC not verified! Session may be compromised.");

            return new Envelope("FAIL");
        }

        return message;
    }
}
