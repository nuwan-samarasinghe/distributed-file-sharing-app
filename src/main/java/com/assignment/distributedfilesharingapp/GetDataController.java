package com.assignment.distributedfilesharingapp;

import com.assignment.distributedfilesharingapp.config.AppConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetDataController {

    private final AppConfig appConfig;

    public GetDataController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping(value = "/ip-table")
    private void printIpTable() {
        appConfig.getMessageBrokerThread().getRoutingTable().printRoutingTable();
    }

}
