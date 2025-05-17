package xyz.wagyourtail.unimined.expect;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static xyz.wagyourtail.unimined.expect.TransformPlatform.PROPERTY_PLATFORM;
import static xyz.wagyourtail.unimined.expect.TransformPlatform.PROPERTY_REMAP;


public class ExpectPlatformAgent {
    private static final String platform = System.getProperty(PROPERTY_PLATFORM);
    private static final String remap = System.getProperty(PROPERTY_REMAP);

    static {
        if (platform == null) {
            throw new IllegalStateException("-D" + PROPERTY_PLATFORM + " not set");
        }

        if (remap == null) {
            throw new IllegalStateException("-D" + PROPERTY_REMAP + " not set");
        }
    }

    private static final TransformPlatform transformPlatform = new TransformPlatform(platform, remap, false);

    public static void premain(String args, Instrumentation inst) {
        if(!inst.isRetransformClassesSupported()) {
            System.out.println("[ExpectPlatformAgent] ur instrumentation is bad lol");
        }

        System.out.println("[ExpectPlatformAgent] Platform: " + platform);
        System.out.println("[ExpectPlatformAgent] Remap: " + transformPlatform.getRemap());

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
