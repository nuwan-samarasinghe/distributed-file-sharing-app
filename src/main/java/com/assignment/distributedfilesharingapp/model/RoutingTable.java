package com.assignment.distributedfilesharingapp.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class RoutingTable {

    @Getter
    private final List<Neighbour> neighbours = new CopyOnWriteArrayList<>();
    @Getter
    private final String address;
    @Getter
    private final int port;

    public RoutingTable(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public Integer addNeighbour(String address, Integer port, Integer clientPort, Integer maxNeighbours) {
        if (!neighbours.isEmpty()) {
            for (Neighbour neighbour : neighbours) {
                if (neighbour.getAddress().equals(address) && neighbour.getPort().equals(port)) {
                    neighbour.Ping();
                    return neighbours.size();
                }
            }
        }
        if (neighbours.size() >= maxNeighbours) {
            log.info("Cannot add neighbour : " + address + ":" + port);
            return 0;
        }
        synchronized (RoutingTable.class) {
            neighbours.add(new Neighbour(address, port, clientPort));
            log.info("Adding neighbour : " + address + ":" + port);
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
        log.info("Address: {}:{}", address, port);
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
