package icey.blackcat.core;

import lombok.extern.slf4j.Slf4j;
import icey.blackcat.collector.FileCollector;
import icey.blackcat.config.SootConfiguration;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Analyser {

    private static final String targetJar = "E:\\sqlite4java.jar";
    private FileCollector fileCollector = new FileCollector();

    public void run() throws IOException{
        log.info("Try to collect all targets");

        // 暂时不包括JDK依赖
        Map<String, String> cps = new HashMap<>();
        Map<String, String> targets = new HashMap<>();
        Map<String, String> files = fileCollector.collect(targetJar);
        cps.putAll(files);
        targets.putAll(files);

        cps.forEach((k, v) -> log.info(k + "----" + v));
        targets.forEach((k, v) -> log.info(k + "----" + v));
        runSootAnalysis(targets, new ArrayList<>(cps.values()));
    }

    public void runSootAnalysis(Map<String, String> targets, List<String> classpaths){
        try {
            SootConfiguration.initSootOption();
            // addBasicClasses()
            log.info("Load basic classes");
            Scene.v().loadBasicClasses();
            log.info("load dynamic classes");
            Scene.v().loadDynamicClasses();
            Scene.v().setSootClassPath(String.join(File.separator, new HashSet<>(classpaths)));
            List<String> realTargets = getTargets(targets);
            if(realTargets.isEmpty()){
                log.info("Nothing to analysis!");
                return;
            }
            Main.v().autoSetOptions();
            log.info("Targets {}, dependencies {}", realTargets.size(), 0);
            long start = System.nanoTime();
            /**
             * TODO 抽取类信息 classInfoScanner.run(realTargets);
             * 构建全量函数调用图
             */
            List<String> classes = SourceLocator.v().getClassesUnder(classpaths.get(0));
//            classes.forEach(log::info);
            for(String cls : classes) {
                SootClass theClass = Scene.v().loadClassAndSupport(cls);
                try {
                    log.info(theClass.getName());
                    log.info("method nums: {}\n", theClass.getMethods().size());
                }catch (Exception e){
                    log.error(e.toString());
                }
//                if(!theClass.isPhantom()){
//                    try {
//                        SootMethod method = theClass.getMethods().get(0);
//                        if(theClass.hasSuperclass() && !theClass.getSuperclass().getName().equals("java.lang.Object")){
//                            log.info("The class {} has fatherClass {}", theClass, theClass.getSuperclass());
//                        }
////                        log.info("Class: {}, one method: {}", theClass.getName(), method);
//                        theClass.setApplicationClass();
//                    } catch (IndexOutOfBoundsException e) {
//                        log.error("错误了", e.toString());
//                    }
//                }
            }
            log.info("Application count: {}", Scene.v().getApplicationClasses().size());
            long time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
            log.info("Total cost {} min {} seconds.", time / 60, time % 60);
        } catch (CompilationDeathException e) {
            if(e.getStatus() != CompilationDeathException.COMPILATION_SUCCEEDED){
                throw e;
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public List<String> getTargets(Map<String, String> targets){
        // 暂时不加入ruleContainer
        Set<String> stuff = new HashSet<>();
        /**
         * 如果被纳入分析，则将该目标纳入isIgnore
         */
        targets.forEach((filename, filepath) -> {
            stuff.add(filepath);
            // newIgnore.add(filename);
        });
        log.info("Total analyse {} targets.", stuff.size());
        Options.v().set_process_dir(new ArrayList<>(stuff));
        return new ArrayList<>(stuff);
    }
}
