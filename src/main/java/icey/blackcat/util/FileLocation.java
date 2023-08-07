package icey.blackcat.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: FileLocation
 * Description:
 * date: 2023/7/6 15:22
 *
 * @author Icey
 * @since JDK 1.8
 */
public class FileLocation {

    private Set<String> cps = new HashSet<>();
    private boolean isDir = false;
    private boolean isJar = false;
    private boolean isClass = false;
    private Path path;

    public FileLocation(Path path){
        this.path = path;
        this.isDir = Files.isDirectory(path);
        if(!isDir){
            String filepath = path.toString();
            if(filepath.endsWith(".jar")){
                this.isJar = true;
            } else if (filepath.endsWith(".class")) {
                this.isClass = true;
            }
        }
    }

    public String getFileName() { return this.path.getFileName().toString(); }

    public Set<String> resolve(){
        Path targetPath = null;
        if(isJar){
            // 拆包
            try {
                targetPath = FileUtils.unpack(path, getFileName()); // 临时目录的路径
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isDir) {
            targetPath = path;
        }

        if(targetPath == null){
            cps.add(path.toString());
        }else {
            try {
                Map<String, Set<String>> targets = new HashMap<>();
                targets.put("jar", new HashSet<>());
                targets.put("classes", new HashSet<>());
                FileUtils.findAllTarget(targetPath, targets);

                if(isDir){
                    Set<String> allUnpackedFiles = new HashSet<>();
                    allUnpackedFiles.addAll(targets.get("jar"));
                    for(String unpacked : allUnpackedFiles){
                        FileLocation location = new FileLocation(Paths.get(unpacked));
                        cps.addAll(location.resolve());
                    }
                }else {
                    cps.addAll(targets.get("jar"));
                }

                // 处理class
                Path tmpPath = targetPath;
                Set<String> remainedClasses = new HashSet<>();
                Set<String> allClasses = targets.get("classes");
                for(String cls : allClasses){
                    if(cls.contains("BOOT-INF/classes/") || cls.contains("WEB-INF/classes/")) continue;
                    remainedClasses.add(cls);
                }
                if(remainedClasses.size() > 0){
                    Path tmpClassesPath = tmpPath.resolve("classes_" + RandomStringUtils.randomAlphanumeric(3));
                    FileUtils.copyAll(remainedClasses, tmpClassesPath, targetPath.toString());
                    cps.add(tmpClassesPath.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cps;
    }
}
