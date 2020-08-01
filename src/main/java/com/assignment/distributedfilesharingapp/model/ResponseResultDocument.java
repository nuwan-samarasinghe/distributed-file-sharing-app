package com.assignment.distributedfilesharingapp.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseResultDocument {
    private List<SearchResult> searchResultList;
    private NodeQueryStatisticsModel statisticsModel;
    private Integer neighbourCount;
}
