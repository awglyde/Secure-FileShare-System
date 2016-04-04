public class ShareFile implements java.io.Serializable, Comparable<ShareFile>
{

    /**
     *
     */
    private static final long serialVersionUID = -6699986336399821598L;
    private int encryptionVersion;
    private int fileSize;
    private String group;
    private String path;
    private String owner;

    public ShareFile(String owner, String group, String path, int encryptionVersion, int fileSize)
    {
        this.group = group;
        this.owner = owner;
        this.path = path;
        this.encryptionVersion = encryptionVersion;
        this.fileSize = fileSize;
    }

    public int getFileSize()
    {
        return this.fileSize;
    }

    public int getEncryptionVersion()
    {
        return this.encryptionVersion;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getOwner()
    {
        return this.owner;
    }

    public String getGroup()
    {
        return this.group;
    }

    public int compareTo(ShareFile rhs)
    {
        if(this.path.compareTo(rhs.getPath()) == 0) return 0;
        else if(this.path.compareTo(rhs.getPath()) < 0) return -1;
        else return 1;
    }


}
