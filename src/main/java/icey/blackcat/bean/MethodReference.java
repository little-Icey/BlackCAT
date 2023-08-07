package icey.blackcat.bean;

import com.google.common.hash.Hashing;
import icey.blackcat.util.SemanticHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import soot.SootClass;
import soot.SootMethod;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class MethodReference {


    private String id;

    private String name;
    private String name0;
    private String signature;
    private String subSignature;
    private String returnType;
    private int modifiers;
    private String className;
    private int paramSize;
    private String vul; // 暂时还用不着
    private transient int callCounter = 0;

    private boolean isSink = false; // 污点分析相关，暂不
    private boolean isSource = false; // 污点分析相关，暂不
    private boolean isStatic = false;
    private boolean isPublic = false;
    private boolean hasParams = false;
    private boolean hasDefaultConstructor = false;
    private boolean isIgnore = false; // 与规则设置相关，是否是之前分析过的target
    private boolean isSerializable = false;
    private boolean isAbstract = false;
    private boolean isContainsSource = false;
    private boolean isEndpoint = false;
    private boolean isNettyEndpoint = false;
    private boolean isContainsOutOfMemOptions = false;
    private boolean isActionContainsSwap = false;
    private boolean isGetter = false;
    private boolean isSetter = false;
    private boolean isFromAbstractClass = false;
    private boolean isBodyParseError = false;

    private boolean isInitialed = false;

    private boolean isActionInitialed = false;

    // 后面还需加入ORM框架
    private Map<String, String> actions = new ConcurrentHashMap<>();

    // TODO 调用边的变量定义
//    private transient Set<Call> callEdge = new HashSet<>();

    private transient SootMethod sootMethod = null;

    public static MethodReference newInstance(String name, String signature){
        MethodReference methodRef = new MethodReference();
        String id = null;
        if(signature == null || signature.isEmpty()){
            id = Hashing.md5()
                    .hashString(UUID.randomUUID().toString(), StandardCharsets.UTF_8)
                    .toString();
        }else {
            signature = signature.replace("'", "");
            id = Hashing.md5()
                    .hashString(signature, StandardCharsets.UTF_8)
                    .toString();
        }
        methodRef.setName(name);
        methodRef.setId(id);
        methodRef.setSignature(signature);
        return methodRef;
    }

    public static MethodReference newInstance(String className, SootMethod method){
        MethodReference methodRef = newInstance(method.getName(), method.getSignature());
        methodRef.setClassName(className);
        methodRef.setName0(String.format("%s.%s", className, method.getName()));
        methodRef.setModifiers(method.getModifiers());
        methodRef.setPublic(method.isPublic());
        methodRef.setSubSignature(method.getSubSignature());
        methodRef.setStatic(method.isStatic());
        methodRef.setReturnType(method.getReturnType().toString());
        methodRef.setAbstract(method.isAbstract());
        if(method.getParameterCount() > 0){
            methodRef.setHasParams(true);
            methodRef.setParamSize(method.getParameterCount());
        }
        return methodRef;
    }

    public SootMethod getMethod(){
        if(this.sootMethod != null) return sootMethod;
        SootClass sc = SemanticHelper.getSootClass(className);
        if(!sc.isPhantom()){ // 如果是虚引用
            // TODO
            sootMethod = SemanticHelper.getMethod(sc, subSignature);
            return sootMethod;
        }
        return null;
    }

    public void setMethod(SootMethod method){
        if(sootMethod == null){
            sootMethod = method;
        }
    }

    public void addAction(String key, String value){
        actions.put(key, value);
    }

    public void addCallCounter(){
        callCounter++;
    }

    // TODO 重写equal和hashCode
}
