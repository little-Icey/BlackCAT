package icey.blackcat.bean;

import com.google.common.hash.Hashing;
import icey.blackcat.util.SemanticHelper;
import lombok.Data;
import soot.SootClass;

import java.beans.Transient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class ClassReference {

    private String id;
    private String name;
    private String superClass;

    private boolean isPhantom = false;
    private boolean isInterface = false;
    private boolean hasSuperClass = false;
    private boolean hasInterface = false;
    private boolean hasDefaultConstructor = false;
    private boolean isInitialed = false;
    private boolean isSerializable = false;  // 反序列化相关，暂时用不到
    private boolean isStrucsAction = false;  // 反序列化相关，暂时用不到
    private boolean isAbstract = false;

    private List<String> interfaces = new ArrayList<>();

    private List<String> childClassNames = new ArrayList<>();

    // @Transient // JPA注解，用于告知JPA的提供者不要持久化该属性，与transient关键字作用不同
    private transient List<Has> hasEdge = new ArrayList<>();

    public static ClassReference newInstance(String name){
        ClassReference classRef = new ClassReference();
        String id = Hashing.md5()
                .hashString(name, StandardCharsets.UTF_8)
                .toString();
        classRef.setId(id);
        classRef.setName(name);
        classRef.setInterfaces(new ArrayList<>());
        return classRef;
    }

    public static ClassReference newInstance(SootClass cls){
        ClassReference classRef = new ClassReference();
        classRef.setInterface(cls.isInterface());
        classRef.setHasDefaultConstructor(SemanticHelper.hasDefaultConstructor(cls));
        classRef.setAbstract(cls.isAbstract());
        // 检查父类信息
        if(cls.hasSuperclass()){
            classRef.setHasSuperClass(cls.hasSuperclass());
            classRef.setSuperClass(cls.getSuperclass().getName());
        }
        // 检查接口信息
        if(cls.getInterfaceCount() > 0){
            classRef.setHasInterface(true);
            for(SootClass intface : cls.getInterfaces()){
                classRef.getInterfaces().add(intface.getName());
            }
        }
        return classRef;
    }

    public void setName(String name){
        if(name.length() >= 0b11111111){
            this.name = name.substring(0, 254);
        }else{
            this.name = name;
        }
    }
}
