package icey.blackcat.collector;

import icey.blackcat.bean.ClassReference;
import icey.blackcat.bean.Has;
import icey.blackcat.bean.MethodReference;
import icey.blackcat.core.data.DataContainer;
import org.springframework.scheduling.annotation.Async;
import soot.SootClass;
import soot.SootMethod;

import java.util.HashSet;
import java.util.Set;

public class ClassInfoCollector {

    private DataContainer dataContainer;
    // 先暂时不加DataContainer, 先写一个能跑的demo出来

    /*
    * 封装
    * */
    public ClassReference collect(SootClass cls){
        return collect0(cls, dataContainer);
    }

    public static ClassReference collect0(SootClass cls, DataContainer dataContainer){
        ClassReference classRef = ClassReference.newInstance(cls);
        Set<String> relatedClassnames = getAllFatherNodes(cls);
        // setSerializable
        // setStructsAction
        if(cls.getMethodCount() > 0){
            cls.getMethods().forEach(m -> extractMethodInfo(m, classRef, relatedClassnames, dataContainer));
        }
        return classRef;
    }


    /*
    * 将某个方法的相关信息保存到内存中，对于普通的调用图构建暂时没有什么作用
    * */
    public static void extractMethodInfo(SootMethod method,
                                         ClassReference ref,
                                         Set<String> relatedClassnames,
                                         DataContainer dataContainer){
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
        dataContainer.store(has);
        dataContainer.store(methodRef);
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
            cls.getInterfaces().forEach(intFace -> {
                nodes.add(intFace.getName());
                nodes.addAll(getAllFatherNodes(intFace));
            });
        }
        return nodes;
    }
}
