package com.assignment.distributedfilesharingapp;

import com.assignment.distributedfilesharingapp.config.AppConfig;
import com.assignment.distributedfilesharingapp.model.FileDownloadModel;
import com.assignment.distributedfilesharingapp.model.Neighbour;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import com.assignment.distributedfilesharingapp.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class GetDataRestController {

    private final Node node;

    private final AppConfig appConfig;

    private final FileService fileService;

    private final Environment environment;

    public GetDataRestController(Node node, AppConfig appConfig, FileService fileService, Environment environment) {
        this.node = node;
        this.appConfig = appConfig;
        this.fileService = fileService;
        this.environment = environment;
    }

    @GetMapping(value = "/ip-table")
    private List<Neighbour> printIpTable() {
        return appConfig.getMessageBrokerThread().getRoutingTable().getNeighbours();
    }

    @GetMapping(value = "/node")
    private ResponseEntity<Node> getNode() {
        return ResponseEntity.ok().body(node);
    }

    @DeleteMapping(value = "/node")
    private ResponseEntity deleteNode() {
        this.appConfig.unregisterNode();
        this.appConfig.stopApplication();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/file")
    private ResponseEntity<List<String>> getCurrentNodeFileList() {
        log.info(this.appConfig.getFileManager().getFileNames());
        return ResponseEntity.ok().body(this.appConfig.getFileManager().getFileNamesList());
    }

    @GetMapping(value = "/file/{fileName}")
    private ResponseEntity<List<SearchResult>> getSearchedFileList(@PathVariable("fileName") String fileName) {
        return ResponseEntity.ok().body(new ArrayList<>(this.appConfig.doSearch(fileName).values()));
    }

    @PostMapping(value = "/file/download")
    private FileDownloadModel downloadFile(@RequestBody SearchResult searchResult) {
        log.info("downloading the following file {}", searchResult);
        try {
            fileService.startReceiveFile(searchResult);
            return new FileDownloadModel("Download started! Check the application root folder of the server.", HttpStatus.OK);
        } catch (Exception e) {
            log.error("an error occurred while downloading a file", e);
            return new FileDownloadModel("Download error!", HttpStatus.BAD_REQUEST);
        }
    }

}
