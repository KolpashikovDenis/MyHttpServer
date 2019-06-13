import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

public class Main {

    static int n = 0;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(10001), 0);
        server.createContext("/hello", new HelloHandler());
        server.createContext("/upload", new UploadHandler());
        server.setExecutor(null);
        server.start();
    }

    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String resp = "<form action=\"upload\" method=\"POST\" enctype=\"multipart/form-data\">\n" +
                    "    <input type=\"file\" name=\"myfile\">\n" +
                    "    <br/>\n" +
                    "    <input type=\"submit\" name=\"Submit\">\n" +
                    "</form>";

            Headers hd = he.getResponseHeaders();
            hd.set("Content-Type", "text/html");
            he.sendResponseHeaders(200, resp.getBytes().length);

            OutputStream os = he.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }
    }

    static class UploadHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange he) throws IOException {
            n += 1;
            String fTmp = String.format("tmp_%d.tmp", n);

            Headers rh = he.getResponseHeaders();
            rh.set("Content-Type", "text/plain");

//            rh.set("Location", "/hello");
            he.sendResponseHeaders(200, 0);

//            StringBuilder sb = new StringBuilder();
            Headers hdrs = he.getRequestHeaders();
            int fSize = Integer.parseInt(hdrs.get("Content-length").get(0));
            OutputStream writer = he.getResponseBody();
            InputStream reader = he.getRequestBody();
            int n;
            FileOutputStream fOut = new FileOutputStream(fTmp);
            byte[] b = new byte[65536];
            while(true){
                n = reader.read(b);
                if (n == -1) {
                    break;
                }
                fOut.write(b, 0, n);
            }
            fOut.close();
            reader.close();
            writer.write("File passed".getBytes());
            writer.close();

            // Read tmp file
            RandomAccessFile r = new RandomAccessFile(fTmp, "r");

            String key = r.readLine();
            String line = r.readLine();
            String fName = line.substring(line.indexOf("filename=")+10, line.length()-1);
            RandomAccessFile w = new RandomAccessFile(fName, "rw");
            r.readLine();
//            r.readLine();

            byte bb;
            StringBuilder sb = new StringBuilder();
            while(true){
                bb = r.readByte();
                sb.append((char)bb);
                if (bb == '\n'){
                    if(sb.toString().contains(key)){
                        break;
                    }
                    w.write(sb.toString().getBytes());
                    sb.setLength(0);
                }
            }
            r.close();
            w.close();
            File file = new File(fTmp);
            file.delete();
        }
    }
}
