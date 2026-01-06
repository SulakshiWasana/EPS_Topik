package com.sejong.academy.eps_topik.service.crypto;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class AESGCMBlockCipher extends BufferedBlockCipher {
    private GCMBlockCipher internalCipher = new GCMBlockCipher(new AESEngine());

    public AESGCMBlockCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
    }

    public void init(boolean forEncryption, CipherParameters params) {
        this.internalCipher.init(forEncryption, params);
    }

    public int getOutputSize(int len) {
        return this.internalCipher.getOutputSize(len);
    }

    public int doFinal(byte[] out, int outOff) throws InvalidCipherTextException {
        return this.internalCipher.doFinal(out, outOff);
    }

    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) {
        return this.internalCipher.processBytes(in, inOff, len, out, outOff);
    }
}
