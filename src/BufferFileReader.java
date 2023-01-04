import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class BufferFileReader {
    public byte[] mainBuffer;
    public FileInputStream f;
    static int numOfChars;
    public int id;
    static int P;
    static ConcurrentHashMap<Integer, Integer> ends = new ConcurrentHashMap<>();
    boolean isActive = true;
    static AtomicInteger numOfActiveThreads = new AtomicInteger(0);
    public void readFile() {
        // If the number of threads is larger than the number of characters in the orders.txt files, the program panics!
        if (P > numOfChars) {
            try {
                throw new Exception(Helpers.panicThreadsError);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Initial division of the file
        int start = id * numOfChars / P;
        int end = Math.min((id + 1) * numOfChars / P, numOfChars);

        // Buffers for storing the info from the file
        byte[] buffer = new byte[20000];
        byte[] buffer2;
        int startBufferIndex = 0;
        try {
            // Read the bytes by the initial division
            f.skip(start);
            f.readNBytes(buffer, 0, end - start);
            // If the end is in the middle of the order, the end moves to the next \n
            int bufferLength = end - start;
            while (buffer[bufferLength - 1] != 10 && start + bufferLength < numOfChars) {
                f.read(buffer, bufferLength, 1);
                bufferLength++;
            }
            // Wait for all the threads to finish reading
            Tema2.barrier.await();
            // If there are multiple orders with the same end, we choose the longest one
            if (!ends.containsKey(start + bufferLength) || ends.get(start + bufferLength) < bufferLength) {
                ends.put(start + bufferLength, bufferLength);
            }
            // Waiting to get the longest order from each thread
            Tema2.barrier.await();
            if (ends.get(start + bufferLength) == bufferLength) {
                buffer2 = buffer;
            } else {
                isActive = false;
                return;
            }
            // If the start of the order sequence is not where it should be, it is moved
            while (buffer2[startBufferIndex + 1] != 95) {
                startBufferIndex++;
                if (startBufferIndex > end - start) {
                    isActive = false;
                    return;
                }
            }
            // Finally we got the orders
            mainBuffer = Arrays.copyOfRange(buffer2, startBufferIndex, buffer2.length);
            numOfActiveThreads.incrementAndGet();
        } catch (IOException | InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }
}
