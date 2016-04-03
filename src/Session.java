import java.security.Key;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
public class Session
{
	EncryptionSuite hmacKey;
	EncryptionSuite aesSessionKey;
	EncryptionSuite targetPubKey;
    byte[] nonce;
	int sequenceNum = -1;

	public Session()
	{

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
		this.sequenceNum = this.sequenceNum+1;
		return this.sequenceNum;
	}

	public boolean verifySequenceNumber(int newSequenceNum)
	{
		return (this.sequenceNum+1 == newSequenceNum);
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
        byte[] messageBytes = this.getEnvelopeBytes(message);
		return this.generateHmac(messageBytes);
	}

	public byte[] getEnvelopeBytes(Envelope message) throws Exception
	{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(message);
        return baos.toByteArray();
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
        int newSequenceNum = (int)message.getObjContents().get(0);
        message.removeObject((int)message.getObjContents().get(0));
        if (!this.verifySequenceNumber(newSequenceNum))
        {
            System.out.println("Sequence number out of order! Session may be compromised. Exiting.");
            System.exit(0);
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
            int sequenceNum = (int)message.getObjContents().get(0);
            message.removeObject(message.getObjContents().get(0));
            if (this.getSequenceNum() == -1 || this.verifySequenceNumber(sequenceNum))
            {
                this.setSequenceNum(sequenceNum);
                this.incrementSequenceNum();
				return message;
            }
			else
			{
            	System.out.println("Sequence number out of order! Session may be compromised. Disconnecting");
				return new Envelope("DISCONNECT");
			}
        }
		return new Envelope("DISCONNECT");
	}

	public Envelope clientHmacVerify(Envelope message) throws Exception
	{
        byte[] hmac = (byte[])message.getObjContents().get(message.getObjContents().size()-1);
		message.removeObject(message.getObjContents().get(message.getObjContents().size()-1));
        if (!this.verifyHmac(hmac, this.getEnvelopeBytes(message)))
        {
            System.out.println("HMAC not verified! Session may be compromised. Exiting.");
            System.exit(0);
        }
		return message;
	}

	public Envelope serverHmacVerify(Envelope message) throws Exception
	{
        byte[] hmac = (byte[])message.getObjContents().get(message.getObjContents().size()-1);
		message.removeObject(message.getObjContents().get(message.getObjContents().size()-1));
        if (!this.verifyHmac(hmac, this.getEnvelopeBytes(message)))
        {
            System.out.println("HMAC not verified! Session may be compromised. Disconnecting.");
			return new Envelope("DISCONNECT");
        }
		return message;
	}

}
