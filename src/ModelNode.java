import com.jogamp.opengl.*;

public class ModelNode extends SGNode {

  protected Light3Model model;

  public ModelNode(String name, Light3Model m) {
    super(name);
    model = m;
  }

  public void draw(GL3 gl) {
    model.render(gl, worldTransform);
    for (int i=0; i<children.size(); i++) {
      children.get(i).draw(gl);
    }
  }

}
