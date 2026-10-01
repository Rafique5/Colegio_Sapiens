import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class ServidorFront {
    public static void main(String[] args) throws Exception {
        HttpServer s = SimpleFileServer.createFileServer(
                new InetSocketAddress(5500),
                Path.of("C:\\projeto\\frontend"),
                SimpleFileServer.OutputLevel.INFO);
        s.setExecutor(Executors.newFixedThreadPool(8));
        s.start();
        System.out.println("Front a correr em http://localhost:5500");
    }
}