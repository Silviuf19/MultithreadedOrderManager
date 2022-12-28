public class Tema2 {
    static String folder_input;
    static int P;
    public static void main(String[] args) {
        if(Helpers.isNumeric(args[2])) {
            System.out.println("The format is 'java Tema2 <folder_input> <nr_max_threads>'\nnr_max_threads has to be a number");
        }
        folder_input = args[1];
        P = Integer.parseInt(args[2]);

        System.out.println(folder_input + " " + P);

    }
}
