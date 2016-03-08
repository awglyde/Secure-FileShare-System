/* File worker thread handles the business of uploading, downloading, and removing files for clients with valid tokens */

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.security.Key;

public class FileThread extends Thread
{
    private final Socket socket;
    private FileServer my_fs;

    public FileThread(Socket _socket, FileServer _fs)
    {
        socket = _socket;
        my_fs = _fs;
    }

    public void run()
    {
        boolean proceed = true;
        try
        {
            System.out.println("*** New connection from " + this.socket.getInetAddress() + ":" + this.socket.getPort() + "***");
            final ObjectInputStream input = new ObjectInputStream(this.socket.getInputStream());
            final ObjectOutputStream output = new ObjectOutputStream(this.socket.getOutputStream());
            Envelope response;

            do
            {

                // flushes / resets the output stream
                output.flush();
                output.reset();

                Envelope e = (Envelope) input.readObject();
                EncryptionSuite sessionKey = null;
				if (e.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_RSA))
                {
                    // Decrypt message with file server's private key
            		e = my_fs.serverRSAKeys.getDecryptedMessage(e);
                }
                else if (e.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_AES))
                {
                    // Decrypt message with shared AES key
                    // TODO: MAKE A LIST OF SHARED AES KEYS MAPPED TO.. WHAT? USERNAME?
                    Integer clientPubHash = (Integer)e.getObjContents().get(1);
                    sessionKey = my_fs.getSessionES(clientPubHash);
            		e = my_fs.getSessionES(clientPubHash).getDecryptedMessage(e);
                }

                response = new Envelope("FAIL");

                // Handler to list files that this user is allowed to see
                if(e.getMessage().equals("LFILES"))
                {
                    if( e.getObjContents().size() >= 1 && e.getObjContents().get(0) != null)
                    {
                        UserToken yourToken = (UserToken) e.getObjContents().get(0); //Extract the token
						// Make sure your token isn't expired and validate signed hash with group server public key
						if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
						{
							System.out.println("Successfully verified token!");
	                        response = new Envelope("OK"); //Success
	                        response.addObject(FileServer.fileList.getUserFiles(yourToken)); // append the users files
						}
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(e.getMessage().equals("GPUBLICKEY"))
				{
                    if(e.getObjContents().size() >= 1)
                    {
                        if(e.getObjContents().get(0) != null)
                        {
							// Map the client's hash of their key to their key, so we know who we're talking to in the future
		                    my_fs.mapClientCodeToPublicKey((Integer)e.getObjContents().get(0).hashCode(), (Key)e.getObjContents().get(0));
		                    response = new Envelope("OK");

							// Add the server's public key to the envelope and send it back.
		                    response.addObject(my_fs.getPublicKey());
						}
					}

                    output.writeObject(response);
				}
                else if(e.getMessage().equals("AUTHCHALLENGE"))
                {
					EncryptionSuite clientKeys = null;
                    if(e.getObjContents().size() >= 2)
                    {
                        if(e.getObjContents().get(0) != null && e.getObjContents().get(1) != null)
                        {
		                    byte[] challenge = (byte[])e.getObjContents().get(0); // User's challenge R
		                    Integer clientPubHash = (Integer)e.getObjContents().get(1); // Hash of users pub key

		                    // Retrieving the client's public key from our hashmap
		                    Key clientPubKey = my_fs.getClientPublicKey(clientPubHash);
                            my_fs.removePublicKeyMapping(clientPubHash);

		                    sessionKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);

                            // Adding session key to our mapping (Allows multiple users)
                            my_fs.mapSessionES(clientPubKey.hashCode(), sessionKey);
		                    clientKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, clientPubKey, null);

		                    response = new Envelope("OK");
		                    // Adding completed challenge
		                    response.addObject(sessionKey.hashBytes(challenge));
		                    // Adding new AES session key
		                    response.addObject(sessionKey.getEncryptionKey());
						}
					}

                    // Encrypting it all and sending it along
                    output.writeObject(clientKeys.getEncryptedMessage(response));
                }
                else if(e.getMessage().equals("UPLOADF"))
                {
                    if(e.getObjContents().size() < 3)
                    {
                        response = new Envelope("FAIL-BADCONTENTS");
                    }
                    else
                    {
                        if(e.getObjContents().get(0) == null)
                        {
                            response = new Envelope("FAIL-BADPATH");
                        }
                        if(e.getObjContents().get(1) == null)
                        {
                            response = new Envelope("FAIL-BADGROUP");
                        }
                        if(e.getObjContents().get(2) == null)
                        {
                            response = new Envelope("FAIL-BADTOKEN");
                        }
                        else
                        {
                            String remotePath = (String) e.getObjContents().get(0);
                            String group = (String) e.getObjContents().get(1);
                            UserToken yourToken = (UserToken) e.getObjContents().get(2); //Extract token

                            // Verify token signature and make sure it isn't expired
                            response = new Envelope("FAIL-BADTOKEN");
        					if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
        					{
                                if(FileServer.fileList.checkFile(remotePath))
                                {
                                    System.out.printf("Error: file already exists at %s\n", remotePath);
                                    response = new Envelope("FAIL-FILEEXISTS"); //Success
                                }
                                else if(!yourToken.getGroups().contains(group))
                                {
                                    System.out.printf("Error: user missing valid token for group %s\n", group);
                                    response = new Envelope("FAIL-UNAUTHORIZED"); //Success
                                }
                                else
                                {
                                    File file = new File("shared_files/" + remotePath.replace('/', '_'));
                                    file.createNewFile();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    System.out.printf("Successfully created file %s\n", remotePath.replace('/', '_'));

                                    response = new Envelope("READY"); //Success
                                    output.writeObject(sessionKey.getEncryptedMessage(response));

                                    e = (Envelope) input.readObject();
                                    e = sessionKey.getDecryptedMessage(e);
                                    while(e.getMessage().compareTo("CHUNK") == 0)
                                    {
                                        fos.write((byte[]) e.getObjContents().get(0), 0, (Integer) e.getObjContents().get(1));
                                        response = new Envelope("READY"); //Success
                                        output.writeObject(sessionKey.getEncryptedMessage(response));
                                        e = (Envelope) input.readObject();
                                        e = sessionKey.getDecryptedMessage(e);
                                    }

                                    if(e.getMessage().compareTo("EOF") == 0)
                                    {
                                        System.out.printf("Transfer successful file %s\n", remotePath);
                                        FileServer.fileList.addFile(yourToken.getSubject(), group, remotePath);
                                        response = new Envelope("OK"); //Success
                                    }
                                    else
                                    {
                                        System.out.printf("Error reading file %s from client\n", remotePath);
                                        response = new Envelope("ERROR-TRANSFER"); //Success
                                    }
                                    fos.close();
                                }
                            }
                        }
                    }

                    output.writeObject(sessionKey.getEncryptedMessage(response));
                }
                else if(e.getMessage().compareTo("DOWNLOADF") == 0)
                {

                    String remotePath = (String) e.getObjContents().get(0);
                    UserToken yourToken = (UserToken) e.getObjContents().get(1); //Extract token

                    ShareFile sf = FileServer.fileList.getFile("/" + remotePath);

                    // Verify token signature and make sure it isn't expired
        			if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
        			{
                        if(sf == null)
                        {
                            System.out.printf("Error: File %s doesn't exist\n", remotePath);
                            e = new Envelope("ERROR_FILEMISSING");
                            output.writeObject(sessionKey.getEncryptedMessage(e));

                        }
                        else if(!yourToken.getGroups().contains(sf.getGroup()))
                        {
                            System.out.printf("Error user %s doesn't have permission\n", yourToken.getSubject());
                            e = new Envelope("ERROR_PERMISSION");
                            output.writeObject(sessionKey.getEncryptedMessage(e));
                        }
                        else
                        {

                            try
                            {
                                File f = new File("shared_files/_" + remotePath.replace('/', '_'));
                                if(!f.exists())
                                {
                                    System.out.printf("Error file %s missing from disk\n", "_" + remotePath.replace('/', '_'));
                                    e = new Envelope("ERROR_NOTONDISK");
                                    output.writeObject(sessionKey.getEncryptedMessage(e));
                                }
                                else
                                {
                                    FileInputStream fis = new FileInputStream(f);

                                    do
                                    {
                                        byte[] buf = new byte[4096];
                                        if(e.getMessage().compareTo("DOWNLOADF") != 0)
                                        {
                                            System.out.printf("Server error: %s\n", e.getMessage());
                                            break;
                                        }
                                        e = new Envelope("CHUNK");
                                        int n = fis.read(buf); //can throw an IOException
                                        if(n > 0)
                                        {
                                            System.out.printf(".");
                                        }
                                        else if(n < 0)
                                        {
                                            System.out.println("Read error");
                                        }

                                        e.addObject(buf);
                                        e.addObject(new Integer(n));

                                        output.writeObject(sessionKey.getEncryptedMessage(e));

                                        e = (Envelope) input.readObject();
                                        e = sessionKey.getDecryptedMessage(e);
                                    }
                                    while(fis.available() > 0);

                                    //If server indicates success, return the member list
                                    if(e.getMessage().compareTo("DOWNLOADF") == 0)
                                    {
                                        e = new Envelope("EOF");
                                        output.writeObject(sessionKey.getEncryptedMessage(e));

                                        e = (Envelope) input.readObject();
                                        e = sessionKey.getDecryptedMessage(e);
                                        if(e.getMessage().compareTo("OK") == 0)
                                        {
                                            System.out.printf("File data upload successful\n");
                                        }
                                        else
                                        {
                                            System.out.printf("Upload failed: %s\n", e.getMessage());
                                        }
                                    }
                                    else
                                    {
                                        System.out.printf("Upload failed: %s\n", e.getMessage());
                                    }
                                }
                            }
                            catch(Exception e1)
                            {
                                System.err.println("Error: " + e.getMessage());
                                e1.printStackTrace(System.err);
                            }
                        }
                    }
                }
                else if(e.getMessage().compareTo("DELETEF") == 0)
                {
                    String remotePath = (String) e.getObjContents().get(0);
                    UserToken yourToken = (UserToken) e.getObjContents().get(1);

                    // Verify token signature and make sure it isn't expired
					if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
					{
                        ShareFile sf = FileServer.fileList.getFile("/" + remotePath);
                        if(sf == null)
                        {
                            System.out.printf("Error: File %s doesn't exist\n", remotePath);
                            e = new Envelope("ERROR_DOESNTEXIST");
                        }
                        else if(!yourToken.getGroups().contains(sf.getGroup()))
                        {
                            System.out.printf("Error user %s doesn't have permission\n", yourToken.getSubject());
                            e = new Envelope("ERROR_PERMISSION");
                        }
                        else
                        {

                            try
                            {
                                File f = new File("shared_files/" + "_" + remotePath.replace('/', '_'));
                                if(!f.exists())
                                {
                                    System.out.printf("Error file %s missing from disk\n", "_" + remotePath.replace('/', '_'));
                                    e = new Envelope("ERROR_FILEMISSING");
                                }
                                else if(f.delete())
                                {
                                    System.out.printf("File %s deleted from disk\n", "_" + remotePath.replace('/', '_'));
                                    FileServer.fileList.removeFile("/" + remotePath);
                                    e = new Envelope("OK");
                                }
                                else
                                {
                                    System.out.printf("Error deleting file %s from disk\n", "_" + remotePath.replace('/', '_'));
                                    e = new Envelope("ERROR_DELETE");
                                }
                            }
                            catch(Exception e1)
                            {
                                System.err.println("Error: " + e1.getMessage());
                                e1.printStackTrace(System.err);
                                e = new Envelope(e1.getMessage());
                            }
                        }
                    }
                    output.writeObject(sessionKey.getEncryptedMessage(e));

                }
                else if(e.getMessage().equals("DISCONNECT"))
                {
                    socket.close();
                    proceed = false;
                }
            } while(proceed);
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

}
