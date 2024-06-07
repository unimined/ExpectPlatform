package xyz.wagyourtail.ept.a;

public class MainImpl {


    public static String platformTest(String name) {
        return "Hello a! " + name;
    }

    public static String platformTest2(String name) {
        return "Goodbye a! " + name;
    }

    public static void environmentCheck(Env env) {
        if (env.value() == Env.EnvType.CLIENT) {
            System.out.println("Client only");
        } else if (env.value() == Env.EnvType.SERVER) {
            System.out.println("Server only");
        } else {
            System.out.println("Joined");
        }
    }

}
