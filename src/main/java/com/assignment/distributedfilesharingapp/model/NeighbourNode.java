package com.assignment.distributedfilesharingapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NeighbourNode {
    private String ipAddress;
    private Integer port;
}
