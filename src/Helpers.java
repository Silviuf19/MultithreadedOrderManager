import java.io.File;
public class Helpers {
    static String numberErrorMessage = "The format is 'java Tema2 <folder_input> <nr_max_threads>'\nnr_max_threads has to be a number";
    static String fileReadError = "The path to the current folder does not exist";
    static String panicThreadsError = "Valeu!";
    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
    public static boolean fileExists(String path) {
        try {
            File f = new File(path);
            return f.exists();
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
