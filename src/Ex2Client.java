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
            System.out.println("Connected to server.\nReceived bytes:");
            while(true) {
                if(i%8  == 0)
                    System.out.println();
                Byte leftByte, rightByte;
                if(i == byteMessage.length) break;
                Integer bytes;
                bytes = is.read() << 4;
                bytes |= is.read();
                System.out.print(Integer.toHexString(bytes& 0XFF).toUpperCase() );
                byteMessage[i++] = bytes.byteValue();
            }

            CRC32 cyclicRedundancyCheck = new CRC32();
            cyclicRedundancyCheck.update(getByteMessage());
            System.out.println("\nGenerated CRC32\t" + Long.toHexString(cyclicRedundancyCheck.getValue()));
            System.out.println(cyclicRedundancyCheck.getValue());
            long crcVal = cyclicRedundancyCheck.getValue();
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");
            for (int j = 3; j >= 0; j-=1){
                os.write((byte)(crcVal >> j * 8));
            }
//            long leftMask = Long.decode("1111111100000000"), rightMask = Long.decode("11111111");
//            leftMask &= cyclicRedundancyCheck.getValue();
//            rightMask &= cyclicRedundancyCheck.getValue();
//            out.print(leftMask);
//            out.print(rightMask);
//            out.println(Long.toHexString(cyclicRedundancyCheck.getValue()));
//            for(byte b : byteArray) {
//                if(i % 4 == 0) {
//                    out.println(msg);
//                    System.out.println("byte is" + msg);
//                    msg = 0;
//                }
//                msg |= b;
//                msg <<= msg;
//                i++;
//            }
            while(true){
                if(is.read() == 1){
                    System.out.println("1\n" + "Response good.");
                } else {
                    System.out.println("0\n" + "Response bad.");
                }
                break;
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