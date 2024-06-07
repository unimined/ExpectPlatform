package xyz.wagyourtail.ept.b;

public class MainImpl {

    public static String platformTest(String name) {
        return "Hello b! " + name;
    }

    public static String platformTest2(String name) {
        return "Goodbye b! " + name;
    }

    public static void environmentCheck(OnlyIn onlyIn) {
        if (onlyIn.env() == OnlyIn.Type.CLIENT) {
            System.out.println("Client only");
        } else if (onlyIn.env() == OnlyIn.Type.SERVER) {
            System.out.println("Server only");
        } else {
            System.out.println("Combined");
        }
    }
}
