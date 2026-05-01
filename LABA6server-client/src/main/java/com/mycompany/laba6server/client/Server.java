package com.mycompany.laba6server.client;

import java.net.*;
import java.util.*;

public class Server {

    private static final int PORT = 5000;
    private static final int BUF = 1024;

    private static final List<ClientInfo> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("Server started");

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[BUF], BUF);
            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength());

            if (msg.equals("REGISTER")) {
                clients.add(new ClientInfo(packet.getAddress(), packet.getPort()));
                System.out.println("Client registered: " + packet.getPort());
            }

            if (msg.startsWith("TASK")) {

                if (clients.isEmpty()) {
                    System.out.println("No clients!");
                    continue;
                }

                String[] p = msg.split(";");
                double a = Double.parseDouble(p[1]);
                double b = Double.parseDouble(p[2]);
                double h = Double.parseDouble(p[3]);

                double part = (b - a) / clients.size();

                for (int i = 0; i < clients.size(); i++) {
                    double start = a + i * part;
                    double end = (i == clients.size() - 1) ? b : start + part;

                    String task = "TASK;" + start + ";" + end + ";" + h;

                    socket.send(new DatagramPacket(
                            task.getBytes(),
                            task.length(),
                            clients.get(i).address,
                            clients.get(i).port
                    ));
                }

                double total = 0;

                for (int i = 0; i < clients.size(); i++) {
                    DatagramPacket resp = new DatagramPacket(new byte[BUF], BUF);
                    socket.receive(resp);

                    String res = new String(resp.getData(), 0, resp.getLength());
                    total += Double.parseDouble(res.split(";")[1]);
                }

                String result = "RESULT;" + total;

                socket.send(new DatagramPacket(
                        result.getBytes(),
                        result.length(),
                        packet.getAddress(),
                        packet.getPort()
                ));
            }
        }
    }

    static class ClientInfo {
        InetAddress address;
        int port;

        ClientInfo(InetAddress a, int p) {
            address = a;
            port = p;
        }
    }
}