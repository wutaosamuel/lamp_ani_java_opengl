import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

public class SpotLight {

  private Material material;
  private Vec3 position;
  private Vec3 direction;
  private Mat4 model;
  private Shader shader;
  private Camera camera;
  private float constant;
  private float linear;
  private float quadratic;
  private float cutOff;
  private float outerCutOff;
  private float rotateAngle;
  private float lightScale;
  private float switchState;
  //private Mat4 perspective;

  public SpotLight(GL3 gl) {
    direction = new Vec3(0f,-1f,0f);
    cutOff = (float)Math.cos(Math.toRadians(12.5));
    outerCutOff = (float)Math.cos(Math.toRadians(15.0));
    constant = 1.0f;
    linear = 0.06f;
    quadratic = 0.01f;
    material = new Material();
    material.setAmbient(0.5f, 0.5f, 0.5f);
    material.setDiffuse(0.8f, 0.8f, 0.8f);
    material.setSpecular(1.0f, 1.0f, 1.0f);
    position = new Vec3(3f,2f,1f);
    model = new Mat4(1);
    shader = new Shader(gl, "vs_light_01.txt", "fs_lamplight.txt");
    rotateAngle = 0f;
    lightScale = 0.4f;
    switchState = 1f;
    fillBuffers(gl);
  }

  public float getSwitch(){
    return switchState;
  }

  public void setSwitch(float s) {
    this.switchState = s;
  }

  public void setScale(float scale) {
    this.lightScale = scale;
  }

  public void setDirection(Vec3 dir) {
    direction = dir;
  }

  public void setDirection(float x, float y, float z) {
    direction.x = x;
    direction.y = y;
    direction.z = z;
  }

  public Vec3 getDirection() {
    return direction;
  }
  public float getConstant(){
    return constant;
  }

  public float getLinear(){
    return linear;
  }

  public float getQuadratic(){
    return quadratic;
  }

  public float getCutOff(){
    return cutOff;
  }

  public float getOuterCutOff(){
    return outerCutOff;
  }

  public void setPosition(Vec3 v) {
    position.x = v.x;
    position.y = v.y;
    position.z = v.z;
  }

  public void setPosition(float x, float y, float z) {
    position.x = x;
    position.y = y;
    position.z = z;
  }

  public Vec3 getPosition() {
    return position;
  }

  public void setMaterial(Material m) {
    material = m;
  }

  public Material getMaterial() {
    return material;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void setRotateAngle(float a) {
    this.rotateAngle = a;
  }

  /*public void setPerspective(Mat4 perspective) {
    this.perspective = perspective;
  }*/

  public void render(GL3 gl) { //, Mat4 perspective, Mat4 view) {
    Mat4 model = new Mat4(1);
    model = Mat4.multiply(model, Mat4Transform.translate(position));
    model = Mat4.multiply(model, Mat4Transform.rotateAroundZ(rotateAngle));
    model = Mat4.multiply(model, Mat4Transform.scale(lightScale,lightScale,lightScale));
    model = Mat4.multiply(model, Mat4Transform.translate(0,0.5f,0));

    Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), model));

    shader.use(gl);
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

    shader.setFloat(gl, "switchState", switchState);

    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
    gl.glBindVertexArray(0);
  }

  public void dispose(GL3 gl) {
    gl.glDeleteBuffers(1, vertexBufferId, 0);
    gl.glDeleteVertexArrays(1, vertexArrayId, 0);
    gl.glDeleteBuffers(1, elementBufferId, 0);
  }

    // ***************************************************
  /* THE DATA
   */
  // anticlockwise/counterclockwise ordering

    private float[] vertices = new float[] {  // x,y,z
      -0.5f, -0.5f, -0.5f,  // 0
      -0.5f, -0.5f,  0.5f,  // 1
      -0.5f,  0.5f, -0.5f,  // 2
      -0.5f,  0.5f,  0.5f,  // 3
       0.5f, -0.5f, -0.5f,  // 4
       0.5f, -0.5f,  0.5f,  // 5
       0.5f,  0.5f, -0.5f,  // 6
       0.5f,  0.5f,  0.5f   // 7
     };

    private int[] indices =  new int[] {
      0,1,3, // x -ve
      3,2,0, // x -ve
      4,6,7, // x +ve
      7,5,4, // x +ve
      1,5,7, // z +ve
      7,3,1, // z +ve
      6,4,0, // z -ve
      0,2,6, // z -ve
      0,4,5, // y -ve
      5,1,0, // y -ve
      2,3,7, // y +ve
      7,6,2  // y +ve
    };

  private int vertexStride = 3;
  private int vertexXYZFloats = 3;

  // ***************************************************
  /* THE LIGHT BUFFERS
   */

  private int[] vertexBufferId = new int[1];
  private int[] vertexArrayId = new int[1];
  private int[] elementBufferId = new int[1];

  private void fillBuffers(GL3 gl) {
    gl.glGenVertexArrays(1, vertexArrayId, 0);
    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glGenBuffers(1, vertexBufferId, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
    FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);

    gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);

    int stride = vertexStride;
    int numXYZFloats = vertexXYZFloats;
    int offset = 0;
    gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride*Float.BYTES, offset);
    gl.glEnableVertexAttribArray(0);

    gl.glGenBuffers(1, elementBufferId, 0);
    IntBuffer ib = Buffers.newDirectIntBuffer(indices);
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
    gl.glBindVertexArray(0);
  }

}
