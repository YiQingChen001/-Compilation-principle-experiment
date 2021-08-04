public class TestG {
    public static void main(String[] args) {
        String str="F→(E)|i";
        String[] split = str.split("\\|");
        for (String s : split) {
            System.out.println(s);
        }
    }
}
