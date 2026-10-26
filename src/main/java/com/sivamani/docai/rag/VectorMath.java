package com.sivamani.docai.rag;

import java.nio.ByteBuffer;

/** Vector helpers: cosine similarity and (de)serialisation of float vectors for MySQL BLOBs. */
public final class VectorMath {

    private VectorMath() {}

    /** Returns -1 when the vectors have different dimensions (e.g. embedding model was changed). */
    public static double cosine(float[] a, float[] b) {
        if (a.length != b.length) return -1;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public static byte[] toBytes(float[] v) {
        ByteBuffer buffer = ByteBuffer.allocate(v.length * 4);
        buffer.asFloatBuffer().put(v);
        return buffer.array();
    }

    public static float[] fromBytes(byte[] bytes) {
        float[] v = new float[bytes.length / 4];
        ByteBuffer.wrap(bytes).asFloatBuffer().get(v);
        return v;
    }
}
