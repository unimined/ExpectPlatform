package xyz.wagyourtail.ept;

import xyz.wagyourtail.unimined.expect.annotation.ExpectPlatform;

public class Main {

    public static void main(String[] args) {
        System.out.println(platformTest("test"));
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

}
