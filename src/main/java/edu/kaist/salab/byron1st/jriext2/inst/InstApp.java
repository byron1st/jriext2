package edu.kaist.salab.byron1st.jriext2.inst;

import edu.kaist.salab.byron1st.jriext2.ettype.ETType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by byron1st on 2017. 6. 20..
 */
public class InstApp {
    public static InstApp getInstance() {
        return instApp;
    }

    private static final String DEFAULT_DIR_NAME = System.getProperty("user.dir") + File.separator + "jriext_userdata";
    private static final Path CACHE_ROOT = Paths.get(DEFAULT_DIR_NAME + File.separator + "cache");
    private static InstApp instApp = new InstApp();

    private static void deleteCacheFolderIfExists() {
        if(Files.exists(CACHE_ROOT)) {
            try {
                Files.walkFileTree(CACHE_ROOT, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void instrument(Path targetClassPath, ArrayList<ETType> ettypeList) throws ClassReaderNotConstructedException, WritingInstrumentedClassFailedException, CopyingNotInstClassesFailedException {
        HashSet<String> isAlreadyCopied = new HashSet<>();

        for (ETType ettype: ettypeList) {
            ImmutablePair<ClassReader, HashSet<String>> returnedPair;
            ClassReader classReader = null;
            try {
                returnedPair = getClassReader(targetClassPath, ettype.getClassName());
                classReader = returnedPair.getLeft();
                isAlreadyCopied.addAll(returnedPair.getRight());
            } catch (IOException e) {
                throw new ClassReaderNotConstructedException("ClassReader object cannot be constructed.", e);
            }

            ClassWriter classWriter = instrumentClass(classReader, ettype);

            try {
                printClass(ettype.getClassName(), classWriter);
            } catch (IOException e) {
                throw new WritingInstrumentedClassFailedException("Writing the instrumented class to the cache directory has been failed.", e);
            }
        }

        try {
            copyNotInstrumentedClassesToCacheDir(targetClassPath, isAlreadyCopied);
        } catch (IOException e) {
            throw new CopyingNotInstClassesFailedException("Copying classes that are not instrumented is failed.", e);
        }
    }

    private void copyNotInstrumentedClassesToCacheDir(Path targetClassPath, HashSet<String> isAlreadyCopied) throws IOException {
        Files.walkFileTree(targetClassPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(!isAlreadyCopied.contains(file.toString()) && file.toString().endsWith(".class")) {
                    copyClassFile(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private InstApp() {
        deleteCacheFolderIfExists();
    }

    private ImmutablePair<ClassReader, HashSet<String>> getClassReader(Path targetClassPath, String className) throws IOException {
        HashSet<String> isAlreadyCopied = new HashSet<>();
        ClassReader classReader;

        Path pathInCache = CACHE_ROOT.resolve(className + ".class");
        if(Files.exists(pathInCache)) {
            classReader = new ClassReader(new FileInputStream(pathInCache.toFile()));
        } else {
            Path pathInClasspath = targetClassPath.resolve(className + ".class");
            if(Files.exists(pathInClasspath)) {
                isAlreadyCopied.add(pathInClasspath.toString());
                classReader = new ClassReader(new FileInputStream(pathInClasspath.toFile()));
            } else {
                classReader = new ClassReader(className);
            }
        }

        return new ImmutablePair<>(classReader, isAlreadyCopied);
    }

    private ClassWriter instrumentClass(ClassReader classReader, ETType ettype) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        JRiExt2ClassVisitor classVisitor = new JRiExt2ClassVisitor(classWriter, ettype);
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);

        return classWriter;
    }

    private void printClass(String className, ClassWriter classWriter) throws IOException {
        String fileName;
        String dirName = CACHE_ROOT.toString();
        int del = className.lastIndexOf('/');
        if(del != -1) {
            fileName = className.substring(className.lastIndexOf('/') + 1);
            dirName += File.separator + className.substring(0, className.lastIndexOf('/'));
        } else
            fileName = className;
        Files.createDirectories(Paths.get(dirName));
        Path path = Paths.get(dirName, fileName + ".class");
        Files.write(path, classWriter.toByteArray());
    }

    private void copyClassFile(Path file) throws IOException {
        ClassReader reader = new ClassReader(new FileInputStream(file.toFile()));
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(writer, ClassReader.SKIP_FRAMES);

        printClass(reader.getClassName(), writer);
    }
}
