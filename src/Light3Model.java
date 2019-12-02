import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

public class Light3Model {

  private Mesh mesh;
  private int[] textureId1;
  private int[] textureId2;
  private Material material;
  private Shader shader;
  private Mat4 modelMatrix;
  private Camera camera;
  private PointLight light1, light2;
  private SpotLight spotLight;
  private float switchLampState, switchWorldState1, switchWorldState2;

  public Light3Model(GL3 gl, Camera camera, PointLight light1, PointLight light2, SpotLight spotLight, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, int[] textureId1, int[] textureId2) {
    this.mesh = mesh;
    this.material = material;
    this.modelMatrix = modelMatrix;
    this.shader = shader;
    this.camera = camera;
    this.light1 = light1;
    this.light2 = light2;
    this.spotLight = spotLight;
    this.textureId1 = textureId1;
    this.textureId2 = textureId2;
    switchLampState = 1f;
    switchWorldState1 = 1f;
    switchWorldState2 = 1f;
  }

  public Light3Model(GL3 gl, Camera camera, PointLight light1, PointLight light2, SpotLight spotLight, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh, int[] textureId1) {
    this(gl, camera, light1, light2, spotLight, shader, material, modelMatrix, mesh, textureId1, null);
  }

  public Light3Model(GL3 gl, Camera camera, PointLight light1, PointLight light2, SpotLight spotLight, Shader shader, Material material, Mat4 modelMatrix, Mesh mesh) {
    this(gl, camera, light1, light2, spotLight, shader, material, modelMatrix, mesh, null, null);
  }

  public void setSwitchLampState(float s) {
    this.switchLampState = s;
  }

  public void setSwitchWorldState(float s, PointLight light) {
    if (light == this.light1)
      this.switchWorldState1 = s;
    if (light == this.light2)
      this.switchWorldState2 = s;
  }

  public void setModelMatrix(Mat4 m) {
    modelMatrix = m;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void setLight1(PointLight light1) {
    this.light1 = light1;
  }

  public void setLight2(PointLight light2) {
    this.light2 = light2;
  }

  public void setPointLight(SpotLight spotLight) {
    this.spotLight = spotLight;
  }

  public void render(GL3 gl, Mat4 modelMatrix) {
    Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), modelMatrix));
    shader.use(gl);
    shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

    shader.setVec3(gl, "viewPos", camera.getPosition());

    shader.setVec3(gl, "light1.position", light1.getPosition());
    shader.setFloat(gl, "light1.constant", light1.getConstant());
    shader.setFloat(gl, "light1.linear", light1.getLinear());
    shader.setFloat(gl, "light1.quadratic", light1.getQuadratic());
    shader.setVec3(gl, "light1.ambient", light1.getMaterial().getAmbient());
    shader.setVec3(gl, "light1.diffuse", light1.getMaterial().getDiffuse());
    shader.setVec3(gl, "light1.specular", light1.getMaterial().getSpecular());

    shader.setVec3(gl, "light2.position", light2.getPosition());
    shader.setFloat(gl, "light2.constant", light2.getConstant());
    shader.setFloat(gl, "light2.linear", light2.getLinear());
    shader.setFloat(gl, "light2.quadratic", light2.getQuadratic());
    shader.setVec3(gl, "light2.ambient", light2.getMaterial().getAmbient());
    shader.setVec3(gl, "light2.diffuse", light2.getMaterial().getDiffuse());
    shader.setVec3(gl, "light2.specular", light2.getMaterial().getSpecular());

    shader.setVec3(gl, "spotLight.position", spotLight.getPosition());
    shader.setVec3(gl, "spotLight.direction", spotLight.getDirection());
    shader.setFloat(gl, "spotLight.cutOff", spotLight.getCutOff());
    shader.setFloat(gl, "spotLight.outerCutOff", spotLight.getOuterCutOff());
    shader.setFloat(gl, "spotLight.constant", spotLight.getConstant());
    shader.setFloat(gl, "spotLight.linear", spotLight.getLinear());
    shader.setFloat(gl, "spotLight.quadratic", spotLight.getQuadratic());
    shader.setVec3(gl, "spotLight.ambient", spotLight.getMaterial().getAmbient());
    shader.setVec3(gl, "spotLight.diffuse", spotLight.getMaterial().getDiffuse());
    shader.setVec3(gl, "spotLight.specular", spotLight.getMaterial().getSpecular());

    shader.setVec3(gl, "material.ambient", material.getAmbient());
    shader.setVec3(gl, "material.diffuse", material.getDiffuse());
    shader.setVec3(gl, "material.specular", material.getSpecular());
    shader.setFloat(gl, "material.shininess", material.getShininess());

    shader.setInt(gl, "material.diffuse", 0);  // be careful to match these with GL_TEXTURE0 and GL_TEXTURE1
    shader.setInt(gl, "material.specular", 1);
    shader.setFloat(gl, "switchLampState", switchLampState);
    shader.setFloat(gl, "switchWorldState1", switchWorldState1);
    shader.setFloat(gl, "switchWorldState2", switchWorldState2);

    if (textureId1!=null) {
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glBindTexture(GL.GL_TEXTURE_2D, textureId1[0]);
    }
    if (textureId2!=null) {
      gl.glActiveTexture(GL.GL_TEXTURE1);
      gl.glBindTexture(GL.GL_TEXTURE_2D, textureId2[0]);
    }
    mesh.render(gl);
  }

  public void render(GL3 gl) {
    render(gl, modelMatrix);
  }

  public void dispose(GL3 gl) {
    mesh.dispose(gl);
    if (textureId1!=null) gl.glDeleteBuffers(1, textureId1, 0);
    if (textureId2!=null) gl.glDeleteBuffers(1, textureId2, 0);
  }

}
