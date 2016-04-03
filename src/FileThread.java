/* File worker thread handles the business of uploading, downloading, and removing files for clients with valid tokens */

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.security.Key;

public class FileThread extends Thread
{
    private final Socket socket;
    private FileServer my_fs;
	private Session session;

    public FileThread(Socket _socket, FileServer _fs)
    {
        socket = _socket;
        my_fs = _fs;
		session = new Session();
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

                Envelope message = (Envelope) input.readObject();
                if (message.getObjContents().size() > 1) // if envelope contains a public key as well
				{
                    // Checking first param isn't null
                    if(message.getObjContents().get(1) != null)
                    {
						// Map the client's hash of their key to their key, so we know who we're talking to in the future

                        Key clientPublicKey = (Key)message.getObjContents().get(1);
						session.setTargetKey(clientPublicKey);
						// TODO: Remove this property. Deprecated with new protocol
	                    // my_gs.mapClientCodeToPublicKey((Integer)key.hashCode(), key);
						// Add the server's public key to the envelope and send it back.
					}

				}
				if (message.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_RSA))
                {
                    // Decrypt message with file server's private key
            		message = my_fs.serverRSAKeys.getDecryptedMessage(message);
                }
                else if (message.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_AES))
                {
                    // gets shared AES for the correct client
					message = session.getDecryptedMessage(message);
                    // Handle the client's sequence number. if it's wrong, send a disconnect message
                    message = session.serverSequenceNumberHandler(message);
                }

                System.out.println("Request received: " + message.getMessage());

                response = new Envelope("FAIL");

                if (!message.getMessage().equals("AUTHCHALLENGE") && !message.getMessage().equals("GPUBLICKEY"))
                    response.addObject(this.session.getSequenceNum());

                System.out.println("Sequence Number: " + session.getSequenceNum());

                // Handler to list files that this user is allowed to see
                if(message.getMessage().equals("GPUBLICKEY"))
                {
                    response.setMessage("OK"); //Success
                    response.addObject(my_fs.serverRSAKeys.getEncryptionKey());
                    output.writeObject(response);
                }
                else if(message.getMessage().equals("LFILES"))
                {
                    if(message.getObjContents().size() >= 1 && message.getObjContents().get(0) != null)
                    {
                        UserToken yourToken = (UserToken) message.getObjContents().get(0); //Extract the token
						// Make sure your token isn't expired and validate signed hash with group server public key
						if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
						{
							System.out.println("Successfully verified token!");
	                        response.setMessage("OK"); //Success
	                        response.addObject(FileServer.fileList.getUserFiles(yourToken)); // append the users files
						}
                    }

                    output.writeObject(session.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("AUTHCHALLENGE"))
                {
					EncryptionSuite clientKeys = null;
                    if(message.getObjContents().size() >= 1)
                    {
                        if(message.getObjContents().get(0) != null)
                        {
		                    byte[] challenge = (byte[])message.getObjContents().get(0); // User's challenge R

		                    // Generating a new AES session key
							session.setAESKey();

							// Setting the nonce
							session.setNonce(challenge);

		                    response.setMessage("OK");
		                    // Adding completed challenge
		                    response.addObject(session.completeChallenge());
		                    // Adding new AES session key
		                    response.addObject(session.getAESKey().getEncryptionKey());
						}
					}

                    // Encrypting it all and sending it along
                    output.writeObject(session.getEncryptedMessageTargetKey(response));
                }
                else if(message.getMessage().equals("UPLOADF"))
                {
                    if(message.getObjContents().size() < 3)
                    {
                        response.setMessage("FAIL-BADCONTENTS");
                    }
                    else
                    {
                        if(message.getObjContents().get(0) == null)
                        {
                            response.setMessage("FAIL-BADPATH");
                        }
                        if(message.getObjContents().get(1) == null)
                        {
                            response.setMessage("FAIL-BADGROUP");
                        }
                        if(message.getObjContents().get(2) == null)
                        {
                            response.setMessage("FAIL-BADTOKEN");
                        }
                        else
                        {
                            String remotePath = (String) message.getObjContents().get(0);
                            String group = (String) message.getObjContents().get(1);
                            UserToken yourToken = (UserToken) message.getObjContents().get(2); //Extract token
                            byte[] fileBytes = (byte[])message.getObjContents().get(3);

                            // Verify token signature and make sure it isn't expired
                            response.setMessage("FAIL-BADTOKEN");
        					if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
        					{
                                if(FileServer.fileList.checkFile(remotePath))
                                {
                                    System.out.printf("Error: file already exists at %s\n", remotePath);
                                    response.setMessage("FAIL-FILEEXISTS"); //Success
                                }
                                else if(!yourToken.getGroups().contains(group))
                                {
                                    System.out.printf("Error: user missing valid token for group %s\n", group);
                                    response.setMessage("FAIL-UNAUTHORIZED"); //Success
                                }
                                else
                                {
                                    File file = new File("shared_files/" + remotePath.replace('/', '_'));
                                    file.createNewFile();

                                    FileOutputStream fos = new FileOutputStream(file);
                                    System.out.printf("Successfully created file %s\n", remotePath.replace('/', '_'));

                                    fos.write(fileBytes);
                                    fos.close();
                                    System.out.println("Successfully received the file from the client.");

                                    FileServer.fileList.addFile(yourToken.getSubject(), group, remotePath);

                                    response = new Envelope("OK"); //Success
                                    response.addObject(this.session.getSequenceNum());
                                    output.writeObject(session.getEncryptedMessage(response));
                                }
                            }
                            else
                            {
                                output.writeObject(session.getEncryptedMessage(response));
                            }
                        }
                    }
                }
                else if(message.getMessage().compareTo("DOWNLOADF") == 0)
                {

                    String remotePath = (String) message.getObjContents().get(0);
                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract token
                    ShareFile sf = FileServer.fileList.getFile("/" + remotePath);

                    // Verify token signature and make sure it isn't expired
        			if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
        			{
                        if(sf == null)
                        {
                            System.out.printf("Error: File %s doesn't exist\n", remotePath);
                            response.setMessage("ERROR_FILEMISSING");
                            output.writeObject(session.getEncryptedMessage(response));

                        }
                        else if(!yourToken.getGroups().contains(sf.getGroup()))
                        {
                            System.out.printf("Error user %s doesn't have permission\n", yourToken.getSubject());
                            response.setMessage("ERROR_PERMISSION");
                            output.writeObject(session.getEncryptedMessage(response));
                        }
                        else
                        {

                            try
                            {
                                File f = new File("shared_files/_" + remotePath.replace('/', '_'));
                                if(!f.exists())
                                {
                                    System.out.printf("Error file %s missing from disk\n", "_" + remotePath.replace('/', '_'));
                                    response.setMessage("ERROR_NOTONDISK");
                                    output.writeObject(session.getEncryptedMessage(response));
                                }
                                else
                                {
                                    response.setMessage("FILE");
                                    byte[] fileBytes = new byte[(int)f.length()];
                                    FileInputStream fis = new FileInputStream(f);
                                    fis.read(fileBytes);
                                    fis.close();

                                    response.addObject(fileBytes);

                                    output.writeObject(session.getEncryptedMessage(response));
                                }
                            }
                            catch(Exception e1)
                            {
                                System.err.println("Error: " + message.getMessage());
                                e1.printStackTrace(System.err);
                            }
                        }
                    }
                }
                else if(message.getMessage().compareTo("DELETEF") == 0)
                {
                    String remotePath = (String) message.getObjContents().get(0);
                    UserToken yourToken = (UserToken) message.getObjContents().get(1);

                    // Verify token signature and make sure it isn't expired
					if (!yourToken.isExpired() && my_fs.verifyToken(yourToken))
					{
                        ShareFile sf = FileServer.fileList.getFile("/" + remotePath);
                        if(sf == null)
                        {
                            System.out.printf("Error: File %s doesn't exist\n", remotePath);
                            response.setMessage("ERROR_DOESNTEXIST");
                        }
                        else if(!yourToken.getGroups().contains(sf.getGroup()))
                        {
                            System.out.printf("Error user %s doesn't have permission\n", yourToken.getSubject());
                            response.setMessage("ERROR_PERMISSION");
                        }
                        else
                        {

                            try
                            {
                                File f = new File("shared_files/" + "_" + remotePath.replace('/', '_'));
                                if(!f.exists())
                                {
                                    System.out.printf("Error file %s missing from disk\n", "_" + remotePath.replace('/', '_'));
                                    response.setMessage("ERROR_FILEMISSING");
                                }
                                else if(f.delete())
                                {
                                    System.out.printf("File %s deleted from disk\n", "_" + remotePath.replace('/', '_'));
                                    FileServer.fileList.removeFile("/" + remotePath);
                                    response.setMessage("OK");
                                }
                                else
                                {
                                    System.out.printf("Error deleting file %s from disk\n", "_" + remotePath.replace('/', '_'));
                                    response.setMessage("ERROR_DELETE");
                                }
                            }
                            catch(Exception e1)
                            {
                                System.err.println("Error: " + e1.getMessage());
                                e1.printStackTrace(System.err);
                                response.setMessage(e1.getMessage());
                            }
                        }
                    }
                    output.writeObject(session.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("DISCONNECT"))
                {
                    socket.close();
                    proceed = false;
                }
            } while(proceed);
        }
        catch(Exception message)
        {
            System.err.println("Error: " + message.getMessage());
            message.printStackTrace(System.err);
        }
    }

}
