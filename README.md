# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

CS1653 - Applied Cryptography

### How do I get set up? ###

1. Download the source code.
2. Verify you have Java version 1.8 or above.
3. Run the following command from the src folder to compile.

    On the lab machines in 6110 from the directory cs1653-project-awg13-ejw45-gah33/src/

	Compile with
	``` javac -cp ../resources/bcprov-jdk15on-154.jar *.java ```

4. To run the code you must run the Group Server and the File Server. To do that use the following commands:

	``` java -cp ../resources/bcprov-jdk15on-154.jar:. RunGroupServer [(optional) Port Number] ```

	``` java -cp ../resources/bcprov-jdk15on-154.jar:. RunFileServer [(optional) Port Number] ```

5. Then run the User Client to utilize the Group Server and File Server. Follow the prompt:

	``` java -cp ../resources/bcprov-jdk15on-154.jar:. UserClient [groupServerIP] [groupServerPort] [fileServerIP] [fileServerPort] ```

    If no arguments are passed in, default values will be taken.

### Contribution guidelines ###

YOLO

### Who do I talk to? ###

George Hoesch, Alex Glyde
gah33, awg13

# TODO
    * Validating input - Entering a userName when looking for a group gives an exception
    * Cleaning up error messages. I.e., returning one error message when a requested action fails
    * UserClient freezes if file exists on file server side... waiting for response
    * close thread not entire file server / group server when HMAC or sequence number is not correct
    * test removing a group member for key ring case, should add a key but still be able to decrypt old files
    * make sure to clear bin files before testing new functionality with key ring
    * test use cases on 6110

## Tests

### Create User
- Do not allow creation of duplicate user.
- Requester must be admin

### Add User to Group
- User must exist.
- Group must exist.
- User must not already be in group.
- Requester must be owner of group.
- Owner cannot add themselves to the group as a member.

### Create Group
- Group with the same name cannot already exist.
- Requester is set to owner of the new group.

### Delete User
- User must not be the owner of the ADMIN group.
- Delete any groups which this user owns (see delete group)
- Remove this user from all groups.
- User must exist.
- Requester must be admin.
- User cannot delete themselves (specifically ADMIN members)

### Delete Group
- Requester must be owner of group.
- Group must exist
- Remove membership and ownership from all users.
- Group cannot be ADMIN

### Remove User from Group
- Requester must be owner
- If requester is the owner, delete the group (see delete group). All associations must be removed.
- User must exist
- Group must exist
- User must not be the owner of the ADMIN group.

### List Files
- User can only see files of groups they are in.
