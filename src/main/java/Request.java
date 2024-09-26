import lombok.Data;

import java.util.TreeMap;

@Data
public class Request {
    private String method;
    private String path;
    private TreeMap<String, String> headers;
    private String body;
}
