package icey.blackcat.collector;

import icey.blackcat.util.FileLocation;
import icey.blackcat.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: FileCollector
 * Description:
 * date: 2023/7/6 15:22
 *
 * @author Icey
 * @since JDK 1.8
 */
public class FileCollector {

    public Map<String, String> collect(String targetPath){
        Map<String, String> allTargets = new HashMap<>();
        Path path = Paths.get(targetPath).toAbsolutePath();
        if(Files.notExists(path)){
            throw new IllegalArgumentException("Invalid target path: " + path);
        }
        FileLocation fileLocation = new FileLocation(path);
        Set<String> cps = fileLocation.resolve();
        for(String cp : cps){
            Path tmp = Paths.get(cp);
            if(Files.isDirectory(tmp)){
                allTargets.put(cp, cp);
            }else{
                String  filename = tmp.getFileName().toString();
                String fileMD5 = FileUtils.getFileMD5(cp);
                // 暂时不加入全局配置
                allTargets.put(fileMD5, cp);
            }
        }
        return allTargets;
    }
}
