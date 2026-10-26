package com.sivamani.docai.rag;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VectorMathTest {

    @Test
    void identicalVectorsHaveSimilarityOne() {
        assertEquals(1.0, VectorMath.cosine(new float[]{1, 2, 3}, new float[]{1, 2, 3}), 1e-9);
    }

    @Test
    void orthogonalVectorsHaveSimilarityZero() {
        assertEquals(0.0, VectorMath.cosine(new float[]{1, 0}, new float[]{0, 1}), 1e-9);
    }

    @Test
    void differentDimensionsAreFlagged() {
        assertEquals(-1.0, VectorMath.cosine(new float[]{1, 2}, new float[]{1, 2, 3}), 1e-9);
    }

    @Test
    void bytesRoundTrip() {
        float[] v = {0.5f, -1.25f, 3f};
        assertArrayEquals(v, VectorMath.fromBytes(VectorMath.toBytes(v)), 0f);
    }
}
