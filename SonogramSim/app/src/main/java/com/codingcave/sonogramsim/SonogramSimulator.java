package com.codingcave.sonogramsim;

import android.opengl.Matrix;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lukas on 01/10/16.
 */

public class SonogramSimulator {

    Vector3 camera_cor;
    Vector3 camera_view_axis;
    Vector3 camera_head_axis;
    Vector3 camera_side_axis;

    float[] rot_m;

    byte[][] slice_data;

    byte[][][] sono_data;
    Vector3 data_center_cor;

    int data_size_x;
    int data_size_y;
    int data_size_z;

    float camera_depth;
    float slice_w;
    float slice_h;
    float slice_res;


    int slice_w_i;
    int slice_h_i;

    public SonogramSimulator() {
        data_size_x = 205;
        data_size_y = 165;
        data_size_z = 308;

        camera_depth = 350f;
        slice_w = 121f;
        slice_h = 121f;
        slice_res = 1.0f;

        slice_w_i = Math.round(slice_w / slice_res);
        slice_h_i = Math.round(slice_h / slice_res);
        slice_data = new byte[slice_h_i][slice_w_i];

        camera_cor = new Vector3(0, 0, 350);
        camera_view_axis = new Vector3(0, 0, -1);
        camera_head_axis = new Vector3(0, 1, 0);
        camera_side_axis = Vector3.get_cross(camera_view_axis, camera_head_axis);

        data_center_cor = new Vector3(data_size_x / 2.0f, data_size_x / 2.0f, data_size_z / 2.0f);
    }

    public void load_file(InputStream file_stream) {
        try {
            BufferedInputStream buf = new BufferedInputStream(file_stream);

            /*buf.skip(291);

            d = new byte[2];
            buf.read(d, 0, 2);
            int tag = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).getShort();

            d = new byte[4];
            buf.read(d, 0, 4);
            int tag_length = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).getInt();

            short[] sono_data_1d = new short[tag_length / 2]; // Size of unint8: 2 bytes
            d = new byte[tag_length];
            buf.read(d, 0, tag_length);
            ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sono_data_1d);


            System.out.println("#################");
            System.out.println("#################");
            System.out.println("#################");

            System.out.println(tag);
            System.out.println(tag_length);
            */

            /*
            index 0: [z=0,y=0,x=0]
            index 1: [z=0,y=0,x=1]
            index 2: [z=0,y=1,x=0]
            index 3: [z=0,y=1,x=1]
            index 4: [z=1,y=0,x=0]
            index 5: [z=1,y=0,x=1]
            index 6: [z=1,y=1,x=0]
            index 7: [z=1,y=1,x=1]
             */

            int size = data_size_x*data_size_y*data_size_z;

            byte[] sono_data_1d = new byte[size]; // Size of unint8: 2 bytes
            buf.read(sono_data_1d, 0, size);

            // Reshape data into 3D array

            int i = 0;

            sono_data = new byte[data_size_z][data_size_y][data_size_x];

            for (int z = 0; z < data_size_z; z++) {
                for (int y = 0; y < data_size_y; y++) {
                    for (int x = 0; x < data_size_x; x++)  {
                        sono_data[z][y][x] = sono_data_1d[i];

                        i++;
                    }
                }
            }

            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void extract_slice() {

        camera_depth += 1;

        Vector3 corner_top_left = Vector3.get_multiplied(camera_side_axis, -(slice_w/2));
        corner_top_left.add(Vector3.get_multiplied(camera_head_axis, (slice_h/2)));
        corner_top_left.add(camera_cor);
        corner_top_left.add(Vector3.get_multiplied(camera_view_axis, camera_depth));

        Vector3 vec_step_right = Vector3.get_multiplied(camera_side_axis, slice_res);
        Vector3 vec_step_down = Vector3.get_multiplied(camera_head_axis, -slice_res);

        Vector3 point = Vector3.get_addition(corner_top_left, data_center_cor);

        for (int y = 0; y < slice_h_i; y++) {
            for (int x = 0; x < slice_w_i; x++) {
                float cx = point.v[0] + vec_step_down.v[0]*x;
                float cy = point.v[1] + vec_step_down.v[1]*x;
                float cz = point.v[2] + vec_step_down.v[2]*x;

                slice_data[y][x] = extract_value_from_data(cx, cy, cz);
            }

            point.add(vec_step_right);
        }
    }

    byte extract_value_from_data(float cx, float cy, float cz) {
        /*int x = Math.floor(cx);
        int y = Math.floor(cy);
        int z = Math.floor(cz);*/
        int x = (int) cx;
        int y = (int) cy;
        int z = (int) cz;

        return sono_data[z][y][x];

        /*
        if  ( (x >= 0 && x < data_size_x) &&
                (y >= 0 && y < data_size_y) &&
                (z >= 0 && z < data_size_z) ) {
            return sono_data[z][y][x];
        } else {
            System.out.println("@@@@@");
            return 0;

        }*/
    }

    void rotate_camera(Vector3 axis, float theta) {
        Matrix.setRotateM(rot_m,0, theta, axis.v[0], axis.v[1], axis.v[2]);
        camera_view_axis.matrix_multiply(rot_m);
        camera_head_axis.matrix_multiply(rot_m);
        camera_side_axis.matrix_multiply(rot_m);
    }

    public byte[][] get_slice_data() {
        return slice_data;
    }
}
