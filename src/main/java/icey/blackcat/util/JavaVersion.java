package icey.blackcat.util;

import java.util.Arrays;

public class JavaVersion {

    public int major;
    public int minor;
    public int update;

    public static JavaVersion getLocalVersion(){
        String property = System.getProperties().getProperty("java.version");
        if(property == null){
            return null;
        }
        JavaVersion version = new JavaVersion();
        String parts[] = property.split("\\.|_|-");
        int start = "1".equals(parts[0]) ? 1 : 0;
        version.major = Integer.parseInt(parts[start + 0]);
        version.minor = Integer.parseInt(parts[start + 1]);
        version.update = Integer.parseInt(parts[start + 2]);
        return version;
    }

    public static boolean isAtLeast(int major){
        JavaVersion version = JavaVersion.getLocalVersion();
        return version != null && version.major >= major;
    }
}
