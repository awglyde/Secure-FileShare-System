import java.util.HashMap;
import java.security.Key;

public abstract class Server
{

    public String name;
    protected int port;
    protected EncryptionSuite serverRSAKeys;

    public Server(int _SERVER_PORT, String _serverName)
    {
        this.port = _SERVER_PORT;
        this.name = _serverName;
    }

    abstract void start() throws Exception;

    public Key getPublicKey()
    {
        return this.serverRSAKeys.getEncryptionKey();
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
