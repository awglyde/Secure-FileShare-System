import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.security.Key;
import javax.xml.bind.DatatypeConverter;

public class Token implements UserToken, java.io.Serializable
{
    private static final long serialVersionUID = -7726335089121458703L;
    private String issuer;
    private String subject;
    private ArrayList<String> groups;
    private Key fileServerPublicKey;
    private Date expirationDate;
	private byte[] signedHash;

    public Token(String issuer, String subject, ArrayList<String> groups, Key fileServerPublicKey)
    {
        this.issuer = issuer;
        this.subject = subject;
        this.groups = groups;
        this.fileServerPublicKey = fileServerPublicKey;
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        cal.add(Calendar.HOUR, 6);
        this.expirationDate = cal.getTime();

		this.signedHash = null;
    }

    /**
     * This method should return a string describing the issuer of
     * this token.  This string identifies the group server that
     * created this token.  For instance, if "Alice" requests a token
     * from the group server "Server1", this method will return the
     * string "Server1".
     *
     * @return The issuer of this token
     */
    public String getIssuer()
    {
        return this.issuer;
    }


    /**
     * This method should return a string indicating the name of the
     * subject of the token.  For instance, if "Alice" requests a
     * token from the group server "Server1", this method will return
     * the string "Alice".
     *
     * @return The subject of this token
     */
    public String getSubject()
    {
        return this.subject;
    }


    /**
     * This method extracts the list of groups that the owner of this
     * token has access to.  If "Alice" is a member of the groups "G1"
     * and "G2" defined at the group server "Server1", this method
     * will return ["G1", "G2"].
     *
     * @return The list of group memberships encoded in this token
     */
    public List<String> getGroups()
    {
        return this.groups;
    }

    public Key getFileServerPublicKey()
    {
        return this.fileServerPublicKey;
    }

    public boolean isAdmin()
    {
        if (this.getGroups().contains("ADMIN"))
            return true;
        else
            return false;
    }

    public boolean isExpired()
    {
        if ( ( this.expirationDate.getTime() - new Date().getTime() ) <= 0 )
            return true;
        else
            return false;
    }

	// Sets signed hash. The group server will call this method to sign the hash
	public void setSignedHash(byte[] signedHash)
	{
		this.signedHash = signedHash;
	}
	// Returns a signed hash of the token created using the toString method
	// when the token was issued
	public byte[] getSignedHash()
	{
		return signedHash;
	}


    public String toString()
    {

		String groupList = null;
		for (String group : this.groups)
		{
			if (groupList != null)
				groupList = groupList+","+group;
			else
				groupList = group;
		}
        return this.issuer+System.lineSeparator()+
                this.subject+System.lineSeparator()+
                groups+System.lineSeparator()+
                DatatypeConverter.printHexBinary(this.fileServerPublicKey.getEncoded())+System.lineSeparator()+
                this.expirationDate.toString()+System.lineSeparator();

    }

}
