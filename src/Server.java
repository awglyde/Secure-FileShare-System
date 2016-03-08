import java.util.HashMap;
import java.security.Key;

public abstract class Server
{

    public String name;
    protected int port;
    protected HashMap<Integer, Key> clientCodeToPublicKey;
    protected HashMap<Integer, EncryptionSuite> clientCodeToSessionES;
    protected EncryptionSuite serverRSAKeys;

    public Server(int _SERVER_PORT, String _serverName)
    {
        this.port = _SERVER_PORT;
        this.name = _serverName;
        clientCodeToSessionES = new HashMap<Integer, EncryptionSuite>();
        clientCodeToPublicKey = new HashMap<Integer, Key>();
    }

    abstract void start() throws Exception;

    public Key getPublicKey()
    {
        return serverRSAKeys.getEncryptionKey();
    }


    public EncryptionSuite getSessionES(Integer pubKeyHash)
    {
        return this.clientCodeToSessionES.get(pubKeyHash);
    }

    public void mapClientCodeToPublicKey(Integer pubKeyHash, Key publicKey)
    {
        this.clientCodeToPublicKey.put(pubKeyHash, publicKey);
    }

    public Key getClientPublicKey(Integer pubKeyHash)
    {
        return this.clientCodeToPublicKey.get(pubKeyHash);
    }

    public void removePublicKeyMapping(Integer pubKeyHash)
    {
        this.clientCodeToPublicKey.remove(pubKeyHash);
    }

    public void mapSessionES(Integer clientPubHashCode, EncryptionSuite sessionKey)
    {
        this.clientCodeToSessionES.put(clientPubHashCode, sessionKey);
    }

    public int getPort()
    {
        return this.port;
    }

    public String getName()
    {
        return this.name;
    }

}
