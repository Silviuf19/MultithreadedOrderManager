import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public class OrderManager  extends BufferFileReader implements Runnable {
    static File ordersFile;
    public Map<String, Integer> orders;
    public static ExecutorService tpe;
    public static AtomicInteger inQueue;
    public static CyclicBarrier remainingThreadsBarrier;
    public static BufferedWriter orderWriter;
    OrderManager(int id) {
        this.id = id;
        try {
            this.f = new FileInputStream(ordersFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private void executeSecondThreads() {
        // Goes through each order and creates a number of tasks equal to the number of products in the order
        for(Map.Entry<String, Integer> order : orders.entrySet()){
            // Ignores the order if it has no products
            if(order.getValue() == 0) {
                continue;
            }
            ConcurrentHashMap<Integer, Boolean> productLines = new ConcurrentHashMap<>();
            CountDownLatch latch = new CountDownLatch(order.getValue());
            Semaphore mutex = new Semaphore(1);
            AtomicInteger remainingIndex = new AtomicInteger(0);
            for(int i = 0; i < order.getValue(); i++) {
                tpe.submit(new ProductManager(order, tpe, latch, i, mutex));
            }
            // Wait for the above tasks to finish
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // When the order is finished, write the order to the file
            synchronized (this) {
                try {
                    orderWriter.write(order.getKey() + "," + order.getValue() + ",shipped\n");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // Wait for all the orders to finish
        try{
            remainingThreadsBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tpe.shutdown();
    }

    // Parse the orders file and create a map of orders
    public Map<String, Integer> parseBuffer(byte[] buffer) {
        int index = 0;
        Map<String, Integer> orders = new HashMap<>();
        while(buffer[index] != 0){
            StringBuilder id = new StringBuilder();
            StringBuilder numOfProducts = new StringBuilder();
            while(buffer[index] != (int)',' && buffer[index] != 0){
                id.append((char)buffer[index]);
                index++;
            }
            index++;
            while(buffer[index] != 10 && buffer[index] != 0) {
                numOfProducts.append((char)buffer[index]);
                index++;
            }
            if(numOfProducts.length() != 0) {
                orders.put(id.toString(), Integer.parseInt(numOfProducts.toString()));
            }
            index++;
        }
        return orders;
    }

    @Override
    public void run() {
        readFile();
        // Wait for all the threads to finish reading the file
        try{
            Tema2.barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create a new barrier for the remaining threads
        if(id==0){
            remainingThreadsBarrier = new CyclicBarrier(numOfActiveThreads.get());
        }
        // Only run if the threads are still active
        if(isActive) {
           orders = parseBuffer(mainBuffer);
           executeSecondThreads();
        }
    }
}