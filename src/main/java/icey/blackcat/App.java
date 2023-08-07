package icey.blackcat;

import icey.blackcat.core.Analyser;
import org.springframework.beans.factory.annotation.Autowired;

public class App {

    private static Analyser analyser = new Analyser();

    public static void main(String[] args) throws Exception{
        analyser.run();
    }
}
