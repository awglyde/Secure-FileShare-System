import java.security.Key;
public class Session
{
	EncryptionSuite hmacKey;
	EncryptionSuite aesSessionKey;
	EncryptionSuite targetPubKey;
    byte[] nonce;

	public Session()
	{

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

	public boolean verifyHmac(byte[] hmacMesssage, byte[] messageBytes) throws Exception
	{
		return this.hmacKey.verifyHmac(hmacMesssage, messageBytes);
	}

	public byte[] completeChallenge() throws Exception
	{
		return this.aesSessionKey.hashBytes(this.nonce);
	}
}
