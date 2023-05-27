package org.sdg3.gestor;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


public class BrokerGestor {

    private static String[] ipSede = {"10.43.100.191", "10.43.100.187"};

    public static void main(String[] args)
    {
        //  Prepare our context and sockets
        try (ZContext context = new ZContext()) {
            //  Socket facing clients
            ZMQ.Socket central = context.createSocket(SocketType.ROUTER);
            central.bind("tcp://"+ipSede[Integer.parseInt(args[0])]+":5555");

            //  Socket facing services
            ZMQ.Socket op = context.createSocket(SocketType.DEALER);
            op.bind("tcp://"+ipSede[Integer.parseInt(args[0])]+":5556");

            //  Start the proxy
            ZMQ.proxy(central, op, null);
        }
    }
}