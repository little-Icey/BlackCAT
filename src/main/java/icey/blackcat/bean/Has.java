package icey.blackcat.bean;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

@Setter
@Getter
public class Has {

    private String id;
    private ClassReference classRef;
    private MethodReference methodRef;

    public static Has newInstance(ClassReference classRef, MethodReference methodRef){
        Has has = new Has();
        has.setId(UUID.randomUUID().toString());
        has.setClassRef(classRef);
        has.setMethodRef(methodRef);
        return has;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Has has = (Has) o;
        return new EqualsBuilder().append(classRef.getName(), has.classRef.getName()).append(methodRef.getSignature(), has.methodRef.getSignature()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(classRef.getName()).append(methodRef.getSignature()).toHashCode();
    }
}
