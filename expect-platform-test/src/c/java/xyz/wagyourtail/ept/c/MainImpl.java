package xyz.wagyourtail.ept.c;

public class MainImpl {

    public static String platformTest2(String name) {
        return "Goodbye c! " + name;
    }

    public static void environmentCheck(Environment env) {
        if (env.type() == Environment.EnvType.CLIENT) {
            System.out.println("Client only");
        } else if (env.type() == Environment.EnvType.SERVER) {
            System.out.println("Server only");
        } else {
            System.out.println("Combined");
        }
    }

}
