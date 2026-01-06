package com.sejong.academy.eps_topik.service.crypto;

import com.sejong.academy.eps_topik.util.LogMessageUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

//convert to pkcs8 using below command
//openssl pkcs8 -topk8 -nocrypt -in key.pem -out keypcks8
@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Crypto {

    @Value("${crypto.private-key}")
    private String base64PrivateKey;

    @Value("${crypto.password}")
    private String aesPassword;

    private String aesAlgorithm = "AES/CBC/PKCS5Padding";


    private SecretKey getAesKeyFromPassword(String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(this.aesPassword.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private SecretKey getAesKeyFromPasswordWithKey(String salt, String key) throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private String encryptAes(String input, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(this.aesAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    private String encryptAes(String input, SecretKeySpec secretKeySpec, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(this.aesAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    private byte[] encrypt(String data, String publicKey) throws BadPaddingException, IllegalBlockSizeException,
            InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey("RSA", publicKey));
        return cipher.doFinal(data.getBytes());
    }

    private String decryptAes(String cipherText, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(this.aesAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    private String decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(data));
    }

    private byte[] encryptECIES(String plaintext, byte[] publicKeyBytes) throws NoSuchAlgorithmException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException,
            NoSuchProviderException, NoSuchPaddingException {
        Security.addProvider(new BouncyCastleProvider());

        byte[] inputBytes = plaintext.getBytes();

        org.bouncycastle.jce.spec.IESParameterSpec params = new IESParameterSpec(null, null, 128, 128, null);
        IESCipherGCM cipher = new IESCipherGCM(new IESEngineGCM(new ECDHBasicAgreement(),
                new KDF2BytesGenerator(new SHA256Digest()), new AESGCMBlockCipher()), 128);

        cipher.engineInit(Cipher.ENCRYPT_MODE, getPublicKey("ECDH", new String(publicKeyBytes)), params,
                new SecureRandom());

        return cipher.engineDoFinal(inputBytes, 0, inputBytes.length);
    }

    private PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            log.info(LogMessageUtil.PRIVATE_KEY, "Unable to retrieve private key: " + e.getMessage());
        }
        return privateKey;
    }

    public String encryptAes(String data, String secretKey) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES");

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            String encryptedString = encryptAes(data, secretKeySpec, ivSpec);
            String base64Iv = Base64.getEncoder().encodeToString(iv);
            log.info(LogMessageUtil.ENCRYPT_DATA_AES, "data encrypted successfully.");
            //base64iv.ciper
            return base64Iv + "." + encryptedString;
        } catch (Exception e) {
            log.info(LogMessageUtil.ENCRYPT_DATA_AES, "Unable to encrypt the data: " + e.getMessage());
            return null;
        }
    }

    public String encryptAes(String data) {
        try {
            // generate iv
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltString = new String(salt);
            SecretKey key;

            key = this.getAesKeyFromPassword(saltString);

            String encryptedString = this.encryptAes(data, key, ivSpec);

            String base64Iv = Base64.getEncoder().encodeToString(iv);
            String base64Salt = Base64.getEncoder().encodeToString(salt);
            log.info(LogMessageUtil.ENCRYPT_DATA_AES, "data encrypted successfully.");
            //base64salt.base64iv.ciper
            return base64Salt + "." + base64Iv + "." + encryptedString;
        } catch (Exception e) {
            log.info(LogMessageUtil.ENCRYPT_DATA_AES, "Unable to encrypt the data: " + e.getMessage());
            return null;
        }
    }

    public String decryptAes(String data) {
        try {
            //base64salt.base64iv.ciper
            String[] ciperData = data.split("\\.");
            byte[] iv = Base64.getDecoder().decode(ciperData[1]);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            String saltString = new String(Base64.getDecoder().decode(ciperData[0]));
            SecretKey key = this.getAesKeyFromPassword(saltString);
            log.info(LogMessageUtil.DECRYPT_DATA_AES, "data decrypted successfully.");
            return this.decryptAes(ciperData[2], key, ivSpec);
        } catch (Exception e) {
            log.info(LogMessageUtil.DECRYPT_DATA_AES, "Unable to decrypt the data: " + e.getMessage());
            return null;
        }
    }

    public String decrypt(String data) {
        data = data.replace("\n", "");
        try {
            log.info(LogMessageUtil.DECRYPT_DATA, "data decrypted successfully.");
            return this.decrypt(Base64.getDecoder().decode(data.getBytes()), getPrivateKey(this.base64PrivateKey));
        } catch (Exception e) {
            log.info(LogMessageUtil.DECRYPT_DATA, "Unable to decrypt the data: " + e.getMessage());
            return null;
        }
    }

    public byte[] encryptData(String data, String publicKey) {
        try {
            log.info(LogMessageUtil.ENCRYPT_DATA, "data encrypted successfully");
            return encrypt(data, publicKey);
        } catch (Exception e) {
            log.info(LogMessageUtil.ENCRYPT_DATA, "Encryption failed: " + e.getMessage());
            return new byte[0];
        }
    }

    public byte[] encryptDataWithEC(String data, String publicKey) {
        try {
            log.info(LogMessageUtil.ENCRYPT_DATA_EC, "data encrypted successfully");
            return encryptECIES(data, publicKey.getBytes());
        } catch (Exception e) {
            log.info(LogMessageUtil.ENCRYPT_DATA_EC, "Encryption failed: " + e.getMessage());
        }
        return new byte[0];
    }

    public PublicKey getPublicKey(String keyType, String base64PublicKey) {
        PublicKey publicKey = null;
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance(keyType);
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.info(LogMessageUtil.PUBLIC_KEY, "Unable to retrieve public key: " + e.getMessage());
        }
        return publicKey;
    }

    public String generateSignature(String dataString, String privateKey) {
        String signature = "";
        try {
            byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);

            Signature sig = Signature.getInstance("SHA256WithRSA");
            sig.initSign(privKey);
            sig.update(dataString.getBytes());
            byte[] signatureBytes = sig.sign();
            log.info(LogMessageUtil.GENERATE_SIGNATURE, "Generate Signature");
            signature = Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.info(LogMessageUtil.GENERATE_SIGNATURE, "Unable to generate the signature: " + e.getMessage());
        }
        return signature;
    }
}
