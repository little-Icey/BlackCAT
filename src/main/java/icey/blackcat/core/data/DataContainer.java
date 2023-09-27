package icey.blackcat.core.data;

import icey.blackcat.bean.Call;
import icey.blackcat.bean.ClassReference;
import icey.blackcat.bean.Has;
import icey.blackcat.bean.MethodReference;
import icey.blackcat.collector.ClassInfoCollector;
import icey.blackcat.core.scanner.ClassInfoScanner;
import org.springframework.beans.factory.annotation.Autowired;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.jimple.MulExpr;

import java.util.*;

public class DataContainer {

    // 先不将信息持久化，只保存在内存中

    private Map<String, ClassReference> savedClassRefs = Collections.synchronizedMap(new HashMap<>());

    private Map<String, MethodReference> savedMethodRefs = Collections.synchronizedMap(new HashMap<>());

    private Set<Call> savedCallNodes = Collections.synchronizedSet(new HashSet<>());
    private Set<Has> savedHasNodes = Collections.synchronizedSet(new HashSet<>()); // 后续要删掉，不需要Has边

    /**
     * 将程序语义节点保存到内存
     */
    public <T> void store(T ref){
        if(ref == null) return;

        if(ref instanceof ClassReference){
            ClassReference classRef = (ClassReference) ref;
            savedClassRefs.put(classRef.getName(), classRef);
        } else if (ref instanceof MethodReference) {
            MethodReference methodRef = (MethodReference) ref;
            savedMethodRefs.put(methodRef.getSubSignature(), methodRef);
        } else if (ref instanceof Call) {
            savedCallNodes.add((Call) ref);
        } else if (ref instanceof Has) {
            savedHasNodes.add((Has) ref);
        }
    }

    /**
     * 通过类名查找class节点
     */
    public ClassReference getClassRefByName(String name){
        ClassReference ref = savedClassRefs.getOrDefault(name, null);
        return ref;
    }

    /**
     * 通过subsignature和所属类 获取指定method节点
     * 注：函数签名带有类名，子签名没有类名
     */
    public MethodReference getMethodRefBySubSignature(String classname, String subSignature){
        String signature = String.format("<%s: %s>", subSignature.replace("'", ""));
        MethodReference ref = savedMethodRefs.getOrDefault(signature, null);
        return ref;
    }

    /**
     * 通过完整签名查找指定的method节点
     */
    public MethodReference getMethodRefBySignature(String signature){
        MethodReference ref = savedMethodRefs.getOrDefault(signature, null);
        return ref;
    }

    /**
     * overloading
     */
    public MethodReference getMethodRefBySignature(SootMethodRef sootMethodRef){
        SootClass cls = sootMethodRef.getDeclaringClass();
        String subSignature = sootMethodRef.getSubSignature().toString();
        MethodReference target = getMethodRefBySubSignature(cls.getName(), subSignature);
        return target != null? target : getFirstMethodRefFromFatherNodes(cls, subSignature);
    }


    /**
     * 广度优先搜素
     */
    public MethodReference getFirstMethodRefFromFatherNodes(SootClass cls, String subSignature){
        MethodReference target = null;

        if(cls.hasSuperclass()){
            SootClass superCls = cls.getSuperclass();
            target = getTargetMethodRef(superCls, subSignature);
            if(target != null) return target;
        }

        if(cls.getInterfaceCount() > 0){
            for(SootClass intFace : cls.getInterfaces()){
                target = getTargetMethodRef(cls, subSignature);
                if(target != null) return target;
            }
        }
        return null;
    }

    private MethodReference getTargetMethodRef(SootClass cls, String subSignature){
        MethodReference target = getMethodRefBySubSignature(cls.getName(), subSignature);
        if(target == null){
            target = getFirstMethodRefFromFatherNodes(cls, subSignature);
        }
        return target;
    }


    /*
    * 从内存中找一个方法，没有的话就新建一个
    * 后续需要添加创建新Alias边
    * */
    public MethodReference getOrAddMethodRef(SootMethodRef sootMethodRef, SootMethod method){
        MethodReference methodRef = getMethodRefBySignature(sootMethodRef);

        if(methodRef == null){
            SootClass cls = sootMethodRef.getDeclaringClass();
            ClassReference classRef = getClassRefByName(cls.getName());
            if(classRef == null){
                methodRef = MethodReference.newInstance(classRef.getName(), method);
                // 如果没有的话，需要把一个新的Has关系加入到ClassReference中
                Has has = Has.newInstance(classRef, methodRef);
                if(!classRef.getHasEdge().contains(has)){
                    classRef.getHasEdge().add(has);
                    store(has);
                    // classinfoscanner需要创建一条新的别名边
                }
                store(methodRef);
            }
        }

        return methodRef;
    }


}
