package com.assignment.distributedfilesharingapp.common.ftp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


@Slf4j
public class FTPServer implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private String userName;
    private Environment environment;

    public FTPServer(int port, String userName, Environment environment) throws Exception {
        serverSocket = new ServerSocket(port);
        this.userName = userName;
        this.environment = environment;
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                log.error("an error occurred while accepting files", e);
            }
            Thread ftpThread = new Thread(new DataSendingOperation(clientSocket, userName, environment));
            ftpThread.start();
        }
    }
}
