package mandrik.security.notepad.controller;


import com.sun.deploy.util.ArrayUtil;
import mandrik.security.notepad.controller.utils.RestUtils;
import mandrik.security.notepad.controller.utils.ValuesConfiguration;
import mandrik.security.notepad.service.crypto.FileCipher;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

@RestController
public class NotepadRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotepadRestController.class);

    private ValuesConfiguration valuesConfiguration;

    @Autowired
    public NotepadRestController(ValuesConfiguration valuesConfiguration) {
        this.valuesConfiguration = valuesConfiguration;
    }

    @GetMapping("/generate/key")
    public Map generateKey(HttpServletRequest request) {

        /*int[] bytes = new int[valuesConfiguration.getSessionKeyLength()];

        for(int i = 0; i < valuesConfiguration.getSessionKeyLength(); i+=4) {
            bytes[i] = new Random().nextInt(16);
        }

        ArrayList<Byte> sessionKey = new ArrayList<>();
        sessionKey.addAll(Arrays.asList(ArrayUtils.toObject(bytes)));

        new Random().nextBytes(sessionKey);

        StringBuilder sessionKeyStb = new StringBuilder();

        for(int i = 0; i < valuesConfiguration.getSessionKeyLength(); i++) {
            sessionKeyStb.append((char) sessionKey[i]);
        }

        String stringKey = sessionKeyStb.toString();

        */

        byte[] sessionKey = new byte[valuesConfiguration.getSessionKeyLength()];
        new Random().nextBytes(sessionKey);

        HttpSession session = request.getSession();
        session.setAttribute("sessionKey", sessionKey);


        Map response = RestUtils.mapOf("sessionKey", sessionKey);
        LOGGER.info("Generated new session key: " + Arrays.toString(sessionKey));
        return response;
    }

    @PostMapping("/download")
    public Map downloadFile(@RequestBody Map<String, String> mapFileName, HttpServletRequest request) {

        String fileName = mapFileName.get("fileName");

        String downloadFilePath = valuesConfiguration.getFilesDir() + fileName;
        String encryptFilePath = valuesConfiguration.getEncryptFilesDir() + fileName;
        FileCipher fileCipher = new FileCipher(downloadFilePath, encryptFilePath,
                String.valueOf(request.getSession().getAttribute("sessionKey")), true);

        try {
            fileCipher.cryptFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileCipher = new FileCipher(encryptFilePath, valuesConfiguration.getFilesDir() + "123.txt",
                String.valueOf(request.getSession().getAttribute("sessionKey")), false);

        try {
            fileCipher.cryptFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return RestUtils.RESULT_SUCCESS;
    }


    @PostMapping("/upload/public-rsa-key")
    public Map uploadOpenRSAKey(@RequestBody Map<String, String> mapOpenRSAKey, HttpServletRequest request) {

        String openKey = mapOpenRSAKey.get("publicRSAKey");

        HttpSession session = request.getSession();
        session.setAttribute("publicRSAKey", openKey);



        return RestUtils.RESULT_SUCCESS;
    }
}
