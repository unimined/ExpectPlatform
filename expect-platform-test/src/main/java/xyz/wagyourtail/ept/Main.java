package xyz.wagyourtail.ept;

import xyz.wagyourtail.unimined.expect.annotation.Environment;
import xyz.wagyourtail.unimined.expect.annotation.ExpectPlatform;
import xyz.wagyourtail.unimined.expect.annotation.PlatformOnly;
import xyz.wagyourtail.unimined.expect.Target;

public class Main {

    public static void main(String[] args) {
        System.out.println("current platform: " + Target.getCurrentTarget());

        Target.getCurrentTarget();
        Target.getCurrentTarget();

        System.out.println(platformTest("test"));
        System.out.println(platformTest2("test"));

        try {
            Main.class.getDeclaredMethod("platformOnlyTest");
            System.out.println("platformOnlyTest exists");

            environmentCheck(Main.class.getDeclaredMethod("clientTest").getAnnotation(Environment.class));
        } catch (NoSuchMethodException e) {
            System.out.println("platformOnlyTest does not exist");
        }
    }

    @ExpectPlatform(
        platforms = @ExpectPlatform.Platform(
            name = "c",
            target = "xyz/wagyourtail/ept/c/NotMain"
        )
    )
    public static String platformTest(String name) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static String platformTest2(String name) {
        throw new AssertionError();
    }

    @PlatformOnly({"a", "b"})
    public static void platformOnlyTest() {
    }

    @Environment(Environment.EnvType.COMBINED)
    public static void clientTest() {
    }

    @ExpectPlatform
    public static void environmentCheck(Environment env) {
        throw new AssertionError();
    }
}
