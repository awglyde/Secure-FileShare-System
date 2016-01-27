public abstract class Server
{

    public String name;
    protected int port;

    public Server(int _SERVER_PORT, String _serverName)
    {
        port = _SERVER_PORT;
        name = _serverName;
    }

    abstract void start();

    public int getPort()
    {
        return port;
    }

    public String getName()
    {
        return name;
    }

}
