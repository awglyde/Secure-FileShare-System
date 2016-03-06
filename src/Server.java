public abstract class Server
{

    public String name;
    protected int port;

    public Server(int _SERVER_PORT, String _serverName)
    {
        this.port = _SERVER_PORT;
        this.name = _serverName;
    }

    abstract void start() throws Exception;

    public int getPort()
    {
        return this.port;
    }

    public String getName()
    {
        return this.name;
    }

}
