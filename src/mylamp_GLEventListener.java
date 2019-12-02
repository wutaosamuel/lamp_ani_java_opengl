import gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class mylamp_GLEventListener implements GLEventListener {

  private static final boolean DISPLAY_SHADERS = false;

  public mylamp_GLEventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(3f,25f,50f));
  }

  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }

  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float)width/(float)height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory, if necessary */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    light1.dispose(gl);
    light2.dispose(gl);
    spotLight.dispose(gl);
    floor.dispose(gl);
    wallLow.dispose(gl);
    wallHigh.dispose(gl);
    wallLeft.dispose(gl);
    wallRight.dispose(gl);
    metalSphere.dispose(gl);
    metalCube.dispose(gl);
  }


  // ***************************************************
  /* INTERACTION
   *
   *
   */

  private boolean animation = false;
  private boolean jump = false;
  private double savedTime = 0;

  public void incXPosition() {
    lampXPosition += 0.5f;
    if (lampXPosition>5f) lampXPosition = 5f;
    updateMove();
  }

  public void decXPosition() {
    lampXPosition -= 0.5f;
    if (lampXPosition<-5f) lampXPosition = -5f;
    updateMove();
  }

  private void updateMove() {
    lampMoveTranslate.setTransform(Mat4Transform.translate(lampXPosition,lampYPosition,lampZPosition));
    lampMoveTranslate.update();
    spotLight.setScale(lightScale);
    spotLight.setPosition(lampXPosition-(baseHight + lowArmLength)*(float)Math.sin(thetaBL) + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.cos(thetaJ)+0.5f*(headLength+lightScale)*(float)Math.sin(thetaJ), lampYPosition+legHeight+tableHeight+(baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.sin(thetaJ)-0.5f*(headLength+lightScale)*(float)Math.cos(thetaJ), lampZPosition);
    spotLight.setRotateAngle(270 + thetaJ_angle);
    spotLight.setDirection((float)Math.sin(thetaJ), -(float)Math.cos(thetaJ),0);
  }

  public void randomPose() {
    boolean angleState = true;
    while(angleState)
    {
      // upper arm angle: 0 < angle < 90(angleLimit)
      thetaJ_angle = (float)(angleLimit * (float)Math.random());
      // lower arm angle: 30 < angle < 90(angleLimit)
      thetaBL_angle = (float)((angleLimit - 30.0f) * (float)Math.random());
      // check
      if (thetaJ_angle - thetaBL_angle < 90.0f)
      {
        angleState = false;
      }
      else
      {
        angleState = true;
      }
      thetaBL = (float)(thetaBL_angle * Math.PI/180.0);
      thetaJ = (float)(thetaJ_angle * Math.PI/180.0);

      spotLight.setScale(lightScale);
      spotLight.setPosition(lampXPosition-(baseHight + lowArmLength)*(float)Math.sin(thetaBL) + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.cos(thetaJ)+0.5f*(headLength+lightScale)*(float)Math.sin(thetaJ), lampYPosition+legHeight+tableHeight+(baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.sin(thetaJ)-0.5f*(headLength+lightScale)*(float)Math.cos(thetaJ), lampZPosition);
      spotLight.setRotateAngle(270 + thetaJ_angle);
      spotLight.setDirection((float)Math.sin(thetaJ), -(float)Math.cos(thetaJ),0);

      lowArmMove.setTransform(Mat4Transform.translate(0,baseHight,0));
      lowArmMove.update();
      lowArmRotate.setTransform(Mat4Transform.rotateAroundZ(thetaBL_angle));
      lowArmRotate.update();
      junctionMove.setTransform(Mat4Transform.translate(-(baseHight + lowArmLength)*(float)Math.sin(thetaBL), (baseHight + lowArmLength)*(float)Math.cos(thetaBL), 0));
      junctionMove.update();
      highArmMove.setTransform(Mat4Transform.translate(-(baseHight + lowArmLength)*(float)Math.sin(thetaBL), (baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale, 0));
      highArmMove.update();
      highArmRotate.setTransform(Mat4Transform.rotateAroundZ(270 + thetaJ_angle));
      highArmRotate.update();
      headLMove.setTransform(Mat4Transform.translate(-(baseHight + lowArmLength)*(float)Math.sin(thetaBL) + highArmLength * (float)Math.cos(thetaJ), (baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale + highArmLength * (float)Math.sin(thetaJ), 0));
      headLMove.update();
      headLRotate.setTransform(Mat4Transform.rotateAroundZ(270 + thetaJ_angle));
      headLRotate.update();
    }
  }

  public void jumpAnimation() {
    boolean jumpState = true;
    currentXPosition = lampXPosition;
    currentYPosition = 0;
    currentZPosition = lampZPosition;
    while(jumpState)
    {
      maxXPosition = (float)(((float)Math.random() - 0.5f) * (tableLength - baseLength/2.0)) - currentXPosition;
      maxYPosition = 0.5f * (float)Math.sqrt((double)(maxXPosition*maxXPosition + maxZPosition*maxZPosition)) - currentYPosition;
      maxZPosition = (float)(((float)Math.random() - 0.5f) * (tableWidth - baseLength/2.0)) - currentZPosition;
      jumpState = false;
      if (-0.5f*tableLength <= maxXPosition && maxXPosition <= (-0.5f*tableLength + 2f) && -0.5f*tableWidth <= maxYPosition && maxYPosition <= (-0.5f*tableWidth + 2f))
        jumpState = true;
      if (0.5f*tableLength <= maxXPosition && maxXPosition <= (0.5f*tableLength - 0.8f) && 0.5f*tableWidth <= maxYPosition && maxYPosition <= (0.5f*tableWidth - 0.8f))
        jumpState = true;
    }
    startTime = getSeconds() - savedTime;
    jump = true;
  }

  public void switchWorldLight1() {
    currentWorldState1 = switchWorldState1;
    if (switchWorldState1 == 1f)
      switchWorldState1 = 0f;
    else
      switchWorldState1 = 1f;
    world1if = true;

  }

  public void switchWorldLight2() {
    currentWorldState2 = switchWorldState2;
    if (switchWorldState2 == 1f)
      switchWorldState2 = 0f;
    else
      switchWorldState2 = 1f;
    world2if = true;

  }

  public void switchLampLight() {
    currentLampState = switchLampState;
    if (switchLampState == 1f)
      switchLampState = 0;
    else
      switchLampState = 1f;
    lampif = true;

  }


  // ***************************************************
  /* THE SCENE
   * Now define all the methods to handle the scene.
   * This will be added to in later examples.
   */

  private Camera camera;
  private Mat4 perspective;
  private Light3Model floor, window, woodCube, metalCube, metalSphere;
  private Light3Model wallLow, wallHigh, wallLeft, wallRight;
  private PointLight light1, light2;
  private SpotLight spotLight;
  private float lightScale = 3f;
  private float currentWorldState1;
  private float currentWorldState2;
  private float switchWorldState1 = 1f;
  private float switchWorldState2 = 1f;
  private float currentLampState;
  private float switchLampState = 1f;
  private boolean lampif = false;
  private boolean world1if = false;
  private boolean world2if = false;
  private SGNode lampRoot;
  private SGNode tableRoot;

  private float lampXPosition = 0;
  private float lampYPosition = 0;
  private float lampZPosition = 0;
  private float angleLimit = 90;
  private float thetaBL_angle = 30;
  private float thetaBL = (float)(thetaBL_angle * Math.PI/180.0);
  private float thetaJ_angle = 30;
  private float thetaJ = (float)(thetaJ_angle * Math.PI/180.0);

  // limit on table length and width
  private float maxXPosition = 2f;
  private float maxYPosition = 2f;
  private float maxZPosition = 2f;
  private float currentXPosition = 0;
  private float currentYPosition = 0;
  private float currentZPosition = 0;
  private TransformNode lampMoveTranslate, lowArmMove, junctionMove, highArmMove, headLMove;
  private TransformNode lowArmRotate, highArmRotate, headLRotate;

  // lamp
  private float baseHight  = 0.5f;
  private float baseLength = 2;
  private float lowArmLength = 3;
  private float lowArmScale = 0.5f;
  private float junctionScale = 5f;
  private float highArmLength = 5;
  private float highArmScale = 0.5f;
  private float headHight = 1;
  private float headLength = 2;

  // table
  private float legHeight	= 8f;
  private float legLength	= 1f;
  private float tableHeight	= 0.5f;
  private float tableLength	= 20f;
  private float tableWidth = 15f;


  private void initialise(GL3 gl) {
    createRandomNumbers();
    int[] textureId0  = TextureLibrary.loadTexture(gl, "textures/chequerboard.jpg");
    int[] textureId1  = TextureLibrary.loadTexture(gl, "textures/jade.jpg");
    int[] textureId2  = TextureLibrary.loadTexture(gl, "textures/jade_specular.jpg");
    int[] textureId3  = TextureLibrary.loadTexture(gl, "textures/container2.jpg");
    int[] textureId4  = TextureLibrary.loadTexture(gl, "textures/container2_specular.jpg");
    int[] textureId5  = TextureLibrary.loadTexture(gl, "textures/wattBook.jpg");
    int[] textureId6  = TextureLibrary.loadTexture(gl, "textures/wattBook_specular.jpg");
    int[] textureId7  = TextureLibrary.loadTexture(gl, "textures/jup0vss1.jpg");
    int[] textureId8  = TextureLibrary.loadTexture(gl, "textures/jup0vss1_specular.jpg");
    int[] textureId9  = TextureLibrary.loadTexture(gl, "textures/mar0kuu2.jpg");
    int[] textureId10 = TextureLibrary.loadTexture(gl, "textures/mar0kuu2_specular.jpg");
    int[] textureId11 = TextureLibrary.loadTexture(gl, "textures/cloud.jpg");

    // light1
    light1 = new PointLight(gl);
    light1.setCamera(camera);
    light1.setPosition(10f, 20f, 15f);

    // light2
    light2 = new PointLight(gl);
    light2.setCamera(camera);
    light2.setPosition(-10f, 8f, 15f);

    // spotlight
    spotLight = new SpotLight(gl);
    spotLight.setCamera(camera);
    spotLight.setScale(lightScale);
    spotLight.setPosition(lampXPosition-(baseHight + lowArmLength)*(float)Math.sin(thetaBL) + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.cos(thetaJ)+0.5f*(headLength+lightScale)*(float)Math.sin(thetaJ), lampYPosition+legHeight+tableHeight+(baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.sin(thetaJ)-0.5f*(headLength+lightScale)*(float)Math.cos(thetaJ), lampZPosition);
    spotLight.setRotateAngle(270 + thetaJ_angle);
    spotLight.setDirection((float)Math.sin(thetaJ), -(float)Math.cos(thetaJ),0);

    // mesh, material, model, shape
    // Cube

    // Cube::(cubeMesh & cubeShader & cubeModelMatrix)
    Mesh cubeMesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    Shader cubeShader = new Shader(gl, "vs_3light.txt", "fs_3light.txt");
    Mat4 cubeModelMatrix = Mat4.multiply(Mat4Transform.scale(1,1,1), Mat4Transform.translate(0,0.5f,0));

     // Cube::wood
     // Cube::(cubeMesh & cubeShader & cubeMaterial) :: woodMaterial
     Material woodMaterial = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
      // Cube::wood::output
      // Cube::(cubeMesh & cubeShader & subeModelMatrix) :: woodMaterial::output
      woodCube = new Light3Model(gl, camera, light1, light2, spotLight, cubeShader, woodMaterial, cubeModelMatrix, cubeMesh, textureId7, textureId8);

     // Cube::metal
     // Cube::(cubeMesh & cubeShader & cubeMaterial) :: metalMaterial
     Material metalMaterial = new Material(new Vec3(0.19225f, 0.19225f, 0.19225f), new Vec3(0.50754f, 0.50754f, 0.50754f), new Vec3(0.508273f, 0.508273f, 0.508273f), 32f*0.4f);
      // Cube::metal::output
      // Cube::(cubeMesh & cubeShader & cubeMaterial) :: metalMaterial::output
      metalCube = new Light3Model(gl, camera, light1, light2, spotLight, cubeShader, metalMaterial, cubeModelMatrix, cubeMesh, textureId9, textureId10);

    // Sphere
    // Sphere::(sphereMesh & sphereShader & sphereModelMatrix)
    Mesh sphereMesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    Shader sphereShader = new Shader(gl, "vs_3light.txt", "fs_3light.txt");
    Mat4 sphereModelMatrix = Mat4.multiply(Mat4Transform.scale(1,1,1), Mat4Transform.translate(0,0.5f,0));

     // Sphere::matelMaterial(had)
      // Sphere::matelMaterial::output
      metalSphere = new Light3Model(gl, camera, light1, light2, spotLight, sphereShader, metalMaterial, sphereModelMatrix, sphereMesh, textureId1, textureId2);


    // addition: wall floor
    // Background mesh
    float floorSize = 40f;
    float windowSize = 20f;
    float wallWidth1 = floorSize;
    float wallHight1 = tableHeight + legHeight;
    float wallWidth2 = (floorSize - windowSize) * 0.5f;
    float wallHight2 = windowSize;
    Mesh backgrdMesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader backgrdShader = new Shader(gl, "vs_bkg.txt", "fs_bkg.txt");
    // Background::floor
      Material floorMaterial = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
      Mat4 floorMatrix = Mat4Transform.scale(1,1f,1);
        floorMatrix = Mat4.multiply(floorMatrix, Mat4Transform.scale(floorSize,1f,floorSize));
          floor = new Light3Model(gl, camera, light1, light2, spotLight, backgrdShader, floorMaterial, floorMatrix, backgrdMesh, textureId1);
    // Background::window
      Material windowMaterial = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
      Mat4 windowMatrix = Mat4Transform.scale(1,1f,1);
        windowMatrix = Mat4.multiply(Mat4Transform.scale(windowSize,1f,windowSize), windowMatrix);
        windowMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), windowMatrix);
        windowMatrix = Mat4.multiply(Mat4Transform.translate(0, wallHight1+windowSize*0.5f,-floorSize*0.5f), windowMatrix);
          window = new Light3Model(gl, camera, light1, light2, spotLight, backgrdShader, windowMaterial, windowMatrix, backgrdMesh, textureId11);
    // Background::wall
      Material wallMaterial = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
      Mat4 wallMatrix = Mat4Transform.scale(1,1f,1);
        wallMatrix = Mat4.multiply(Mat4Transform.scale(wallWidth1,1f,wallHight1), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.translate(0, wallHight1*0.5f, -floorSize*0.5f), wallMatrix);
          wallLow = new Light3Model(gl, camera, light1, light2, spotLight, backgrdShader, wallMaterial, wallMatrix, backgrdMesh, textureId0);

        wallMatrix = new Mat4(1);
        wallMatrix = Mat4.multiply(Mat4Transform.scale(wallWidth1,1f,wallHight1), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.translate(0, wallHight1*1.5f + windowSize, -floorSize*0.5f), wallMatrix);
          wallHigh = new Light3Model(gl, camera, light1, light2, spotLight, backgrdShader, wallMaterial, wallMatrix, backgrdMesh, textureId0);

        wallMatrix = new Mat4(1);
        wallMatrix = Mat4.multiply(Mat4Transform.scale(wallWidth2,1f,wallHight2), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.translate(wallWidth2*0.5f-wallWidth1*0.5f, wallHight1 + windowSize*0.5f, -floorSize*0.5f), wallMatrix);
          wallLeft = new Light3Model(gl, camera, light1, light2, spotLight, backgrdShader, wallMaterial, wallMatrix, backgrdMesh, textureId0);

        wallMatrix = new Mat4(1);
        wallMatrix = Mat4.multiply(Mat4Transform.scale(wallWidth2,1f,wallHight2), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), wallMatrix);
        wallMatrix = Mat4.multiply(Mat4Transform.translate(wallWidth1*0.5f - wallWidth2*0.5f, wallHight1 + windowSize*0.5f, -floorSize*0.5f), wallMatrix);
          wallRight = new Light3Model(gl, camera, light1, light2, spotLight, backgrdShader, wallMaterial, wallMatrix, backgrdMesh, textureId0);

    // table

    tableRoot = new NameNode("tableroot");
    TransformNode tableMoveTranslate = new TransformNode("table transform", Mat4Transform.translate(0,0,0));

    TransformNode tableTranslate = new TransformNode("table transform", Mat4Transform.translate(0,legHeight,0));

    NameNode table = new NameNode("table");
     Mat4 tableM = Mat4Transform.scale(tableLength,tableHeight,tableWidth);
     tableM = Mat4.multiply(tableM, Mat4Transform.translate(0, 0.5f,0));
     TransformNode tableTransform = new TransformNode("table transform", tableM);
      ModelNode tableShape = new ModelNode("Cube(table)", woodCube);

    float legLVariance = (tableLength - legLength) / 2.0f;
    float legWVariance = (tableWidth - legLength) / 2.0f;
    NameNode leg1 = new NameNode("leg1");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(legLVariance,0-legHeight,0+legWVariance));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(legLength,legHeight,legLength));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode leg1Scale = new TransformNode("leg1 scale", tableM);
        ModelNode leg1Shape = new ModelNode("Cube(leg1)", woodCube);

    NameNode leg2 = new NameNode("leg2");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(legLVariance,0-legHeight,0-legWVariance));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(legLength,legHeight,legLength));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode leg2Scale = new TransformNode("leg2 scale", tableM);
        ModelNode leg2Shape = new ModelNode("Cube(leg2)", woodCube);

    NameNode leg3 = new NameNode("leg3");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(-legLVariance,0-legHeight,0-legWVariance));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(legLength,legHeight,legLength));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode leg3Scale = new TransformNode("leg3 scale", tableM);
        ModelNode leg3Shape = new ModelNode("Cube(leg3)", woodCube);

    NameNode leg4 = new NameNode("leg4");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(-legLVariance,0-legHeight,0+legWVariance));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(legLength,legHeight,legLength));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode leg4Scale = new TransformNode("leg4 scale", tableM);
        ModelNode leg4Shape = new ModelNode("Cube(leg4)", woodCube);

    NameNode phone = new NameNode("phone");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0.5f*tableLength-0.25f, tableHeight, 0.5f*tableWidth-0.4f));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(0.5f,0.2f,0.8f));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode phoneTransform = new TransformNode("phone Transform", tableM);
        ModelNode phoneShape = new ModelNode("Cube(phone)", metalCube);

    NameNode studentCard = new NameNode("studentCard");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0.5f*tableLength-2.3f, tableHeight, -0.5f*tableWidth+0.2f));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(0.6f,0.1f,0.4f));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode studentCardTransform = new TransformNode("studentCard Transform", tableM);
        ModelNode studentCardShape = new ModelNode("Cube(phoe)", metalCube);

      NameNode earth = new NameNode("earth");
      tableM = new Mat4(1);
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0.5f*tableLength-1.0f, tableHeight, -(0.5f*tableWidth-2.0f)));
      tableM = Mat4.multiply(tableM, Mat4Transform.scale(2.0f, 2.0f, 2.0f));
      tableM = Mat4.multiply(tableM, Mat4Transform.translate(0,0.5f,0));
      TransformNode earthTransform = new TransformNode("earth Transform", tableM);
        ModelNode earthShape = new ModelNode("Sphere(earth)", metalSphere);

    // lamp

    lampRoot = new NameNode("lampRoot");
    lampMoveTranslate = new TransformNode("lamp transform", Mat4Transform.translate(lampXPosition,lampYPosition,lampZPosition));

    TransformNode lampTranslate = new TransformNode("lamp transform", Mat4Transform.translate(0,legHeight + tableHeight,0));

    NameNode base = new NameNode("base");
      Mat4 lampM = new Mat4(1);
      lampM = Mat4.multiply(lampM, Mat4Transform.scale(baseLength, baseHight, baseLength));
      lampM = Mat4.multiply(lampM, Mat4Transform.translate(0,0.5f,0));
      TransformNode baseTransform = new TransformNode("base transform", lampM);
        ModelNode baseShape = new ModelNode("Cube(base)", metalCube);

    NameNode lowArm = new NameNode("lowArm");
      lowArmMove = new TransformNode("lowarm move", Mat4Transform.translate(0,baseHight,0));
      lowArmRotate = new TransformNode("lowarm rotate", Mat4Transform.rotateAroundZ(thetaBL_angle));
      lampM = new Mat4(1);
      lampM = Mat4.multiply(lampM, Mat4Transform.scale(lowArmScale, lowArmLength, lowArmScale));
      lampM = Mat4.multiply(lampM, Mat4Transform.translate(0,0.5f,0));
      TransformNode lowArmTransform = new TransformNode("lowArm transform", lampM);
        ModelNode lowArmShape = new ModelNode("Sphere(lowArm)", metalSphere);

    NameNode junction = new NameNode("junction");
      junctionMove = new TransformNode("junction Move", Mat4Transform.translate(-(baseHight + lowArmLength)*(float)Math.sin(thetaBL), (baseHight + lowArmLength)*(float)Math.cos(thetaBL), 0));
      lampM = new Mat4(1);
      lampM = Mat4.multiply(lampM, Mat4Transform.scale(junctionScale, junctionScale, junctionScale));
      lampM = Mat4.multiply(lampM, Mat4Transform.translate(0,0.5f,0));
      TransformNode junctionTransform = new TransformNode("junction transform", lampM);
        ModelNode junctionShape = new ModelNode("Sphere(junction)", metalSphere);

    NameNode highArm = new NameNode("highArm");
      highArmMove = new TransformNode("higharm move", Mat4Transform.translate(-(baseHight + lowArmLength)*(float)Math.sin(thetaBL), (baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale, 0));
      highArmRotate = new TransformNode("higharm rotate", Mat4Transform.rotateAroundZ(270 + thetaJ_angle));
      lampM = new Mat4(1);
      lampM = Mat4.multiply(lampM, Mat4Transform.scale(highArmScale, highArmLength, highArmScale));
      lampM = Mat4.multiply(lampM, Mat4Transform.translate(0,0.5f,0));
      TransformNode highArmTransform = new TransformNode("highArm transform", lampM);
        ModelNode highArmShape = new ModelNode("Sphere(highArm)", metalSphere);

    NameNode headL = new NameNode("headL");
      headLMove = new TransformNode("headL move", Mat4Transform.translate(-(baseHight + lowArmLength)*(float)Math.sin(thetaBL) + highArmLength * (float)Math.cos(thetaJ), (baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale + highArmLength * (float)Math.sin(thetaJ), 0));
      headLRotate = new TransformNode("headL rotate", Mat4Transform.rotateAroundZ(270 + thetaJ_angle));
      lampM = new Mat4(1);
      lampM = Mat4.multiply(lampM, Mat4Transform.scale(headLength, headHight, headHight));
      lampM = Mat4.multiply(lampM, Mat4Transform.translate(0,0.5f,0));
      TransformNode headLTransform = new TransformNode("headL transform", lampM);
        ModelNode headLShape = new ModelNode("Sphere(headL)", metalCube);

    // Draw lamp graph and update
    lampRoot.addChild(lampMoveTranslate);
      lampMoveTranslate.addChild(lampTranslate);
        lampTranslate.addChild(base);
          base.addChild(baseTransform);
            baseTransform.addChild(baseShape);
          base.addChild(lowArm);
              lowArm.addChild(lowArmMove);
              lowArmMove.addChild(lowArmRotate);
              lowArmRotate.addChild(lowArmTransform);
              lowArmTransform.addChild(lowArmShape);
          lowArm.addChild(junction);
            junction.addChild(junctionMove);
            junctionMove.addChild(junctionTransform);
            junctionTransform.addChild(junctionShape);
          junction.addChild(highArm);
            highArm.addChild(highArmMove);
            highArmMove.addChild(highArmRotate);
            highArmRotate.addChild(highArmTransform);
            highArmTransform.addChild(highArmShape);
          highArm.addChild(headL);
            headL.addChild(headLMove);
            headLMove.addChild(headLRotate);
            headLRotate.addChild(headLTransform);
            headLTransform.addChild(headLShape);

    // Draw table graph and update
    tableRoot.addChild(tableMoveTranslate);
      tableMoveTranslate.addChild(tableTranslate);
        tableTranslate.addChild(table);
          table.addChild(tableTransform);
            tableTransform.addChild(tableShape);
	        table.addChild(leg1);
	          leg1.addChild(leg1Scale);
	          leg1Scale.addChild(leg1Shape);
          table.addChild(leg2);
	          leg2.addChild(leg2Scale);
	          leg2Scale.addChild(leg2Shape);
	        table.addChild(leg3);
	          leg3.addChild(leg3Scale);
	          leg3Scale.addChild(leg3Shape);
	        table.addChild(leg4);
	          leg4.addChild(leg4Scale);
	          leg4Scale.addChild(leg4Shape);
          table.addChild(phone);
            phone.addChild(phoneTransform);
            phoneTransform.addChild(phoneShape);
          table.addChild(studentCard);
            studentCard.addChild(studentCardTransform);
            studentCardTransform.addChild(studentCardShape);
          table.addChild(earth);
            earth.addChild(earthTransform);
            earthTransform.addChild(earthShape);

    lampRoot.update();
    tableRoot.update();

    //robotRoot.print(0, false);
    //System.exit(0);
  }

  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    light1.render(gl);
    light2.render(gl);
    spotLight.render(gl);
    floor.render(gl);
    wallLow.render(gl);
    wallHigh.render(gl);
    wallLeft.render(gl);
    wallRight.render(gl);
    window.render(gl);
    if (jump) randomJump();
    if (lampif) lampSwitch();
    if (world1if) worldSwitch1();
    if (world2if) worldSwitch2();
    tableRoot.draw(gl);
    lampRoot.draw(gl);
  }

  // The light1's postion is continually being changed, so needs to be calculated for each frame.
  private Vec3 getLightPosition(float yPos) {
    double elapsedTime = getSeconds()-startTime;
    float x = 10.0f*(float)(Math.sin(Math.toRadians(elapsedTime*50)));
    float y = yPos;
    float z = 10.0f*(float)(Math.cos(Math.toRadians(elapsedTime*50)));
    return new Vec3(x,y,z);
    //return new Vec3(5f,3.4f,5f);
  }

  // jump animation
  public void randomJump() {
    double elapsedTime = getSeconds() - startTime;

    if (elapsedTime <= (double)(Math.PI / 2.0))
    {
      lampXPosition = maxXPosition*(float)Math.sin(elapsedTime) + currentXPosition;
      lampYPosition = maxYPosition*(float)Math.sin(elapsedTime * 2.0) + currentYPosition;
      lampZPosition = maxZPosition*(float)Math.sin(elapsedTime) + currentZPosition;
    }
    else
    {
      jump = false;
    }

    spotLight.setScale(lightScale);
    spotLight.setPosition(lampXPosition-(baseHight + lowArmLength)*(float)Math.sin(thetaBL) + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.cos(thetaJ)+0.5f*(headLength+lightScale)*(float)Math.sin(thetaJ), lampYPosition+legHeight+tableHeight+(baseHight + lowArmLength)*(float)Math.cos(thetaBL)+junctionScale + (highArmLength+0.5f*headHight-0.5f*lightScale) * (float)Math.sin(thetaJ)-0.5f*(headLength+lightScale)*(float)Math.cos(thetaJ), lampZPosition);
    spotLight.setRotateAngle(270 + thetaJ_angle);
    spotLight.setDirection((float)Math.sin(thetaJ), -(float)Math.cos(thetaJ),0);
    lampMoveTranslate.setTransform(Mat4Transform.translate(lampXPosition,lampYPosition,lampZPosition));
    lampMoveTranslate.update();
  }

  public void lampSwitch() {
    spotLight.setSwitch(switchLampState);
    shapeLampState(switchLampState);
    lampRoot.update();
    tableRoot.update();
    lampif = false;
  }

  public void worldSwitch1() {
    light1.setSwitch(switchWorldState1);
    shapeWorldState(switchWorldState1, light1);
    lampRoot.update();
    tableRoot.update();
    world1if = false;
  }

  public void worldSwitch2() {
    light2.setSwitch(switchWorldState2);
    shapeWorldState(switchWorldState2, light2);
    lampRoot.update();
    tableRoot.update();
    world2if = false;
  }

  public void shapeLampState(float switchState) {
    floor.setSwitchLampState(switchState);
    window.setSwitchLampState(switchState);
    woodCube.setSwitchLampState(switchState);
    metalCube.setSwitchLampState(switchState);
    metalSphere.setSwitchLampState(switchState);
    wallLow.setSwitchLampState(switchState);
    wallHigh.setSwitchLampState(switchState);
    wallLeft.setSwitchLampState(switchState);
    wallRight.setSwitchLampState(switchState);
  }

  public void shapeWorldState(float switchState, PointLight light) {
    floor.setSwitchWorldState(switchState, light);
    window.setSwitchWorldState(switchState, light);
    woodCube.setSwitchWorldState(switchState, light);
    metalCube.setSwitchWorldState(switchState, light);
    metalSphere.setSwitchWorldState(switchState, light);
    wallLow.setSwitchWorldState(switchState, light);
    wallHigh.setSwitchWorldState(switchState, light);
    wallLeft.setSwitchWorldState(switchState, light);
    wallRight.setSwitchWorldState(switchState, light);
  }

  // ***************************************************
  /* TIME
   */

  private double startTime;

  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }

  // ***************************************************
  /* An array of random numbers
   */

  private int NUM_RANDOMS = 1000;
  private float[] randoms;

  private void createRandomNumbers() {
    randoms = new float[NUM_RANDOMS];
    for (int i=0; i<NUM_RANDOMS; ++i) {
      randoms[i] = (float)Math.random();
    }
  }

}
