package mandrik.security.notepad.controller.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nick Mandrik
 */
public class RestUtils {

    public static Map RESULT_ERROR = mapOf("result", "error");
    public static Map RESULT_SUCCESS = mapOf("result", "success");

    public static Map mapOf(Object... args){
        Map result = new HashMap();
        for ( int i = 0; i < args.length - 1; i+=2){
            result.put(args[i], args[i + 1]);
        }
        return result;
    }
}
