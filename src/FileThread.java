/* File worker thread handles the business of uploading, downloading, and removing files for clients with valid tokens */

import java.io.*;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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
                        // Getting the current client's public key
                        Key clientPublicKey = (Key)message.getObjContents().get(1);
						session.setTargetKey(clientPublicKey);
					}
				}
				if (message.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_RSA))
                {
                    // Decrypt message with file server's private key
            		message = my_fs.serverRSAKeys.getDecryptedMessage(message);
                }
                else if (message.getMessage().equals("ENCRYPTEDENV"+EncryptionSuite.ENCRYPTION_AES))
                {
                    // Decrypt the message with the shared session AES key
                    message = session.getDecryptedMessage(message);

                    if (message.getObjContents().get(0) != null &&
                        message.getObjContents().get(message.getObjContents().size()-1) != null)
                    {
                        // Verify the HMAC the client sent is valid. If it's wrong, send a disconnect message
                        message = session.serverHmacVerify(message);
                        // Handle the client's sequence number. if it's wrong, send a disconnect message
                        message = session.serverSequenceNumberHandler(message);
                    }
                }

                System.out.println("Request received: " + message.getMessage());

                response = new Envelope("FAIL");

                if (!message.getMessage().equals("AUTHCHALLENGE") && !message.getMessage().equals("GPUBLICKEY"))
                    response.addObject(this.session.getSequenceNum());

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

                    // Generate an HMAC of our message for client to verify
                    response.addObject(this.session.generateHmac(response));

                    output.writeObject(session.getEncryptedMessage(response));
                }
                else if(message.getMessage().equals("AUTHCHALLENGE"))
                {
                    if(message.getObjContents().size() >= 3)
                    {
                        // Checking first param isn't null
                        if(message.getObjContents().get(0) != null &&
                            message.getObjContents().get(1) != null &&
                            message.getObjContents().get(2) != null)
                        {
		                    byte[] challenge = (byte[])message.getObjContents().get(0); // User's challenge R
                            Key hmacKey = (Key)message.getObjContents().get(1);
                            byte[] messageHmac = (byte[])message.removeObject(2);

                            // If we verify the message is from the person who sent it
                            if (this.session.getTargetKey().verifyHmac(messageHmac, this.session.getBytes(message)))
                            {
                                // Creating an E.S. for our Hmac key from the client
                                session.setHmacKey(hmacKey);
    		                    // Generating a new AES session key
    							session.setAESKey();
    							// Setting the nonce
    							session.setNonce(challenge);
    		                    // Constructing the envelope
    		                    response.setMessage("OK");

    		                    // Completing challenge
    		                    response.addObject(session.completeChallenge());
    		                    // Adding new AES session key
    		                    response.addObject(session.getAESKey().getEncryptionKey());
                            }

						}
					}

                    // Generate an HMAC of the auth response for the client to verify
                    response.addObject(session.generateHmac(response));

                    // Encrypting it all and sending it along
                    output.writeObject(session.getEncryptedMessageTargetKey(response));
                }
                else if(message.getMessage().equals("UPLOADF"))
                {
                    if(message.getObjContents().size() < 7)
                    {
                        response.setMessage("FAIL-BADCONTENTS");
                    }
                    else
                    {
                        if(message.getObjContents().get(0) == null)
                        {
                            response.setMessage("FAIL-BADPATH");
                        }
                        else if(message.getObjContents().get(1) == null)
                        {
                            response.setMessage("FAIL-BADGROUP");
                        }
                        else if(message.getObjContents().get(2) == null)
                        {
                            response.setMessage("FAIL-BADTOKEN");
                        }
                        else if(message.getObjContents().get(3) == null)
                        {
                            response.setMessage("FAIL-BADFILE");
                        }
                        else if(message.getObjContents().get(4) == null)
                        {
                            response.setMessage("FAIL-BADKEYVERSION");
                        }
                        else if(message.getObjContents().get(5) == null)
                        {
                            response.setMessage("FAIL-BADFILESIZE");
                        }
                        else if(message.getObjContents().get(6) == null)
                        {
                            response.setMessage("FAIL-BADHMAC");
                        }
                        else
                        {
                            String remotePath = (String) message.getObjContents().get(0);
                            String group = (String) message.getObjContents().get(1);
                            UserToken yourToken = (UserToken) message.getObjContents().get(2); //Extract token
                            byte[] fileBytes = (byte[])message.getObjContents().get(3);
                            int keyVersion = (int)message.getObjContents().get(4);
                            int actualFileSize = (int)message.getObjContents().get(5);
                            byte[] fileHmac = (byte[])message.getObjContents().get(6);

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

                                    FileServer.fileList.addFile(yourToken.getSubject(), group, remotePath, keyVersion, actualFileSize, fileHmac);

                                    response.setMessage("OK"); //Success
                                }
                            }
                        }
                    }

                    // Generate an HMAC of our message for server to verify
                    response.addObject(session.generateHmac(response));

                    output.writeObject(session.getEncryptedMessage(response));
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
                                File f = new File("shared_files/_" + remotePath.replace('/', '_'));
                                if(!f.exists())
                                {
                                    System.out.printf("Error file %s missing from disk\n", "_" + remotePath.replace('/', '_'));
                                    response.setMessage("ERROR_NOTONDISK");
                                }
                                else
                                {
                                    response.setMessage("FILE");

                                    Path path = Paths.get("shared_files/_" + remotePath.replace('/', '_'));
                                    byte[] fileBytes = Files.readAllBytes(path);

                                    System.out.println("encrypted file length: " + fileBytes.length);
                                    response.addObject(fileBytes);
                                    response.addObject(sf.getGroup());
                                    response.addObject(sf.getEncryptionVersion());
                                    response.addObject(sf.getFileSize());
                                    response.addObject(sf.getFileHmac());
                                }
                            }
                            catch(Exception e1)
                            {
                                System.err.println("Error: " + message.getMessage());
                                e1.printStackTrace(System.err);
                            }
                        }
                    }

                    // Generate an HMAC of our message for server to verify
                    response.addObject(session.generateHmac(response));

                    output.writeObject(session.getEncryptedMessage(response));
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

                    // Generate an HMAC of our message for server to verify
                    response.addObject(session.generateHmac(response));

                    output.writeObject(session.getEncryptedMessage(response));
                }
                else if (message.getMessage().equals("DISCONNECT"))
                {
                    socket.close();
                    proceed = false;
                }
                else if (message.getMessage().equals("FAIL"))
                {

                    // Generate an HMAC of the auth response for the client to verify
                    response.addObject(session.generateHmac(response));

                    output.writeObject(session.getEncryptedMessage(response));
                }
                else
                {
                    response.setMessage("FAIL"); //Server does not understand client request

                    // Generate an HMAC of the auth response for the client to verify
                    response.addObject(session.generateHmac(response));

                    output.writeObject(session.getEncryptedMessage(response));
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
