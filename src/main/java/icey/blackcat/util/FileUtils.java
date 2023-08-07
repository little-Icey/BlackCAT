package icey.blackcat.util;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ClassName: FileUtils
 * Description:
 * date: 2023/7/6 10:29
 *
 * @author Icey
 * @since JDK 17
 */
@Slf4j
public class FileUtils {
    public static boolean isFatJar(String filepath){
        Path path = Paths.get(filepath);
        if(Files.exists(path)) {
            try(JarFile jarFile = new JarFile(path.toFile())){
                return jarFile.getEntry("WEB-INF") != null
                        || jarFile.getEntry("BOOT-INF") != null
                        || jarFile.getEntry("lib") != null;
            }catch (Exception ignore){}
        }
        return false;
    }

    public static void findAllTarget(Path path, Map<String, Set<String>> map) throws IOException{
        if(path == null) return;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                String file = path.toAbsolutePath().toString();
                if(file.endsWith(".jar")){
                    map.get("jar").add(file);
                } else if (file.endsWith(".class")) {
                    map.get("classes").add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Path unpack(Path path, String filename) throws IOException{
        if(Files.exists(path)){
            Path output = registerTempDirectory(filename + RandomStringUtils.randomAlphabetic(3));
            extract(path, output);
            return output;
        }
        return null;
    }

    public static void extract(Path jarPath, Path tmpDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            Enumeration<? extends ZipEntry> iterator = zipFile.entries();
            ZipEntry zipEntry;
            while (iterator.hasMoreElements()) {
                zipEntry = iterator.nextElement();
//                System.out.println("测试输出：" + zipEntry.getName());
                Path fullPath = tmpDir.resolve(zipEntry.getName());
                if (!zipEntry.isDirectory()
                        && (zipEntry.getName().endsWith(".class")
                        || zipEntry.getName().endsWith(".jar")
                        || zipEntry.getName().endsWith(".jsp")
                        || zipEntry.getName().endsWith(".jspx")
                        || zipEntry.getName().endsWith(".tld")
                        || zipEntry.getName().endsWith(".jmod")
                )) {
                    Path dirName = fullPath.getParent();
                    if (dirName == null) {
                        throw new IllegalStateException("Parent of item is outside temp directory.");
                    }
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }

                    Files.copy(zipFile.getInputStream(zipEntry), fullPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static Path registerTempDirectory(String directory) throws IOException{
        final Path tmpDir = Files.createTempDirectory("1cey_" + directory);
        // 删除临时目录
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteDirectory(tmpDir);
            }catch (IOException e){
                log.error("Failed to clean tmp dir: " + tmpDir.toString() + "   " + e);
            }
        }));

        return tmpDir;
    }

    public static void deleteDirectory(Path root) throws IOException{
        Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copy(String source, Path target) throws IOException {
        Path dirPath = target.getParent();
        if(!Files.exists(dirPath)){
            Files.createDirectories(dirPath);
        }
        Files.copy(Paths.get(source), target, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copyAll(Set<String> sources, Path target, String basePath) throws IOException {
        if (Files.notExists(target)) {
            Files.createDirectories(target);
        }
        int len = basePath.length();
        for (String source : sources) {
            if (source.startsWith(basePath)) {
                String sub = source.substring(len);
                if (sub.startsWith(File.separator)) {
                    sub = sub.substring(1);
                }
                Path path = target.resolve(sub);
                copy(source, path);
            }
        }
    }

    public static boolean fileExists(String path){
        if(path == null) return false;
        File file = new File(path);
        return file.exists();
    }

    public static void delete(String filePath){
        File file = new File(filePath);
        if(file.exists()) file.delete();
    }

    public static String getFileMD5(String filePath){
        return getFileMD5(new File(filePath));
    }

    private static String getFileMD5(File file){
        try {
            return com.google.common.io.Files.hash(file, Hashing.md5()).toString();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }
}
