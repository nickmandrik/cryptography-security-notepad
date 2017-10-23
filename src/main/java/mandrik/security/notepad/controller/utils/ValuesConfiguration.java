package mandrik.security.notepad.controller.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Nick Mandrik
 */
@Component
public class ValuesConfiguration {

    @Value("${session.key.length}")
    private Integer sessionKeyLength;

    @Value("${storage.files.dir}")
    private String filesDir;

    @Value("${storage.files.encryptDir}")
    private String encryptFilesDir;


    public String getFilesDir() {
        return filesDir;
    }

    public void setFilesDir(String filesDir) {
        this.filesDir = filesDir;
    }

    public String getEncryptFilesDir() {
        return encryptFilesDir;
    }

    public void setEncryptFilesDir(String encryptFilesDir) {
        this.encryptFilesDir = encryptFilesDir;
    }

    public Integer getSessionKeyLength() {
        return sessionKeyLength;
    }

    public void setSessionKeyLength(Integer sessionKeyLength) {
        this.sessionKeyLength = sessionKeyLength;
    }
}
