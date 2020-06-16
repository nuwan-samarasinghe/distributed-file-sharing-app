package com.assignment.distributedfilesharingapp;

import com.assignment.distributedfilesharingapp.config.AppConfig;
import com.assignment.distributedfilesharingapp.model.RoutingTableDocument;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class GetDataController {

    private final AppConfig appConfig;

    public GetDataController(AppConfig appConfig) {
        this.appConfig = appConfig;
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
    private ResponseEntity<List<String>> getFileList() {
        log.info(this.appConfig.getFileManager().getFileNames());
        return ResponseEntity.ok().body(this.appConfig.getFileManager().getFileNamesList());
    }

    @PostMapping(value = "/file/{fileName}")
    private ResponseEntity<Map<String, SearchResult>> getDownloadFile(@PathVariable("fileName") String fileName) {
        Map<String, SearchResult> stringSearchResultMap = this.appConfig.doSearch(fileName);
        return ResponseEntity.ok().body(stringSearchResultMap);
    }

}
