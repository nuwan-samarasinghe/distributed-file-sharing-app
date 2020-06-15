package com.assignment.distributedfilesharingapp.common;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class IpPortConverter {

    @Getter
    private final Function<String[], List<InetSocketAddress>> stringToIPPortConverter = strings -> {
        List<InetSocketAddress> neighbourNodes = new ArrayList<>();
        for (int i = 3; i < strings.length - 1; i++) {
            InetSocketAddress node = new InetSocketAddress(strings[i], Integer.parseInt(strings[i + 1]));
            neighbourNodes.add(node);
            i++;
        }
        return neighbourNodes;
    };

}
