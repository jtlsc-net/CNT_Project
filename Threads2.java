import java.net.*;
import java.io.*;

class Threads2 extends Thread{
    private static final int sPort = 8001;
    private static final int cPort = 8000;
    private Thread t;
    private String threadName;
    private int threadType;

    private Socket connection;
    private int clientNo;		//The index number of the client

    Threads2 (String threadName){
        this.threadType = 2;
        this.threadName = threadName;
    }
    //Type 0 = server, type 1 = client
    Threads2 (String threadName, Socket connection, int no, int type) {
        // threadName = name;
        // System.out.println("Creating " + threadName);
        this.threadName = threadName;
        this.connection = connection;
        this.clientNo = no;
        this.threadType = type;
    }
    public void run(){
        if (threadType == 0)
        {
            //Start server
            
            System.out.println("Running " + threadName);
            try{
                for(int i = 4; i > 0; i--) {
                    System.out.println("Thread: " + threadName + ", " + i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e){
                System.out.println("Thread " + threadName + " interrupted.");
            }
            System.out.println("Thread " + threadName + " exiting.");
        }

        else if (threadType == 1)
        {
            //Start client
            try{
                this.connection = new Socket("localhost", cPort);
            }
            catch (ConnectException e){
                System.err.println(e);
            }
            catch(IOException e){
                System.err.println(e);
            }
            finally{
                try{
                    this.connection.close();
                }
                catch(IOException e)
                {
                    System.err.println(e);   
                }
            }
            // System.out.println("Running " + threadName);
            // try{
            //     for(int i = 8; i > 4; i--) {
            //         System.out.println("Thread: " + threadName + ", " + i);
            //         Thread.sleep(50);
            //     }
            // } catch (InterruptedException e){
            //     System.out.println("Thread " + threadName + " interrupted.");
            // }
            // System.out.println("Thread " + threadName + " exiting.");
        }

        else if (threadType == 2){
            try{
                ServerSocket listener = new ServerSocket(sPort);
                int clientNum = 1;
                try {
                    while(true) {
                        new Threads2("Server", listener.accept(),clientNum, 0).start();
                        System.out.println("Client "  + clientNum + " is connected!");
                        clientNum++;
                    }
                } finally {
                    listener.close();
                } 
            }
            catch(IOException e){
                System.out.println("IO exception on server listener");
                System.out.println(e);
            }
        }
        else
        {
            System.out.println("ERRRORRR making Threads2: bad type");
        }
    }
    public void start () {
        System.out.println("Starting " + threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start();
        }
    }
    public static void main(String args[]) throws Exception {
        

        Threads2 R1 = new Threads2("Thread-1");
        R1.start();

        // Socket requestSocket = null;
        // Threads2 R2 = new Threads2(requestSocket,1,1);
        // R2.start();
    }
}
