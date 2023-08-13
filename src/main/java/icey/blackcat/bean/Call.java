package icey.blackcat.bean;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import soot.Unit;
import soot.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ClassName: Call
 * Description:
 * date: 2023/8/13 17:10
 *
 * @author Icey
 * @since JDK 1.8
 */
@Getter
@Setter
public class Call {

    private String id;
    private MethodReference source;
    private MethodReference target;

    // 保存调用现场
    private int lineNum = 0;
    private String invokeType;

    // 记录真实调用类型，由于Java多态可能导致类型错误
    private String realCallType;

    private transient Value base;
    private transient List<Value> params = new ArrayList<>(); // 调用点的形参
    private transient Unit unit; // 代码块

    private static Call newInstance(MethodReference source, MethodReference target){
        Call call = new Call();
        call.setId(UUID.randomUUID().toString());
        call.setSource(source);
        call.setTarget(target);
        return call;
    }
}
