package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;

public class Server {
    private CopyOnWriteArraySet<ClientHandler> clients;

    public Server() throws SQLException {
        AuthService.connect();
//        System.out.println(AuthService.getNickByLoginAndPass("login1","pass1"));
        clients = new CopyOnWriteArraySet<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");

            while(true){
                socket = server.accept();
                System.out.println("Клиент подключился! ");
                new ClientHandler(this,socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }
    public void broadcastMsg(String msg){
        for (ClientHandler c: clients){
            c.sendMsg(msg);
        }

    }
// вещание на конкретного клиента
    public void broadcastMsg(String msg,ClientHandler clientHandler){
        //System.out.println(clientHandler);
        for (ClientHandler c: clients){
            if (clientHandler == c){
                c.sendMsg(msg);
                break;
            }
        }

    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }
}
