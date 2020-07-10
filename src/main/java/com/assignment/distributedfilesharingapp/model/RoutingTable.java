package com.assignment.distributedfilesharingapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class RoutingTable {

    @Getter
    private final String nodeIp;
    @Getter
    private final int nodePort;

    @Getter
    private final List<Neighbour> neighbours = new CopyOnWriteArrayList<>();

    public Integer addNeighbour(String neighbourIpAddress, Integer neighbourPort, Integer maxNeighbours) {
        if (!neighbours.isEmpty()) {
            for (Neighbour neighbour : neighbours) {
                if (neighbour.getAddress().equals(neighbourIpAddress) && neighbour.getPort().equals(neighbourPort)) {
                    neighbour.Ping();
                    return neighbours.size();
                }
            }
        }
        if (neighbours.size() >= maxNeighbours) {
            log.info("Cannot add neighbour : " + neighbourIpAddress + ":" + neighbourPort);
            return 0;
        }
        synchronized (RoutingTable.class) {
            neighbours.add(new Neighbour(neighbourIpAddress, neighbourPort));
            log.info("Adding neighbour : " + neighbourIpAddress + ":" + neighbourPort);
        }
        return neighbours.size();
    }

    public Integer removeNeighbour(String address, Integer port) {
        if (!neighbours.isEmpty()) {
            neighbours.forEach(neighbour -> {
                if (neighbour.getAddress().equals(address) && neighbour.getPort().equals(port)) {
                    log.info("remove neighbour address {} and port is {}", address, port);
                    neighbours.remove(neighbour);
                }
            });
            return neighbours.size();
        }
        return 0;
    }

    public synchronized Integer getNeighboursCount() {
        return neighbours.size();
    }

    public synchronized void printRoutingTable() {
        log.info("\n\n--------------------------------");
        log.info("Total neighbours: {}", neighbours.size());
        log.info("--------------------------------");
        neighbours.forEach(neighbour -> log.info("Address: {} Port: {} Ping: {}", neighbour.getAddress(), neighbour.getPort(), neighbour.getPingPongs()));
        log.info("--------------------------------\n\n");
    }

    public boolean isANeighbour(String address, int port) {
        return neighbours
                .stream()
                .map(neighbour -> neighbour.getAddress().equals(address) && neighbour.getPort().equals(port))
                .findFirst()
                .orElse(false);
    }

    public List<Neighbour> getOtherNeighbours(String address, int port) {
        return neighbours.stream()
                .filter(neighbour -> !(neighbour.getAddress().equals(address) && neighbour.getPort().equals(port)))
                .collect(Collectors.toList());
    }
}
