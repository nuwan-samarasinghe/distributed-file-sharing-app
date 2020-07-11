package com.assignment.distributedfilesharingapp.common.fileTranfer;

import lombok.extern.slf4j.Slf4j;

import java.net.Socket;

@Slf4j
public class FileTransferClient {

    public FileTransferClient(String IpAddress, int port, String fileName) throws Exception {
        Socket serverSock = new Socket(IpAddress, port);
        log.info("Connecting...");
        new Thread(new DataReceivingOperation(serverSock, fileName)).start();
    }
}
