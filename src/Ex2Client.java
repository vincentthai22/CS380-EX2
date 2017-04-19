import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;

/**
 * Created by Vincent on 4/18/2017.
 */
public class Ex2Client {

    private final static String SERVER_NAME = "codebank.xyz";
    private final static int PORT_NUMBER = 38102,
                             MAX_MESSAGE_SIZE=100;

    public Ex2Client(){

    }

    public static void main(String[] args){
        ReceiverClient receiverClient = new ReceiverClient(SERVER_NAME, PORT_NUMBER, MAX_MESSAGE_SIZE);
        new Thread(receiverClient).start();
    }
}
class ReceiverClient implements Runnable {
    String host;
    int port;
    byte[] byteMessage;
    public boolean isRunning;

    public ReceiverClient(String host, int port, int maxMessageSize){
        this.host = host;
        this.port=port;
        byteMessage = new byte[maxMessageSize];
        isRunning = true;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port)) {
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            int i = 0;
            while(true) {
                Byte leftByte, rightByte;
                if(i == byteMessage.length) break;
                Integer bytes;
                bytes = is.read() << 2;
                bytes |= is.read();
                System.out.print(Integer.toHexString(bytes));
                if(i%8  == 0)
                    System.out.println();
                byteMessage[i++] = bytes.byteValue();
            }

            CRC32 cyclicRedundancyCheck = new CRC32();
            cyclicRedundancyCheck.update(getByteMessage());
            System.out.println("\nGenerated CRC32\t" + Long.toHexString(cyclicRedundancyCheck.getValue()));
            new Thread(new SenderClient(host, port, cyclicRedundancyCheck.getValue())).start();
            while(true){
                System.out.println(is.read());
            }
        } catch (NullPointerException e){
            System.out.println("Connection Lost");
        } catch (IOException e){

        } catch (Exception e) {
            e.printStackTrace();
        }
//        for(byte b : byteMessage)
//            System.out.println("\nbytemsg " + b);


    }

    public byte[] getByteMessage(){
        return byteMessage;
    }
}
class SenderClient implements Runnable {
    String host;
    int port;
    long message;
    byte[] byteArray;
    String stringByteArray;

    public SenderClient(String host, int port,long message ){
        this.host = host;
        this.port=port;
        this.message = message;
        byteArray = Long.toBinaryString(message).getBytes();
        stringByteArray = Long.toBinaryString(message);

        //System.out.println("size of byte arr " + stringByteArray);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port)) {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");
            int msg = 0;
            String message="";
            while(true) {
//                for(byte b : byteArray) {
//                    msg |= b;
//                    msg <<= msg;
//                    if(i % 4 == 0) {
//                        out.println(msg);
//                        System.out.println("byte is" + msg);
//                        msg = 0;
//                    }
//                    i++;
//                }
                for(int i =0 ; i < stringByteArray.length(); i++){
                    if(i % 4 == 0){
                        out.print(message);
                        System.out.println(message);
                        message = "";
                    }

                    message += stringByteArray.charAt(i);
                }
                break;
            }
        } catch (NullPointerException e){
            System.out.println("Failed to connect.. try again.");
        } catch (IOException e){

        }
    }
}