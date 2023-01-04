import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    static String folder_input;
    public static int P;
    public static File orders;
    public static File products;
    public static CyclicBarrier barrier;

    public static void main(String[] args) throws Exception{
        // Input testing
        if(!Helpers.fileExists(args[0] + "/orders.txt")){
            throw new Exception(Helpers.fileReadError);
        }
        if(!Helpers.isNumeric(args[1])) {
            throw new Exception(Helpers.numberErrorMessage);
        }

        // Getting the input
        P = Integer.parseInt(args[1]);
        folder_input = args[0];

        barrier = new CyclicBarrier(P);

        orders = new File(folder_input + "/orders.txt");
        products = new File(folder_input + "/order_products.txt");

        // Creating the output files
        File file = new File("orders_out.txt");
        File file2 = new File("order_products_out.txt");
        if (!file.exists() && !file2.exists()) {
            file.createNewFile();
            file2.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        FileWriter fw2 = new FileWriter(file2.getAbsoluteFile());
        BufferedWriter orderWriter = new BufferedWriter(fw);
        BufferedWriter orderProductWriter = new BufferedWriter(fw2);

        // Giving arguments to the OrderManager
        Thread[] threads = new Thread[P];
        OrderManager.ordersFile = orders;
        OrderManager.numOfChars = (int)orders.length();
        OrderManager.inQueue = new AtomicInteger(0);
        OrderManager.tpe = Executors.newFixedThreadPool(P);
        OrderManager.P = P;
        OrderManager.orderWriter = orderWriter;

        // Giving arguments to the ProductManager
        ProductManager.productsFile = folder_input + "/order_products.txt";
        ProductManager.orderProductWriter = orderProductWriter;

        // Starting the threads
        for (int i = 0; i < P; i++) {
            threads[i] = new Thread(new OrderManager(i));
            threads[i].start();
        }
        // Waiting for the threads to finish
        for (int i = 0; i < P; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Closing the output files
        orderWriter.close();
        orderProductWriter.close();
    }
}
