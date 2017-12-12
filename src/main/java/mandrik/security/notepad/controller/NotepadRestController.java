package mandrik.security.notepad.controller;



import mandrik.security.notepad.controller.utils.RestUtils;
import mandrik.security.notepad.controller.utils.ValuesConfiguration;
import mandrik.security.notepad.service.crypto.FileCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

import static mandrik.security.notepad.controller.utils.RestUtils.RESULT_ERROR;
import static mandrik.security.notepad.controller.utils.RestUtils.RESULT_SUCCESS;
import static mandrik.security.notepad.controller.utils.RestUtils.mapOf;

@RestController
public class NotepadRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotepadRestController.class);

    private ValuesConfiguration valuesConfiguration;

    @Autowired
    public NotepadRestController(ValuesConfiguration valuesConfiguration) {
        this.valuesConfiguration = valuesConfiguration;
    }

    @RequestMapping(value = "/key/generate", produces = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.GET)
    public String generateKey(HttpServletRequest request) {

        byte[] sessionKey = new byte[valuesConfiguration.getSessionKeyLength()];
        new Random().nextBytes(sessionKey);

        HttpSession session = request.getSession();
        Byte[] key = new Byte[valuesConfiguration.getSessionKeyLength()];
        int index = 0;
        for(byte b: sessionKey)
            key[index++] = b;
        session.setAttribute("sessionKey", key);


        Map response = RestUtils.mapOf("sessionKey", sessionKey);
        LOGGER.info("Generated new session key: " + Arrays.toString(sessionKey));
        return "success";
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

        return RESULT_SUCCESS;
    }


    @PostMapping("/upload/public-rsa-key")
    public Map uploadOpenRSAKey(@RequestBody Map<String, String> openRSAKey, HttpServletRequest request) {


        if(!openRSAKey.containsKey("key")) {
            return RESULT_ERROR;
        }

        String rsaKey = openRSAKey.get("key");
        byte[] openKey = Base64.getDecoder().decode(rsaKey);

        LOGGER.info(Arrays.toString(openKey));

        Byte[] key = new Byte[valuesConfiguration.getSessionKeyLength()];
        int index = 0;
        for(byte b: openKey)
            key[index++] = b;
        HttpSession session = request.getSession();
        session.setAttribute("publicRSAKey", key);

        return RESULT_SUCCESS;
    }


    @GetMapping("/key/get")
    public Map getSessionKeyEncryptRSA(HttpServletRequest request) {

        HttpSession session = request.getSession();
        Byte[] sessionKey = (Byte[]) session.getAttribute("sessionKey");
        Byte[] publicRSAKey = (Byte[]) session.getAttribute("publicRSAKey");


        byte[] encryptSessionKey = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            byte[] rsaKey = new byte[publicRSAKey.length];

            X509EncodedKeySpec spec = new X509EncodedKeySpec(rsaKey);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            cipher.init(Cipher.ENCRYPT_MODE, fact.generatePublic(spec));

            encryptSessionKey = cipher.doFinal();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            return RESULT_ERROR;
        }

        return mapOf("sessionKey", sessionKey);
    }
}
