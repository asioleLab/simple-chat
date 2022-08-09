import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ChatThread implements Runnable{
    private final Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean running;
    private List<PrintWriter> allUsers;
    private String username;

    public ChatThread(Socket socket, List<PrintWriter> allUsers) {
        this.socket = socket;
        this.allUsers = allUsers;

        try{
            writer=new PrintWriter(socket.getOutputStream());
            reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running=true;
            this.allUsers.add(writer);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        // ask for username
        writer.println("Enter your name:" );
        writer.flush();
        username=this.getText();
        this.broadcastMessage("joined the chat room");
        while (running){
            String message = this.getText();
            if(message == null){
                running = false;
            }else{
                this.broadcastMessage(message);
            }
        }
        this.allUsers.remove(allUsers.indexOf(writer));
        this.closeCloseable(reader);
        this.closeCloseable(writer);
        this.closeCloseable(socket);
    }

    private void closeCloseable(AutoCloseable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    private void broadcastMessage(String message){
        for (PrintWriter w:allUsers){
            if(!w.equals(writer)){
                w.println(username + ": "+ message);
                w.flush();
            }
        }
    }

    private String getText(){
        String response= null;
        try{
            response=reader.readLine();
        }catch(IOException ex){
            System.err.println(ex.getMessage());
        }
        return response;
    }
}
