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
                System.out.println("Request received: " + e.getMessage());

                // Handler to list files that this user is allowed to see
                if(e.getMessage().equals("LFILES"))
                {
                    response = new Envelope("FAIL");
                    // Ensure envelope size is 1 (contains token) and the token is not null
                    if( e.getObjContents().size() == 1 && e.getObjContents().get(0) != null)
                    {
                        UserToken yourToken = (UserToken) e.getObjContents().get(0); //Extract the token
                        response = new Envelope("OK"); //Success
                        response.addObject(FileServer.fileList.getUserFiles(yourToken)); // append the users files
                    }

                    output.writeObject(response);
                }
                if(e.getMessage().equals("GPUBLICKEY"))
				{
                    if(e.getObjContents().size() < 2) // Make sure e size >= 2
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking first param isn't null
                        if(e.getObjContents().get(0) != null)
                        {
                            if(e.getObjContents().get(1) != null)
                            {
							// Map the client's key to the hash of their key, so we know who we're talking to in the future
		                    my_fs.clientCodeToKey.put((Integer)e.getObjContents().get(0).hashCode(),
		                                                (Key)e.getObjContents().get(0));

                            // Store the group server's public key (To verify client's token)
                            my_fs.groupServerPubKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, (Key)e.getObjContents().get(1), null);
		                    response = new Envelope("OK");
							// Add the server's public key to the envelope and send it back.
							// TODO: Ask the client if this is the public key they were expecting
                            }
						}
					}

                    output.writeObject(response);
				}
                else if(e.getMessage().equals("AUTHCHALLENGE"))
                {
					EncryptionSuite clientKeys = null;
                    // If we don't get a challenge, public key hash, and signed token hash fail
                    if(e.getObjContents().size() < 2)
                    {
                        response = new Envelope("FAIL");
                    }
                    else
                    {
                        response = new Envelope("FAIL");

                        // Checking first param isn't null
                        if(e.getObjContents().get(0) != null)
                        {
                            // Checking second param isn't null
                            if(e.getObjContents().get(1) != null)
                            {
			                    byte[] challenge = (byte[])e.getObjContents().get(0); // User's challenge R
			                    Integer clientPubHash = (Integer)e.getObjContents().get(1); // Hash of users pub key

			                    // Retrieving the client's public key from our hashmap
			                    Key clientPubKey = my_fs.clientCodeToKey.get(clientPubHash);
			                    System.out.println("User's challenge R: "+ new String(challenge, "UTF-8"));
			                    // Generating a new AES session key
			                    my_fs.sessionKey = new EncryptionSuite(EncryptionSuite.ENCRYPTION_AES);

			                    System.out.println("\n\nNew Shared Key: \n\n"+my_fs.sessionKey.encryptionKeyToString());
			                    // Making a temporary client key ES object to encrypt the session key with
			                    clientKeys = new EncryptionSuite(EncryptionSuite.ENCRYPTION_RSA, clientPubKey, null);

			                    // Constructing the envelope
			                    response = new Envelope("OK");
			                    // Adding completed challenge
			                    response.addObject(my_fs.sessionKey.hashBytes(challenge));
			                    // Adding new AES session key
			                    response.addObject(my_fs.sessionKey.getEncryptionKey());

							}
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
                                output.writeObject(response);

                                e = (Envelope) input.readObject();
                                while(e.getMessage().compareTo("CHUNK") == 0)
                                {
                                    fos.write((byte[]) e.getObjContents().get(0), 0, (Integer) e.getObjContents().get(1));
                                    response = new Envelope("READY"); //Success
                                    output.writeObject(response);
                                    e = (Envelope) input.readObject();
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

                    output.writeObject(response);
                }
                else if(e.getMessage().compareTo("DOWNLOADF") == 0)
                {

                    String remotePath = (String) e.getObjContents().get(0);
                    Token t = (Token) e.getObjContents().get(1);
                    ShareFile sf = FileServer.fileList.getFile("/" + remotePath);
                    if(sf == null)
                    {
                        System.out.printf("Error: File %s doesn't exist\n", remotePath);
                        e = new Envelope("ERROR_FILEMISSING");
                        output.writeObject(e);

                    }
                    else if(!t.getGroups().contains(sf.getGroup()))
                    {
                        System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
                        e = new Envelope("ERROR_PERMISSION");
                        output.writeObject(e);
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
                                output.writeObject(e);

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

                                    output.writeObject(e);

                                    e = (Envelope) input.readObject();


                                }
                                while(fis.available() > 0);

                                //If server indicates success, return the member list
                                if(e.getMessage().compareTo("DOWNLOADF") == 0)
                                {

                                    e = new Envelope("EOF");
                                    output.writeObject(e);

                                    e = (Envelope) input.readObject();
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
                else if(e.getMessage().compareTo("DELETEF") == 0)
                {

                    String remotePath = (String) e.getObjContents().get(0);
                    Token t = (Token) e.getObjContents().get(1);
                    ShareFile sf = FileServer.fileList.getFile("/" + remotePath);
                    if(sf == null)
                    {
                        System.out.printf("Error: File %s doesn't exist\n", remotePath);
                        e = new Envelope("ERROR_DOESNTEXIST");
                    }
                    else if(!t.getGroups().contains(sf.getGroup()))
                    {
                        System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
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
                    output.writeObject(e);

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
