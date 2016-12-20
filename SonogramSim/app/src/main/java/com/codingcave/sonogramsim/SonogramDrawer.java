package com.codingcave.sonogramsim;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the drawing of the Sonogram data.
 */
public class SonogramDrawer {
    // Contains the latest extracted slice of data from the sonogram data.
    int[] sonogram_slice_data;

    // Contains the sonogram data.
    byte[][][] sonogram_data;

    // Size of sonogram data along the three dimensions.
    int data_size_x;
    int data_size_y;
    int data_size_z;

    // The factor by which the size of the sonogram drawing will increase
    int zoom = 1;

    public SonogramDrawer() {
    }

    /**
     * Converts a byte array to an integer.
     */
    private int byte_array_to_int(byte[] bytes) {
        int out = 0;

        for (int i = 0; i < bytes.length; i++) {
            int val = bytes[i];
            while (val < 0) {
                val += 256;
            }

            out += (int)(val*Math.pow(2, i*8));
        }

        return out;
    }

    /**
     * Loads sonogram data from a file_stream. The first 12 bytes contain the size of the 3D box
     * that the data essentially is structured as. The first 4 bytes are the x-dimensions of the
     * data, the next 4 bytes are the y-dimensions, and the last 4 are the z-dimensions. After that
     * there are data_size_x*data_size_y*data_size_z many bytes corresponding to each value inside
     * the box. The first data_size_x number of bytes is the first row of the bytes, which goes on
     * for data_size_y times to form the first slice of the XY-plane.
     */
    public void load_sonogram_data(InputStream file_stream) {
        try {
            BufferedInputStream buf = new BufferedInputStream(file_stream);

            // First 12 bytes contain the size of the dataset

            byte[] size_bytes = new byte[4];
            buf.read(size_bytes, 0, 4);
            data_size_x = byte_array_to_int(size_bytes);
            buf.read(size_bytes, 0, 4);
            data_size_y = byte_array_to_int(size_bytes);
            buf.read(size_bytes, 0, 4);
            data_size_z = byte_array_to_int(size_bytes);

            sonogram_slice_data = new int[data_size_x * data_size_y];

            int size = data_size_x*data_size_y*data_size_z;

            byte[] sono_data_1d = new byte[size]; // Size of unint8: 2 bytes
            buf.read(sono_data_1d, 0, size);

            // Reshape data into 3D array

            int i = 0;

            sonogram_data = new byte[data_size_z][data_size_y][data_size_x];

            for (int z = 0; z < data_size_z; z++) {
                for (int y = 0; y < data_size_y; y++) {
                    for (int x = 0; x < data_size_x; x++)  {
                        sonogram_data[z][y][x] = sono_data_1d[i];
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

    /**
     * Set the factor by which the size of the sonogram drawing will increase
     */
    public void set_zoom(int zoom) {
        this.zoom = zoom;
        sonogram_slice_data = new int[data_size_x*zoom * data_size_y*zoom];
    }

    /**
     * Gets the width of the slice width in pixels.
     */
    public int get_slice_width() { return zoom*data_size_x; }

    /**
     * Gets the height of the slice width in pixels.
     */
    public int get_slice_height() { return zoom*data_size_y; }

    /**
     * Extracts a slice of the the sonogram data. The extracted data can be accessed using the
     * function get_slice_data(). The function will extract the values from the data along a plane.
     * The position and rotation of the plane is controlled using the function parameters. Setting
     * the roll and tilt angles to 0 will make the plane parallel to the XY-plane.
     * @param z_depth The z-coordinate of the central point of the slicing plane.
     * @param roll_angle Rotation of the slicing plane clockwise along XZ-plane.
     * @param tilt_angle Rotation of the slicing plane clockwise along YZ-plane.
     */
    public void update_sonogram_slice(double z_depth, double roll_angle, double tilt_angle) {
        int z_depth_i = (int)Math.round(z_depth * data_size_z);
        double roll_sin = Math.sin(roll_angle);
        double tilt_sin =  Math.sin(tilt_angle);
        int i = 0;

        for (int y = 0; y < get_slice_height(); y++) {
            for (int x = 0; x < get_slice_width(); x++) {
                int z_displacement = (int)Math.round(roll_sin*x + tilt_sin*y);

                byte val;

                // Check if the array index is out of bounds
                if ((z_depth_i + z_displacement) >= 0 && (z_depth_i + z_displacement) < data_size_z) {
                    val = get_data_value(x/zoom, y/zoom, z_depth_i + z_displacement);
                } else {
                    val = 0; // If the index is out of bounds, the bitmap will be colored black there.
                }

                sonogram_slice_data[i] = val + (val << 8) + (val << 16); // The int contains a 24 bit RGB color.
                i++;
            }
        }

    }

    /**
     * Gets the latest slice data extracted.
     */
    public int[] get_slice_data() {
        return sonogram_slice_data;
    }

    /**
     * Gets the size of the data along the x-axis.
     */
    public int get_data_size_x() { return data_size_x; }

    /**
     * Gets the size of the data along the y-axis.
     */
    public int get_data_size_y() { return data_size_y; }

    /**
     * Gets the size of the data along the z-axis.
     */
    public int get_data_size_z() { return data_size_z; }

    /**
     * Gets the data value at the specified coordinate.
     */
    public byte get_data_value(int x, int y, int z) {
        return sonogram_data[z][y][x];
    }

    /**
     * Draws sonogram data.
     * @param canvas
     * @param paint
     * @param x x-coordinate at which to draw the sonogram slice on the screen.
     * @param y y-coordinate at which to draw the sonogram slice on the screen.
     */
    public void draw_sonogram(Canvas canvas, Paint paint, int x, int y) {
        canvas.drawBitmap(sonogram_slice_data, 0, get_slice_width(), x, y, get_slice_width(), get_slice_height(), false, paint);
    }

    /**
     * Rotates the sonogram data clockwise along the XY-plane.
     */
    public void rotate_data_XY() {
        int new_data_size_x = data_size_y;
        int new_data_size_y = data_size_x;
        int new_data_size_z = data_size_z;

        byte[][][] new_sono_data = new byte[new_data_size_z][new_data_size_y][new_data_size_x];

        for (int z = 0; z < data_size_z; z++) {
            for (int y = 0; y < data_size_y; y++) {
                for (int x = 0; x < data_size_x; x++) {
                    new_sono_data[z][x][y] = sonogram_data[z][y][x];
                }
            }
        }

        sonogram_data = new_sono_data;
        data_size_x = new_data_size_x;
        data_size_y = new_data_size_y;
        data_size_z = new_data_size_z;
    }

    /**
     * Rotates the sonogram data clockwise along the XZ-plane.
     */
    public void rotate_data_XZ() {
        int new_data_size_x = data_size_z;
        int new_data_size_y = data_size_y;
        int new_data_size_z = data_size_x;

        byte[][][] new_sono_data = new byte[new_data_size_z][new_data_size_y][new_data_size_x];

        for (int z = 0; z < data_size_z; z++) {
            for (int y = 0; y < data_size_y; y++) {
                for (int x = 0; x < data_size_x; x++) {
                    new_sono_data[x][y][z] = sonogram_data[z][y][x];
                }
            }
        }

        sonogram_data = new_sono_data;
        data_size_x = new_data_size_x;
        data_size_y = new_data_size_y;
        data_size_z = new_data_size_z;
    }

    /**
     * Rotates the sonogram data clockwise along the YZ-plane.
     */
    public void rotate_data_YZ() {
        int new_data_size_x = data_size_x;
        int new_data_size_y = data_size_z;
        int new_data_size_z = data_size_y;

        byte[][][] new_sono_data = new byte[new_data_size_z][new_data_size_y][new_data_size_x];

        for (int z = 0; z < data_size_z; z++) {
            for (int y = 0; y < data_size_y; y++) {
                for (int x = 0; x < data_size_x; x++) {
                    new_sono_data[y][z][x] = sonogram_data[z][y][x];
                }
            }
        }

        sonogram_data = new_sono_data;
        data_size_x = new_data_size_x;
        data_size_y = new_data_size_y;
        data_size_z = new_data_size_z;
    }

    /**
     * Flip data along X-axis.
     */
    public void flip_data_X() {
        byte[][][] new_sono_data = new byte[data_size_z][data_size_y][data_size_x];

        for (int z = 0; z < data_size_z; z++) {
            for (int y = 0; y < data_size_y; y++) {
                for (int x = 0; x < data_size_x; x++) {
                    new_sono_data[z][y][data_size_x - x - 1] = sonogram_data[z][y][x];
                }
            }
        }

        sonogram_data = new_sono_data;
    }

    /**
     * Flip data along Y-axis.
     */
    public void flip_data_Y() {
        byte[][][] new_sono_data = new byte[data_size_z][data_size_y][data_size_x];

        for (int z = 0; z < data_size_z; z++) {
            for (int y = 0; y < data_size_y; y++) {
                for (int x = 0; x < data_size_x; x++) {
                    new_sono_data[z][data_size_y - y - 1][x] = sonogram_data[z][y][x];
                }
            }
        }

        sonogram_data = new_sono_data;
    }

    /**
     * Flip data along Z-axis.
     */
    public void flip_data_Z() {
        byte[][][] new_sono_data = new byte[data_size_z][data_size_y][data_size_x];

        for (int z = 0; z < data_size_z; z++) {
            for (int y = 0; y < data_size_y; y++) {
                for (int x = 0; x < data_size_x; x++) {
                    new_sono_data[data_size_z - z - 1][y][x] = sonogram_data[z][y][x];
                }
            }
        }

        sonogram_data = new_sono_data;
    }
}
