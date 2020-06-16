package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

import java.util.List;

@Data
public class RoutingTableDocument {
    private String address;
    private Integer port;
    private List<Neighbour> neighbours;
}
