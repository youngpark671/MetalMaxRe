package me.afoolslove.metalmaxre.io;

import org.jetbrains.annotations.Range;

import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 * @author AFoolLove
 */
public class BitInputStream extends ByteArrayInputStream {
    @Range(from = 0x00, to = 0x08)
    private int bitIndex = 0;

    public BitInputStream(byte[] buf) {
        super(buf);
    }

    public BitInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    public synchronized void readBits(byte[] buf, int offset, @Range(from = 0x00, to = 0x08) int bitIndex, long count) {
        // 确保内容能够读取完整
        Objects.checkFromIndexSize(offset, count / 8, buf.length);
    }
}
