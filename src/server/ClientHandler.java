package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
//import java.util.Scanner;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    //   private Scanner console;
    private String nick;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            //           console = new Scanner(System.in);

            new Thread(() -> {
                try {
                    //цикл авторизации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] token = str.split(" ");
                            String newNick = AuthService.getNickByLoginAndPass(
                                    token[1], token[2]);
                            if (newNick != null) {
                                if (server.isNickAuthorized(newNick)) {
                                    sendMsg("Учетная запись уже авторизована!");
                                } else {
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    server.subscribe(this);
                                    break;
                                }

                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        //вариант приватного соощения
                        if (str.startsWith("/w ")) {
                            String token[] = str.split(" ", 3);
                            server.broadcastMsg(token[2], getNick(), token[1]);
                        } else {
                            server.broadcastMsg(str, getNick());
                        }
//                        System.out.println("Client "+str);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
