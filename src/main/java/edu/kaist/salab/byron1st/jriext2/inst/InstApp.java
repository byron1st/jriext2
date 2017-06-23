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
 * Created by util on 2017. 6. 20..
 */
public class InstApp implements Symbols {
    public static InstApp getInstance() {
        return instApp;
    }

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

    /**
     * Execution trace type들에 따라 클래스 파일들을 instrument 한다.
     * @param targetClassPath 재구축 대상 시스템의 class path
     * @param ettypeList instrument 대상을 정의한 execution trace type 리스트
     * @throws ClassReaderNotConstructedException ClassReader를 생성하는데 실패함
     * @throws WritingInstrumentedClassFailedException Instrumented 된 클래스를 캐시 폴더에 쓰는데 실패함
     * @throws CopyingNotInstClassesFailedException targetClassPath의 클래스 파일들을 캐시 폴더로 복사하는데 실패함
     */
    public void instrument(Path targetClassPath, ArrayList<ETType> ettypeList) throws ClassReaderNotConstructedException, WritingInstrumentedClassFailedException, CopyingNotInstClassesFailedException, CopyJRiExtLoggerClassFileFailedException {
        // Instrument가 먼저 진행되기 때문에,
        // instrument 된 클래스는 이미 캐시 폴더에 print 되어 있다.
        // 이때, targetClassPath의 클래스들을 그대로 복사하면
        // instrument 전 클래스 파일을 캐시 폴더에 덮어쓰기 때문에,
        // instrument 후 이 hashMap에 저장하여, instrument 된 클래스는 캐시 폴더로 또 복사하지 않게 체크한다.
        HashSet<String> isAlreadyCopied = new HashSet<>();

        // 각 Execution trace type들을 하나하나 순회하며 instrument 작업을 수행한다.
        for (ETType ettype: ettypeList) {

            // 우선 적절한 ClassReader를 불러온다.
            ClassReader classReader;
            try {
                ImmutablePair<ClassReader, HashSet<String>> returnedPair =
                        getClassReader(targetClassPath, ettype.getClassName());
                classReader = returnedPair.getLeft();
                isAlreadyCopied.addAll(returnedPair.getRight());
            } catch (IOException e) {
                throw new ClassReaderNotConstructedException("ClassReader object cannot be constructed.", e);
            }

            // Instrument를 진행하고, instrumented 된 결과물은 ClassWriter 객체에 저장된다.
            ClassWriter classWriter = instrumentClass(classReader, ettype);

            // ClassWriter 객체에 저장된 instrumented 결과물을 캐시 폴더 내에 .class 파일로 쓴다.
            try {
                printClass(ettype.getClassName(), classWriter);
            } catch (IOException e) {
                throw new WritingInstrumentedClassFailedException("Writing the instrumented class to the cache directory has been failed.", e);
            }
        }

        try {
            copyClassFile(JRIEXTLOGGER_PATH);
        } catch (IOException e) {
            throw new CopyJRiExtLoggerClassFileFailedException("Copying JRiExtLogger class file has been failed.", e);
        }

        // targetClassPath에 있는 클래스 파일들 중,
        // instrumented 되지 않은 파일들을 캐시 폴더로 카피한다.
        try {
            copyNotInstrumentedClassesToCacheDir(targetClassPath, isAlreadyCopied);
        } catch (IOException e) {
            throw new CopyingNotInstClassesFailedException("Copying classes that are not instrumented is failed.", e);
        }
    }

    /**
     * 생성자 클래스. 캐시 폴더가 있다면, 캐시 폴더를 삭제한다.
     */
    private InstApp() {
        deleteCacheFolderIfExists();
    }

    /**
     * targetClassPath의 클래스 파일들 중
     * instrumented 되지 않은 클래스 파일들을 캐시 폴더로 복사한다.
     * @param targetClassPath 재구축 대상 시스템의 class path
     * @param isAlreadyCopied targetClassPath의 클래스 파일들 중 instrumented 된 클래스들 리스트
     * @throws IOException 파일 복사에 실패함
     */
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

    /**
     * ClassReader 객체를 생성한다.
     * 반환되는 isAlreadyCopied 객체에는,
     * 만약 targetClassPath의 클래스가 instrumented 되었을 경우,
     * 이 클래스의 원래 path(캐시 폴더 내의 path가 아니라)를 포함한다.
     * 아닐 경우, size가 0인 HashSet이 반환된다.
     * @param targetClassPath 재구축 대상 시스템의 class path
     * @param className ClassReader 객체를 생성할 클래스 이름
     * @return 생성된 ClassReader 객체와 isAlreadyCopied HashSet 객체.
     * @throws IOException
     */
    private ImmutablePair<ClassReader, HashSet<String>> getClassReader(Path targetClassPath, String className) throws IOException {
        HashSet<String> isAlreadyCopied = new HashSet<>();
        ClassReader classReader;

        Path pathInCache = CACHE_ROOT.resolve(className + ".class");
        if(Files.exists(pathInCache)) {
            // 일단 캐시 폴더를 먼저 수색한다.
            // 캐시 폴더에 있다는 의미는, 해당 클래스가 이미 다른 부분에서 instrumented 되었음을 의미한다.
            // 이 경우, 먼저 instrumented 된 부분을 잃지 않기 위해서,
            // 캐시 폴더 내의 클래스를 로드하여 ClassReader 객체를 만든다.
            classReader = new ClassReader(new FileInputStream(pathInCache.toFile()));
        } else {
            // 캐시 폴더에 없다면,
            // targetClassPath를 수색한다.
            Path pathInClasspath = targetClassPath.resolve(className + ".class");
            if(Files.exists(pathInClasspath)) {
                // 만약 targetClassPath에 해당 클래스가 존재한다면,
                // instrumented 될 것이므로, 향후 중복 카피되지 않게 하기 위해,
                // isAlreadyCopied 리스트에 넣어준다.
                isAlreadyCopied.add(pathInClasspath.toString());
                classReader = new ClassReader(new FileInputStream(pathInClasspath.toFile()));
            } else {
                // targetClassPath에도 없다면,
                // 이는 Java SE 클래스이므로, 그냥 클래스 이름으로 로드 한다.
                // 여기서도 존재하지 않으면,
                // 이는 3rd party 라이브러리의 클래스라는 것을 의미하며,
                // 이는 아직 지원하지 않는다.
                classReader = new ClassReader(className);
            } //TODO: 3rd party library 클래스들 지원.
        }

        return new ImmutablePair<>(classReader, isAlreadyCopied);
    }

    /**
     * Visitor Pattern으로 구현된 ASM5의 방식을 따라,
     * JRiExt2ClassVisitor를 생성하여 instrument를 진행한다.
     * @param classReader Instrument 대상 클래스의 ClassReader 객체
     * @param ettype Execution trace type 객체
     * @return instrumented 된 클래스 정보를 기록한 ClassWriter 객체
     */
    private ClassWriter instrumentClass(ClassReader classReader, ETType ettype) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        JRiExt2ClassVisitor classVisitor = new JRiExt2ClassVisitor(classWriter, ettype);
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);

        return classWriter;
    }

    private void copyClassFile(Path file) throws IOException {
        // 카피는 ClassReader를 이용해서,
        // 있는 그대로 읽은 후, 변경없이 있는 그대로 출력하는 방식을 쓴다.
        ClassReader reader = new ClassReader(new FileInputStream(file.toFile()));
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(writer, ClassReader.SKIP_FRAMES);

        printClass(reader.getClassName(), writer);
    }

    private void printClass(String className, ClassWriter classWriter) throws IOException {
        String fileName;
        String dirName = CACHE_ROOT.toString();

        // Package 구조에 맞게 폴더 구조를 구축한다.
        int del = className.lastIndexOf('/');
        if(del != -1) {
            fileName = className.substring(className.lastIndexOf('/') + 1);
            dirName += File.separator + className.substring(0, className.lastIndexOf('/'));
        } else
            fileName = className;
        Files.createDirectories(Paths.get(dirName));

        // .class를 붙여서 클래스 파일을 만든다.
        Path path = Paths.get(dirName, fileName + ".class");

        // 최종적으로 클래스 파일을 디스크에 쓴다.
        Files.write(path, classWriter.toByteArray());
    }
}
