package com.assignment.distributedfilesharingapp;

import com.assignment.distributedfilesharingapp.config.AppConfig;
import com.assignment.distributedfilesharingapp.model.Node;
import com.assignment.distributedfilesharingapp.model.RoutingTableDocument;
import com.assignment.distributedfilesharingapp.model.SearchResult;
import com.assignment.distributedfilesharingapp.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class GetDataController {

    private final Node node;

    private final AppConfig appConfig;

    private final FileService fileService;

    private final Environment environment;

    public GetDataController(AppConfig appConfig, FileService fileService, Environment environment, Node node) {
        this.appConfig = appConfig;
        this.fileService = fileService;
        this.environment = environment;
        this.node = node;
    }

    @GetMapping("/nodeui")
    public String getNodeUI(Model model) {
        model.addAttribute("name", node.getUserName());
        model.addAttribute("port", node.getPort());
        model.addAttribute("ipAddress", node.getIpAddress());
        return "nodeui";
    }
    

}
