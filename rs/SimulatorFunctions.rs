#pragma version(1)
#pragma rs java_package_name(com.codingcave.sonogramsim)

int data_size_x;
int data_size_y;
int data_size_z;

float3 corner_offset;

int* data;

void init() {
    data_size_x = 410;
    data_size_y = 330;
    data_size_z = 616;

    corner_offset = (float3) { data_size_x / 2.0f, data_size_y / 2.0f, data_size_z / 2.0f };
}

typedef struct Camera {
    float3 pos;
    float depth;
    float3 view;
    float3 head;
    float3 side;
    float slice_width;
    float slice_height;
    float slice_res;
} Camera;

// Returns a normalized cross product of two 3D vectors
static float3 get_normalized_cross_product(float3 a, float3 b) {
    float3 c = cross(a, b);
    return normalize(c);
}

// Returns a rotation matrix that will rotate a vector around
// the provided axis, for the specified angle theta.
static rs_matrix3x3 get_rotation_matrix(float3 axis, float theta) {
    rs_matrix3x3 rot;

    float3 axis_norm = normalize(axis);
    float a = cos(theta/2.0f);

    float _sin_theta = sin(theta/2.0f);
    float b = -axis[0]*_sin_theta;
    float c = -axis[1]*_sin_theta;
    float d = -axis[2]*_sin_theta;

    float aa = a*a;
    float bb = b*b;
    float cc = c*c;
    float dd = d*d;

    float bc = b*c;
    float ad = a*d;
    float ac = a*c;
    float ab = a*b;
    float bd = b*d;
    float cd = c*d;

    rsMatrixSet(&rot, 0, 0, aa+bb-cc-dd);
    rsMatrixSet(&rot, 1, 0, 2*(bc+ad));
    rsMatrixSet(&rot, 2, 0, 2*(bd-ac));

    rsMatrixSet(&rot, 0, 1, 2*(bc-ad));
    rsMatrixSet(&rot, 1, 1, aa+cc-bb-dd);
    rsMatrixSet(&rot, 2, 1, 2*(cd+ab));

    rsMatrixSet(&rot, 0, 2, 2*(bd+ac));
    rsMatrixSet(&rot, 1, 2, 2*(cd-ab));
    rsMatrixSet(&rot, 2, 2, aa+bb-cc-dd);

    return rot;
}

static void rotate_camera(Camera* cam, float3 axis, float theta) {
   rs_matrix3x3 rot = get_rotation_matrix(axis, theta);

   cam->view = rsMatrixMultiply(&rot, cam->view);
   cam->head = rsMatrixMultiply(&rot, cam->head);
   cam->side = rsMatrixMultiply(&rot, cam->side);
}

static int extract_value_from_data(float3 p) {
    int3 pint = convert_int3(p);

    if ( (pint.x >= 0 && pint.x < data_size_x) && (pint.y >= 0 && pint.y < data_size_y) && (pint.z >= 0 && pint.z < data_size_z) ) {
        return data[ pint.x * data_size_x * data_size_y + pint.y * data_size_y + pint.z ];
    } else {
        return 0;
    }
}

static void extract_slice(Camera* cam, int* slice_data, int slice_data_dim_w, int slice_data_dim_h) {
    float3 corner_top_left = -cam->side * (cam->slice_width/2.0f) + cam->head*(cam->slice_height/2.0f) + cam->pos + cam->view*cam->depth;
    float3 vec_step_right = cam->side * cam->slice_res;
    float3 vec_step_down = cam->head * cam->slice_res;

    //float slice_w = (int) round(cam->slice_width / cam->slice_res);
    //float slice_h = (int) round(cam->slice_height / cam->slice_res);
    //int* slice_data = (int*) malloc( sizeof(int) * slice_w * slice_h );

    float3 p = corner_top_left + corner_offset;

    for (int x = 0; x < slice_data_dim_w; x++) {
        float3 p2 = p;

        for (int y = 0; y < slice_data_dim_h; y++) {
            slice_data[ y*slice_data_dim_h + x ] = extract_value_from_data(p);
            p += vec_step_down;
        }

        p = p2 + vec_step_right;
    }
}

void test(rs_allocation i, rs_allocation o) {
    void* val = (void*) 123;
    uint32_t x = 0;
    rsSetElementAt(o, val, x);

}