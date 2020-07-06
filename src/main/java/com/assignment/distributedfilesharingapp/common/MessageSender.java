package com.assignment.distributedfilesharingapp.common;

import com.assignment.distributedfilesharingapp.model.ChannelMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class MessageSender extends Thread {
    private final BlockingQueue<ChannelMessage> channelOut;
    private final DatagramSocket socket;
    private volatile boolean process = true;

    public MessageSender(BlockingQueue<ChannelMessage> channelOut, DatagramSocket socket) {
        this.channelOut = channelOut;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (process) {
            try {
                ChannelMessage message = channelOut.take();
                String address = message.getAddress();
                int port = message.getPort();
                String payload = message.getMessage();
                // log.info("sending message {}", message);
                DatagramPacket packet = new DatagramPacket(payload.getBytes(), payload.length(), InetAddress.getByName(address), port);
                socket.send(packet);
            } catch (IOException e) {
                log.error("an error occurred while sending the message", e);
            } catch (InterruptedException e) {
                log.info("interrupting the thread {}", Thread.interrupted());
            }
        }
        socket.close();
    }

    public void stopSending() {
        this.process = false;
    }
}


