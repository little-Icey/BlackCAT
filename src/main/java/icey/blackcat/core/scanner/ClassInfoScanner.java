package icey.blackcat.core.scanner;

import icey.blackcat.bean.ClassReference;
import icey.blackcat.collector.ClassInfoCollector;
import icey.blackcat.util.JavaVersion;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import soot.ModulePathSourceLocator;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
@Component
public class ClassInfoScanner {

    private ClassInfoCollector collector;

    public Map<String, ClassReference> loadAndExtract(List<String> targets){
//        Map<String, CompletableFuture<ClassReference>> results = new HashMap<>();
        Map<String, ClassReference> results = new HashMap<>();
        log.info("Start to collect {} targets' class information", targets.size());
        Map<String, List<String>> moduleClasses = null;
        if(JavaVersion.isAtLeast(9)){
            moduleClasses = ModulePathSourceLocator.v().getClassUnderModulePath("jrt:/");
        }
        for(final String path : targets){
            List<String> classes = getTargetClasses(path, moduleClasses);
            if(classes == null) continue;

            for(String cls : classes){
                try{ // TODO
                    SootClass theClass = Scene.v().loadClassAndSupport(cls);
                    if(theClass.isPhantomClass()){
                        // 由result收集类信息，相关数据结构还没写完，这里result将会把提取后的每个class保存
                        results.put(cls, collector.collect0(theClass));
                        theClass.setApplicationClass();
                    }
                }catch (Exception e){
                    log.error("Load Error: {}, Message: {}", cls, e.getMessage());
                }
            }
        }
        log.info("Total {} classes", results.size());
        return results;
    }

    public List<String> getTargetClasses(String filePath, Map<String, List<String>> moduleClasses){
        List<String> classes = null;
        Path path = Paths.get(filePath);
        if(Files.notExists(path)) return null;

        if(JavaVersion.isAtLeast(9) && moduleClasses != null){
            String filename = path.getFileName().toString();
            if(filename.endsWith(".jmod")){
                filename = filename.substring(0, filename.length() - 5);
            }
            classes = moduleClasses.get(filename);
        }

        if(classes == null){
            classes = SourceLocator.v().getClassesUnder(filePath); // Soot将类文件加载进行分析环境
        }

        return classes;
    }
}
