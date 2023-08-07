package icey.blackcat.util;

import jdk.jfr.internal.EventWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.JavaVersion;
import soot.*;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class SemanticHelper {

    private static List<String> ARRAY_TYPES = Arrays.asList(
            "java.util.List",
            "java.util.ArrayList",
            "java.util.Set",
            "java.util.HashTable",
            "java.util.HashSet",
            "java.util.Map",
            "java.util.HashMap"
    );

    public static String extractValueName(Object value){
        String name = value.toString();
        if(value instanceof Local){
            name = ((Local) value).getName();
        }else if(value instanceof InstanceFieldRef){
            SootField field = ((InstanceFieldRef) value).getField();
            if(field != null){
                name = field.getSignature();
            }
        }else if(value instanceof SootField){
            name = ((SootField) value).getSignature();
        } else if (value instanceof SootClass) {
            name = ((SootClass) value).getName();
        } else if (value instanceof ArrayRef) {
            Value base = ((ArrayRef) value).getBase();
            name = extractValueName(base);
        }
        return name;
    }

    public static boolean isArray(Type type){
        return type instanceof ArrayType;
    }

    public static boolean isCollection(Type type){
        String typeName = type.toString();
        return ARRAY_TYPES.contains(typeName);
    }

    public static boolean hasDefaultConstructor(SootClass cls){
        if(cls == null) return false;
        try {
            SootMethod method = cls.getMethod("void <init>()");
            return method != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static SootClass getSootClass(String cls){
        if(cls == null) return null;
        try {
            return Scene.v().getSootClass(cls);
        } catch (Exception e) {
            log.warn("Load class {} error", cls);
        }
        return null;
    }

    public static SootMethod getMethod(SootClass cls, String subSignature){
        try { // 找不到所需方法再沿着继承树向上找
            return cls.getMethod(subSignature);
        } catch (RuntimeException e) {
            SootMethod method = null;
            if(cls.hasSuperclass()){
                method = getMethod(cls.getSuperclass(), subSignature);
            }
            // TODO
            if(method == null && cls.getInterfaceCount() > 0){
                for(SootClass intface : cls.getInterfaces()){
                    method = getMethod(intface, subSignature);
                    if(method != null) break;
                }
            }
            return method;
        }
    }

    // 可能不会用到


}
