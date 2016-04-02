import java.security.Key;
public class Session
{
	EncryptionSuite hmacKey;
	EncryptionSuite aesSessionKey;
	EncryptionSuite clientPubKey;
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

	public void setClientPublicKey(Key clientPubKey) throws Exception
	{
		this.clientPubKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, clientPubKey);
	}

	public EncryptionSuite getClientPublicKey()
	{
		return this.clientPubKey;
	}

	public EncryptionSuite getHmacKey()
	{
		return this.hmacKey;
	}

	public EncryptionSuite getAESKey()
	{
		return this.aesSessionKey;
	}

	public Envelope getEncryptedMessageClientKey(Envelope message) throws Exception
	{
		return this.clientPubKey.getEncryptedMessage(message);
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
