package com.assignment.distributedfilesharingapp.common.ftp;

import com.assignment.distributedfilesharingapp.common.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.io.*;
import java.net.Socket;

@Slf4j
public class DataSendingOperation implements Runnable {

    private Socket clientSocket;
    private String userName;
    private final Environment environment;

    public DataSendingOperation(Socket client, String userName, Environment environment) {
        this.clientSocket = client;
        this.userName = userName;
        this.environment = environment;
    }

    @Override
    public void run() {
        try {
            DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
            String fileName = dIn.readUTF();
            FileManager fileManager = FileManager.getInstance(userName, this.environment.getProperty("app.common.file-name"));
            sendFile(fileManager.getFile(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendFile(File file) {
        try {
            //handle file read
            byte[] myByTearray = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(myByTearray, 0, myByTearray.length);
            //handle file send over socket
            OutputStream os = clientSocket.getOutputStream();
            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            dos.writeLong(myByTearray.length);
            dos.write(myByTearray, 0, myByTearray.length);
            dos.flush();
            fis.close();
            log.info("File " + file.getName() + " sent to client.");
        } catch (Exception e) {
            log.error("File does not exist!", e);
        }
    }
}
