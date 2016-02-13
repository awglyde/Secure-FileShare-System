import java.util.ArrayList;
import java.util.List;

public class Token implements UserToken, java.io.Serializable
{
    private static final long serialVersionUID = -7726335089121458703L;
    private String issuer;
    private String subject;
    private ArrayList<String> groups;

    public Token(String issuer, String subject, ArrayList<String> groups)
    {
        this.issuer = issuer;
        this.subject = subject;
        this.groups = groups;
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


    public boolean isAdmin()
    {
        if (this.getGroups().contains("ADMIN"))
            return true;
        else
            return false;
    }


}
