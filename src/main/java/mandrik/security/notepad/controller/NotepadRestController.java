package mandrik.security.notepad.controller;



import mandrik.security.notepad.controller.utils.ValuesConfiguration;
import mandrik.security.notepad.service.crypto.FileCipher;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
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


        LOGGER.info("Generated new session key: " + Arrays.toString(sessionKey));
        return "success";
    }

    @PostMapping("/download")
    public void downloadFile(@RequestBody Map<String, String> mapFileName,
                            HttpServletRequest request, HttpServletResponse response) {

        String fileName = mapFileName.get("fileName");

        String downloadFilePath = valuesConfiguration.getFilesDir() + fileName;
        String encryptFilePath = valuesConfiguration.getEncryptFilesDir() + fileName;
        FileCipher fileCipher = new FileCipher(downloadFilePath, encryptFilePath,
                Base64.encodeBase64String(toByteArray((Byte[])request.getSession().getAttribute("sessionKey"))), true);

        try {
            fileCipher.cryptFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Paths.get(encryptFilePath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(encryptFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*fileCipher = new FileCipher(encryptFilePath, valuesConfiguration.getFilesDir() + "temp.txt",
                Base64.encodeBase64String(toByteArray((Byte[])request.getSession().getAttribute("sessionKey"))), false);

        try {
            fileCipher.cryptFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    @PostMapping("/upload/public-rsa-key")
    public Map uploadOpenRSAKey(@RequestBody Map<String, String> openRSAKey, HttpServletRequest request) {

        if(!openRSAKey.containsKey("key")) {
            return RESULT_ERROR;
        }
        String publicK = openRSAKey.get("key");
        byte[] publicBytes = Base64.decodeBase64(publicK);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        PublicKey pubKey;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            pubKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return RESULT_ERROR;
        }

        LOGGER.info(pubKey.toString());

        HttpSession session = request.getSession();
        session.setAttribute("publicRSAKey", pubKey);

        return RESULT_SUCCESS;
    }


    @GetMapping("/session-key/get")
    public Map getSessionKeyEncryptRSA(HttpServletRequest request) {

        HttpSession session = request.getSession();
        Byte[] sessionKey = (Byte[]) session.getAttribute("sessionKey");
        PublicKey publicRSAKey = (PublicKey) session.getAttribute("publicRSAKey");

        Cipher cipher;

        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return RESULT_ERROR;
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicRSAKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return RESULT_ERROR;
        }

        byte[] encryptSessionKey;
        try {
            encryptSessionKey = cipher.doFinal(toByteArray(sessionKey));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return RESULT_ERROR;
        }

        String sessionKeyString = Base64.encodeBase64String(encryptSessionKey);

        return mapOf("sessionKey", sessionKeyString);
    }

    public static byte[] toByteArray(Byte[] byteArray) {
        byte[] bytes = new byte[byteArray.length];

        for(int i = 0; i < byteArray.length; i++) {
            bytes[i] = byteArray[i];
        }
        return bytes;
    }

    public static Byte[] toByteObjectArray(byte[] bytes) {
        Byte[] byteArray = new Byte[bytes.length];

        for(int i = 0; i < bytes.length; i++) {
            byteArray[i] = bytes[i];
        }
        return byteArray;
    }
}
