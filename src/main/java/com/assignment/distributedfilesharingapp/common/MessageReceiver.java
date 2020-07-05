package com.assignment.distributedfilesharingapp.common;


import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class MessageReceiver extends Thread {
    private final BlockingQueue<ChannelMessage> channelIn;
    private final DatagramSocket socket;
    private volatile boolean process = true;

    public MessageReceiver(BlockingQueue<ChannelMessage> channelIn, DatagramSocket socket) {
        this.channelIn = channelIn;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (process) {

            try {
                byte[] response = new byte[65536];
                DatagramPacket packet = new DatagramPacket(response, response.length);
                socket.receive(packet);
                String address = ((packet.getSocketAddress().toString()).substring(1)).split(":")[0];
                int port = Integer.parseInt(((packet.getSocketAddress().toString()).substring(1)).split(":")[1]);
                String body = new String(response, 0, response.length);
                ChannelMessage message = new ChannelMessage(address, port, body);
                log.info("received the message:{}", message);
                channelIn.put(message);
            } catch (IOException | InterruptedException e) {
                log.error("an error occurred while sending the message", e);
            }
        }
        socket.close();
    }

    public void stopReceiving() {
        this.process = false;
    }
}
