package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
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

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился! ");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

    public boolean isNickAuthorized(String nick) {
        boolean isAuth = false;
        for (ClientHandler c : clients) {
            if (c.getNick().equals(nick)) {
                isAuth = true;
            }
        }
        return isAuth;
    }

    public void broadcastMsg(String msg, String sender) {
        for (ClientHandler c : clients) {
            c.sendMsg(sender + ": " + msg);
        }

    }

    // вещание на конкретного клиента
    public void broadcastMsg(String msg, String sender, String receiver) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver) || c.getNick().equals(sender))
                c.sendMsg("private [" + sender + "] to [" + sender + "]" + msg);
        }

    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientlist ");

        for (ClientHandler c : clients) {
            sb.append(c.getNick() + " ");
        }
        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}
