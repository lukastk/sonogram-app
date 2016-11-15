package com.codingcave.sonogramsim;

import android.opengl.Matrix;

import java.util.Arrays;

/**
 * Created by lukas on 23/10/16.
 */

class Vector3 {
    public float[] v;

    /* Used for vector operations */
    static float[] res_vector;

    public Vector3() {
        v = new float[3];
    }
    public Vector3(Vector3 _c) {
        v = Arrays.copyOf(_c.v, _c.v.length);
    }
    public Vector3(float x, float y, float z) {
        v = new float[] {x, y, z};
    }

    public float x() { return v[0]; }
    public float y() { return v[1]; }
    public float z() { return v[2]; }

    public void normalize() {
        float r = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);

        v[0] /= r;
        v[1] /= r;
        v[2] /= r;
    }

    public Vector3 get_normalized() {
        Vector3 vec  = new Vector3(this);
        vec.normalize();

        return vec;
    }

    public void multiply(float f) {
        v[0] *= f;
        v[1] *= f;
        v[2] *= f;
    }

    public static Vector3 get_multiplied(Vector3 v, float f) {
        Vector3 vec  = new Vector3(v);
        vec.multiply(f);

        return vec;
    }

    public void add(Vector3 b) {
        v[0] += b.v[0];
        v[1] += b.v[1];
        v[2] += b.v[2];
    }

    public static Vector3 get_addition(Vector3 a, Vector3 b) {
        Vector3 c = new Vector3(a);
        c.add(b);

        return c;
    }

    public static Vector3 get_subtraction(Vector3 a, Vector3 b) {
        return new Vector3(a.v[0] - b.v[0], a.v[1] - b.v[1], a.v[2] - b.v[2]);
    }

    private static void matrix_multiply_vector(Vector3 v, float[] m) {
        Matrix.multiplyMM(res_vector, 0, m, 0, v.v, 0);
    }

    public void matrix_multiply(float[] rm) {
        matrix_multiply_vector(this, rm);
    }
    public static Vector3 get_matrix_multiplied(Vector3 v, float[] rm) {
        Vector3 vec  = new Vector3(v);
        vec.matrix_multiply(rm);

        return vec;
    }

    public static Vector3 get_cross(Vector3 a, Vector3 b) {
        Vector3 c = new Vector3();

        c.v[0] = a.v[0]*b.v[2] - a.v[2]*b.v[1];
        c.v[1] = a.v[2]*b.v[0] - a.v[0]*b.v[2];
        c.v[2] = a.v[0]*b.v[1] - a.v[1]*b.v[0];

        return c;
    }
}
