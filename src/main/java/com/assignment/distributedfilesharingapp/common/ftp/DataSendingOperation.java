package com.assignment.distributedfilesharingapp.common.ftp;

import com.assignment.distributedfilesharingapp.common.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;

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
            File file = fileManager.getFile(fileName);
            sendFile(file, updateFileContent(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Long updateFileContent(File file) throws IOException {
        log.info("update the file content {}", file.getAbsolutePath());
        long randomNum;
        do {
            randomNum = (long) (Math.random() * 10);
        } while (randomNum == 0);
        log.info("generated file size is {} bites", randomNum * 1024 * 1024);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.setLength(randomNum * 1024 * 1024);
        randomAccessFile.close();
        return randomNum;
    }


    public void sendFile(File file, Long fileSize) {
        try {
            //handle file read
            byte[] buffer = new byte[(int) file.length()];
            int count;
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            dis.readFully(buffer, 0, buffer.length);
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            byte[] hash = digest.digest();
            log.info("following is the SHA encoded file {} file size {}", new BASE64Encoder().encode(hash), fileSize);
            bis.close();
            //handle file send over socket
            OutputStream os = clientSocket.getOutputStream();
            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            dos.writeLong(buffer.length);
            dos.write(buffer, 0, buffer.length);
            dos.flush();
            fis.close();
            log.info("File " + file.getName() + " sent to client.");
        } catch (Exception e) {
            log.error("File does not exist!", e);
        }
    }
}
