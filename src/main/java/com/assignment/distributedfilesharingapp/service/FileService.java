package com.assignment.distributedfilesharingapp.service;

import com.assignment.distributedfilesharingapp.common.ftp.FTPClient;
import com.assignment.distributedfilesharingapp.common.ftp.FTPServer;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileService {

    private final Node node;

    private final Environment environment;

    public FileService(Node node, Environment environment) {
        this.node = node;
        this.environment = environment;
        try {
            new Thread(new FTPServer(node.getPort() + 100, node.getUserName(), environment)).start();
        } catch (Exception e) {
            log.error("error occurred while starting the FTP server", e);
        }
    }

    public void startReceiveFile(SearchResult fileDetail) throws Exception {
        new FTPClient(fileDetail.getAddress().trim(), fileDetail.getTcpPort(), fileDetail.getFileName().trim());
        System.out.println("Waiting for file download...");
        Thread.sleep(2000);
    }
}
