package icey.blackcat.collector;

import icey.blackcat.bean.ClassReference;
import icey.blackcat.bean.Has;
import icey.blackcat.bean.MethodReference;
import org.springframework.scheduling.annotation.Async;
import soot.SootClass;
import soot.SootMethod;

import java.util.HashSet;
import java.util.Set;

public class ClassInfoCollector {

    public static void extractMethodInfo(SootMethod method,
                                         ClassReference ref,
                                         Set<String> relatedClassnames){
        String className = ref.getName();
        MethodReference methodRef = MethodReference.newInstance(className, method);

        // 污点分析部分代码，暂时用不着

        methodRef.setSink(false);
        methodRef.setIgnore(false);
        methodRef.setSource(false);
        methodRef.setGetter(isGetter(method));
        methodRef.setSetter(isSetter(method));
        methodRef.setSerializable(relatedClassnames.contains("java.io.Serializable"));
        methodRef.setAbstract(method.isAbstract());
        methodRef.setHasDefaultConstructor(ref.isHasDefaultConstructor());
        methodRef.setFromAbstractClass(ref.isAbstract());

        Has has = Has.newInstance(ref, methodRef);
        ref.getHasEdge().add(has);
    }

    public static boolean isGetter(SootMethod method){
        String methodName = method.getName();
        String returnType = method.getReturnType().toString();
        boolean noParameter = method.getParameterCount() == 0;
        boolean isPublic = method.isPublic();

        if(!noParameter || !isPublic) return false;
        if(methodName.startsWith("get") && methodName.length() > 3){
            return !"void".equals(returnType);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return "boolean".equals(returnType);
        }

        return false;
    }

    public static boolean isSetter(SootMethod method){
        String methodName = method.getName();
        String returnType = method.getReturnType().toString();
        boolean singleParameter = method.getParameterCount() == 1;
        boolean isPublic = method.isPublic();

        if(!isPublic || !singleParameter) return false;
        if(methodName.startsWith("set") && methodName.length() > 3){
            return "void".equals(returnType);
        }

        return false;
    }

    public static Set<String> getAllFatherNodes(SootClass cls){
        Set<String> nodes = new HashSet<>();
        if(cls.hasSuperclass() && !cls.getSuperclass().getName().equals("java.lang.Object")){
            nodes.add(cls.getSuperclass().getName());
            nodes.addAll(getAllFatherNodes(cls.getSuperclass()));
        }
        if(cls.getInterfaceCount() > 0){
            cls.getInterfaces().forEach(intface -> {
                nodes.add(intface.getName());
                nodes.addAll(getAllFatherNodes(intface));
            });
        }
        return nodes;
    }
}
