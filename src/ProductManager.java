import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

public class ProductManager implements Runnable{
    public ExecutorService tpe;
    String orderID;
    public int myIndex;
    public int numOfProducts;
    public int id;
    public CountDownLatch latch;
    public static String productsFile;
    public BufferedReader br;
    public Semaphore mutex;
    public static BufferedWriter orderProductWriter;
    ProductManager(Map.Entry<String, Integer> order, ExecutorService tpe, CountDownLatch latch, int id, Semaphore mutex) {
        this.tpe = tpe;
        this.orderID = order.getKey();
        this.numOfProducts = order.getValue();
        this.latch = latch;
        this.id = id;
        this.mutex = mutex;
        this.myIndex = 0;
        try {
            this.br = new BufferedReader(new FileReader(productsFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try{
            String line;
            // Goes through the file until it finds the line that it needs to read
            while((line = br.readLine()) != null) {
                String lineOrderID = line.split(",")[0];
                String productID = line.split(",")[1];
                if(lineOrderID.equals(orderID)) {
                    if (myIndex == id) {
                        mutex.acquire();
                            try {
                                orderProductWriter.write(lineOrderID + "," + productID + ",shipped\n");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        mutex.release();
                        latch.countDown();
                        return;
                        // If the line is not the one that the thread needs to read, it will skip it
                    } else {
                        myIndex++;
                    }
                }
            }
        }
         catch (Exception e) {
            e.printStackTrace();
        }
    }
}
