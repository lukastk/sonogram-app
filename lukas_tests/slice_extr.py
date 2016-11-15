
# coding: utf-8

import numpy as np
import math
import matplotlib.pyplot as plt

f = open("data/leberExport.vol1.vol","r") # Öffne die Datei
f.read(291) # Überspringe die ersten 291 Bytes

data       = f.read(2) # Schlüsselwort / Tag
tag        = np.frombuffer(data, np.uint8)[0] # interpretiere als unsigned short (2 Byte)

data       = f.read(4) # Größe des Datensatzes
tag_length = np.frombuffer(data, np.uint32)[0] # interpretiere als unsigned long (4 Byte)

print "Sind wir an der richtigen Stelle?", tag == int(0x0050) # Tag: 80 = int(0x0050)
print "Die Größe des Datensatzes in Bytes:", tag_length

data   = f.read(tag_length) # 3D Datensatz auslesen
# Z Y X

VOLUME = np.frombuffer(data, np.uint8).reshape(616,330,410)#interpretiere als unsigned short

data_size_x = 410.0
data_size_y = 330.0
data_size_z = 616.0

corner_offset = np.array( [ data_size_x / 2, data_size_y / 2, data_size_z /2 ] )



# Right hand rule
def get_normalized_cross_product(a, b):
    #return [ a[2]*b[3] - a[3]*b[2], a[3]*b[1] - a[1]*b[3], a[1]*b[2] - a[2]*b[1] ]
    c = np.cross(a, b)
    return c / np.linalg.norm(c)

def extract_value_from_data(vec):
    x, y, z = map(int, map(round, vec))

    if (x >= 0 and x < data_size_x) and (y >= 0 and y < data_size_y) and (z >= 0 and z < data_size_z):
        return VOLUME[z, y, x]
    else:
        return 0

def extract_slice(camera_cor, camera_view_axis, camera_head_axis, camera_side_axis, camera_depth, slice_w, slice_h, slice_res, file_name):

    corner_top_left = - camera_side_axis * (slice_w/2) + camera_head_axis * (slice_h/2) + camera_cor + camera_view_axis*camera_depth
    vec_step_right = camera_side_axis * slice_res
    vec_step_down = - camera_head_axis * slice_res

    slice_w = int(round(slice_w / slice_res))
    slice_h = int(round(slice_h / slice_res))

    slice_data = np.zeros([slice_w, slice_h])

    p = corner_top_left + corner_offset
    for x in range(slice_w):
        p2 = np.copy(p)

        for y in range(slice_h):
            slice_data[x, y] =  extract_value_from_data(p)
            p += vec_step_down
        p = p2 + vec_step_right

    plt.pcolormesh(slice_data,cmap=plt.cm.Greys_r) #plotten
    plt.savefig(file_name + ".png") #Plot speichern
    plt.close()

def rotation_matrix(axis, theta):
    """
    Return the rotation matrix associated with counterclockwise rotation about
    the given axis by theta radians.
    """
    axis = np.asarray(axis)
    axis = axis/math.sqrt(np.dot(axis, axis))
    a = math.cos(theta/2.0)
    b, c, d = -axis*math.sin(theta/2.0)
    aa, bb, cc, dd = a*a, b*b, c*c, d*d
    bc, ad, ac, ab, bd, cd = b*c, a*d, a*c, a*b, b*d, c*d
    return np.array([[aa+bb-cc-dd, 2*(bc+ad), 2*(bd-ac)],
                     [2*(bc-ad), aa+cc-bb-dd, 2*(cd+ab)],
                     [2*(bd+ac), 2*(cd-ab), aa+dd-bb-cc]])

def get_rotated_camera(view, head, side, axis, theta):
    rot_m = rotation_matrix(axis, theta)
    return ( np.dot(rot_m, view),  np.dot(rot_m, head),  np.dot(rot_m, side) )

# head and the side axis both lie in a plane parallel to the slice plane.
# the slice plane is the same plane but displaced a distance camera_depth in the
# direction of the view axis.

camera_cor = np.array([0, 0, 350.0])
camera_view_axis = np.array([0, 0, -1.0])
camera_head_axis = np.array([0, 1.0, 0])
camera_side_axis = get_normalized_cross_product(camera_view_axis, camera_head_axis)

camera_depth = 300.0
slice_w = 500.0
slice_h = 500.0
slice_res = 1.0

file_name = "output/slice"

# Produce several slices

start_angle = -math.pi/3
end_angle = math.pi/3
steps = 1000
d_theta = (end_angle - start_angle) / steps

camera_view_axis, camera_head_axis, camera_side_axis = get_rotated_camera(camera_view_axis, camera_head_axis, camera_side_axis, camera_side_axis, start_angle)

for i in range(steps):
    camera_view_axis, camera_head_axis, camera_side_axis = get_rotated_camera(camera_view_axis, camera_head_axis, camera_side_axis, camera_side_axis, d_theta)
    extract_slice(camera_cor, camera_view_axis, camera_head_axis, camera_side_axis, camera_depth, slice_w, slice_h, slice_res, file_name + str(i + 1))

    print "Slice " + str(i + 1) + " out of " + str(steps) + " complete"
