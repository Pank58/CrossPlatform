package com.mycompany.laba6server.client;

import java.net.*;
import java.util.concurrent.*;

public class Client {

    private static final String HOST = "192.168.43.181";
    private static final int PORT = 5000;

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket();

        socket.send(new DatagramPacket(
                "REGISTER".getBytes(),
                8,
                InetAddress.getByName(HOST),
                PORT
        ));

        System.out.println("Client started");

        while (true) {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength());

            if (msg.startsWith("TASK")) {

                String[] p = msg.split(";");
                double a = Double.parseDouble(p[1]);
                double b = Double.parseDouble(p[2]);
                double h = Double.parseDouble(p[3]);

                ExecutorService ex = Executors.newFixedThreadPool(3);

                double part = (b - a) / 3;

                Future<Double> f1 = ex.submit(() -> calc(a, a + part, h));
                Future<Double> f2 = ex.submit(() -> calc(a + part, a + 2 * part, h));
                Future<Double> f3 = ex.submit(() -> calc(a + 2 * part, b, h));

                double res = f1.get() + f2.get() + f3.get();

                ex.shutdown();

                String response = "RESULT;" + res;

                socket.send(new DatagramPacket(
                        response.getBytes(),
                        response.length(),
                        packet.getAddress(),
                        packet.getPort()
                ));
            }
        }
    }

    private static double calc(double start, double end, double step) {
        double sum = 0;
        double x;

        for (x = start; x < end; x += step) {
            sum += Math.cos(x) * step;
        }

        if (x > end && (x - step) < end) {
            sum += Math.cos(end) * (end - (x - step));
        }

        return sum;
    }
}