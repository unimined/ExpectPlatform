package xyz.wagyourtail.unimined.expect;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class TransformPlatform {
    private static final int EXPECT_PLATFORM_ACCESS = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;

    private final String platformName;
    private final boolean stripAnnotations;
    private final Map<String, String> remap = new HashMap<>();

    public TransformPlatform(String platformName, String map, boolean stripAnnotations) {
        this.platformName = platformName;
        this.stripAnnotations = stripAnnotations;
        stringMapParser(map);
    }

    public TransformPlatform(String platformName, Map<String, String> map, boolean stripAnnotations) {
        this.platformName = platformName;
        this.stripAnnotations = stripAnnotations;
        remap.putAll(map);
    }

    public Map<String, String> getRemap() {
        return remap;
    }

    public void transform(Path inputRoot, Path outputRoot) throws IOException {
        try (Stream<Path> files = Files.walk(inputRoot)) {
            files.parallel().forEach(path -> {
                try {
                    if (Files.isDirectory(path)) return;
                    Path parent = path.getParent();
                    if (parent != null) {
                        Files.createDirectories(outputRoot.resolve(inputRoot.relativize(parent).toString()));
                    }
                    Path output = outputRoot.resolve(inputRoot.relativize(path).toString());

                    if (!path.toString().endsWith(".class")) {
                        Files.copy(path, output, StandardCopyOption.REPLACE_EXISTING);
                        return;
                    }
                    ClassReader reader = new ClassReader(Files.newInputStream(path));
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);

                    classNode = transform(classNode);

                    ClassWriter writer = new ClassWriter(reader, 0);
                    classNode.accept(writer);

                    Files.write(output, writer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        }
    }

    public ClassNode transform(ClassNode classNode) {
        Map<MethodNode, AnnotationNode> expectPlatform = new HashMap<>();
        Map<MethodNode, AnnotationNode> platformOnly = new HashMap<>();

        ClassNode target = new ClassNode();
        ClassRemapper remapper = new BetterClassRemapper(target, new SimpleRemapper(remap));
        classNode.accept(remapper);

        classNode = target;

        if (classNode.methods == null) return classNode;
        for (MethodNode method : classNode.methods) {
            if (method.invisibleAnnotations == null) continue;
            for (AnnotationNode annotation : method.invisibleAnnotations) {
                if (annotation.desc.equals("Lxyz/wagyourtail/unimined/expect/annotation/ExpectPlatform;")) {
                    expectPlatform.put(method, annotation);
                } else if (annotation.desc.equals("Lxyz/wagyourtail/unimined/expect/annotation/PlatformOnly;")) {
                    platformOnly.put(method, annotation);
                }
            }
        }

        for (Map.Entry<MethodNode, AnnotationNode> entry : expectPlatform.entrySet()) {
            expectPlatform(entry.getKey(), classNode, entry.getValue());
            if (stripAnnotations) {
                classNode.invisibleAnnotations.remove(entry.getValue());
            }
        }

        for (Map.Entry<MethodNode, AnnotationNode> entry : platformOnly.entrySet()) {
            platformOnly(entry.getKey(), classNode, entry.getValue());
            if (stripAnnotations) {
                classNode.invisibleAnnotations.remove(entry.getValue());
            }
        }

        getCurrentTarget(classNode);

        return classNode;
    }

    private void expectPlatform(MethodNode methodNode, ClassNode classNode, AnnotationNode annotationNode) {
        if ((methodNode.access & EXPECT_PLATFORM_ACCESS) != EXPECT_PLATFORM_ACCESS) {
            throw new RuntimeException("ExpectPlatform methods must be public and static");
        }


        String platformClass = null;
        List<AnnotationNode> platforms = getAnnotationValue(annotationNode, "platforms");
        if (platforms != null) {
            for (AnnotationNode platform : platforms) {
                String name = null;
                String target = null;
                for (int i = 0; i < platform.values.size(); i += 2) {
                    String key = (String) platform.values.get(i);
                    Object value = platform.values.get(i + 1);
                    if (key.equals("name")) {
                        name = (String) value;
                    } else if (key.equals("target")) {
                        target = (String) value;
                    }
                }
                if (platformName.equals(name) && target != null) {
                    platformClass = target;
                    break;
                }
            }
        }
        if (platformClass == null) {
            int lastSlash = classNode.name.lastIndexOf('/');
            String pkg = lastSlash == -1 ? "" : classNode.name.substring(0, lastSlash);
			String className = classNode.name.substring(lastSlash + 1);
            if (pkg.isEmpty()) {
                platformClass = platformName + "/" + className + "Impl";
            } else {
                platformClass = pkg + "/" + platformName + "/" + className + "Impl";
            }
        }

        methodNode.instructions.clear();
        Type type = Type.getMethodType(methodNode.desc);
        int stackIndex = 0;
        for (Type arg : type.getArgumentTypes()) {
            methodNode.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), stackIndex));
            stackIndex += arg.getSize();
        }

        methodNode.instructions.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                platformClass,
                methodNode.name,
                methodNode.desc,
                false
            )
        );

        methodNode.instructions.add(new InsnNode(type.getReturnType().getOpcode(Opcodes.IRETURN)));

        // recalculate proper maxStack and maxLocals manually so we don't have to recompute anything
        methodNode.maxStack = Math.max(type.getReturnType().getSize(), stackIndex);
        methodNode.maxLocals = stackIndex;
    }

    private void platformOnly(MethodNode methodNode, ClassNode classNode, AnnotationNode annotationNode) {
        List<String> platforms = getAnnotationValue(annotationNode, "value");

        if (platforms != null && !platforms.contains(platformName)) {
            classNode.methods.remove(methodNode);
        }
    }

    private void getCurrentTarget(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InsnList instructions = methodNode.instructions;
            if (instructions == null) continue;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                if (insnNode.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    if (methodInsnNode.owner.equals("xyz/wagyourtail/unimined/expect/Target") &&
                        methodInsnNode.name.equals("getCurrentTarget")
                    ) {
                        iterator.set(new LdcInsnNode(platformName));
                    }
                }
            }
        }
    }

    private void stringMapParser(String str) {
        if (str.isEmpty()) return;
        String[] split = str.split(";[=|];");
        for (int i = 0; i < split.length; i += 2) {
            remap.put(split[i], split[i + 1]);
        }
    }

    public static String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(";=;").append(entry.getValue()).append(";|;");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 3);
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T getAnnotationValue(AnnotationNode annotation, String key) {
        if(annotation.values == null) return null;
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (annotation.values.get(i).equals(key)) {
                return (T) annotation.values.get(i + 1);
            }
        }
        return null;
    }

}
