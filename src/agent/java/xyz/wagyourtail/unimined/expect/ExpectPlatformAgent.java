package xyz.wagyourtail.unimined.expect;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class ExpectPlatformAgent {
    private static final String EXPECT_PLATFORM = "expect.platform";
    private static final String REMAP = "expect.remap";
    private static final String platform = System.getProperty(EXPECT_PLATFORM);
    private static final String remap = System.getProperty(REMAP);

    private static final TransformPlatform transformPlatform = new TransformPlatform(platform, remap);

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[ExpectPlatformAgent] Platform: " + platform);
        System.out.println("[ExpectPlatformAgent] Remap: " + transformPlatform.getRemap());
        if (platform == null) {
            throw new IllegalStateException("-D" + EXPECT_PLATFORM + " not set");
        }
        inst.addTransformer(new ExpectPlatformTransformer(), inst.isRetransformClassesSupported());
    }

    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }

    public static class ExpectPlatformTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            classNode = transformPlatform.transform(classNode);

            ClassWriter writer = new ClassWriter(reader, 0);
            classNode.accept(writer);

            return writer.toByteArray();
        }

    }

}
