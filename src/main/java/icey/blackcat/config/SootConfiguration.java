package icey.blackcat.config;

import lombok.extern.slf4j.Slf4j;
import soot.G;
import soot.options.Options;

@Slf4j
public class SootConfiguration {
    /**
     * Soot配置
     */
    public static void initSootOption(){
        G.reset();
        Options.v().set_verbose(false); // 是否打印详细信息
        Options.v().set_prepend_classpath(true); // 优先载入soot classpath
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true); // 记录文件行数
        Options.v().set_src_prec(Options.src_prec_class); // 优先处理class格式
        Options.v().set_whole_program(true);
        Options.v().set_oaat(true);
    }
}
