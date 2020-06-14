package com.semicolon.ds.bootstrap_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class BootstrapServer {

    private static final Logger LOG = Logger.getLogger(BootstrapServer.class.getName());

    public static void main(String[] args) {
        DatagramSocket sock;
        String convertString;
        List<Neighbour> nodes = new ArrayList<Neighbour>();
        try {
            sock = new DatagramSocket(55555);

            LOG.info("Bootstrap Server created at " + sock.getLocalPort() + ". Waiting for incoming data...");

            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data = incoming.getData();
                convertString = new String(data, 0, incoming.getLength());

                //echo the details of incoming data - client ip : client port - client message
                LOG.info(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + convertString);

                StringTokenizer st = new StringTokenizer(convertString, " ");

                String length = st.nextToken();
                LOG.info("Next token size " + length);
                String command = st.nextToken();
                LOG.info("Command going to execute : " + command);

                if (command.equals("REG")) {
                    StringBuilder reply = new StringBuilder("REGOK ");

                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String username = st.nextToken();
                    if (nodes.size() == 0) {
                        reply.append("0");
                        LOG.info("adding new neighbour node : " + ip + ":" + port + " user name : " + username);
                        nodes.add(new Neighbour(ip, port, username));
                    } else {
                        boolean isOkay = true;
                        for (Neighbour node : nodes) {
                            LOG.info("get neighbour node : " + node.getIp() + ":" + node.getPort() + " user name : " + node.getUsername());
                            if (node.getPort() == port) {
                                if (node.getUsername().equals(username)) {
                                    reply.append("9998");
                                    LOG.info("node has same port and user name");
                                } else {
                                    reply.append("9997");
                                    LOG.info("node has same port but different user name");
                                }
                                isOkay = false;
                            }
                        }
                        if (isOkay) {
                            if (nodes.size() == 1) {
                                reply.append("1 ").append(nodes.get(0).getIp()).append(" ").append(nodes.get(0).getPort());
                            } else if (nodes.size() == 2) {
                                reply.append("2 ").append(nodes.get(0).getIp()).append(" ").append(nodes.get(0).getPort()).append(" ").append(nodes.get(1).getIp()).append(" ").append(nodes.get(1).getPort());
                            } else {
                                Random r = new Random();
                                int Low = 0;
                                int High = nodes.size();
                                int random_1 = r.nextInt(High - Low) + Low;
                                int random_2 = r.nextInt(High - Low) + Low;
                                while (random_1 == random_2) {
                                    random_2 = r.nextInt(High - Low) + Low;
                                }
                                LOG.info(random_1 + " " + random_2);
                                reply.append("2 ").append(nodes.get(random_1).getIp()).append(" ").append(nodes.get(random_1).getPort()).append(" ").append(nodes.get(random_2).getIp()).append(" ").append(nodes.get(random_2).getPort());
                            }
                            LOG.info("adding new neighbour node -2 : " + ip + ":" + port + " user name : " + username);
                            nodes.add(new Neighbour(ip, port, username));
                        }
                    }

                    reply.insert(0, String.format("%04d", reply.length() + 5) + " ");
                    LOG.info("REG : replying to the nodes : " + reply);
                    DatagramPacket dpReply = new DatagramPacket(reply.toString().getBytes(), reply.toString().getBytes().length, incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                } else if (command.equals("UNREG")) {
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String username = st.nextToken();

                    LOG.info("Unregister node : " + ip + ":" + port + " user name : " + username);

                    for (int i = 0; i < nodes.size(); i++) {
                        if (nodes.get(i).getPort() == port) {
                            nodes.remove(i);
                            String reply = "0012 UNROK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                            sock.send(dpReply);
                        }
                    }
                } else if (command.equals("ECHO")) {
                    for (Neighbour node : nodes) {
                        LOG.info(node.getIp() + " " + node.getPort() + " " + node.getUsername());
                    }
                    String reply = "0012 ECHOK 0";
                    LOG.info("ECHO : replying to the nodes : " + reply);
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                    sock.send(dpReply);
                }

            }
        } catch (IOException e) {
            System.err.println("IOException " + e);
            LOG.info("An error occurred." + e.getMessage());
        }
    }
}
