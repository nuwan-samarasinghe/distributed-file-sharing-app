package com.assignment.distributedfilesharingapp;

import com.assignment.distributedfilesharingapp.config.AppConfig;
import com.assignment.distributedfilesharingapp.model.RoutingTableDocument;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import com.assignment.distributedfilesharingapp.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
public class GetDataController {

    private final AppConfig appConfig;

    private final FileService fileService;

    private final Environment environment;

    public GetDataController(AppConfig appConfig, FileService fileService, Environment environment) {
        this.appConfig = appConfig;
        this.fileService = fileService;
        this.environment = environment;
    }

    @GetMapping(value = "/ip-table")
    private RoutingTableDocument printIpTable() {
        return appConfig.getMessageBrokerThread().getRoutingTable().getRoutingTableDocument();
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

    @PostMapping(value = "/file/{fileName}")
    private ResponseEntity<Map<String, SearchResult>> getSearchedFileList(@PathVariable("fileName") String fileName) {
        Map<String, SearchResult> stringSearchResultMap = this.appConfig.doSearch(fileName);
        return ResponseEntity.ok().body(stringSearchResultMap);
    }

    @PostMapping(value = "/file/download")
    private void downloadFile(@RequestBody SearchResult searchResult) {
        log.info("downloading the following file {}", searchResult);
        try {
            fileService.startReceiveFile(searchResult);
        } catch (Exception e) {
            log.error("an error occurred while downloading a file", e);
        }
    }

}
