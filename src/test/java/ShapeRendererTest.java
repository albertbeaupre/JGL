import jgl.Application;
import jgl.JGL;
import jgl.Window;
import jgl.camera.OrthographicCamera2D;
import jgl.graphics.Color;
import jgl.math.geometry.*;
import jgl.viewport.ScreenViewport;
import jgl.viewport.Viewport;

import static org.lwjgl.opengl.GL11.glClearColor;

public class ShapeRendererTest implements Application {

    private ShapeRenderer renderer;
    private Viewport viewport;

    @Override
    public void init() {
        this.renderer = new ShapeRenderer();
        this.viewport = new ScreenViewport(Window.getWidth(), Window.getHeight());
        this.viewport.setCamera(new OrthographicCamera2D());
        this.viewport.getCamera().setCenter(Window.getWidth() / 2f, Window.getHeight() / 2f);

        Rectangle rectangle = new Rectangle(150, 150, 100, 100);
        rectangle.setRadius(5);
        rectangle.setBorder(new Border(Color.GREEN, 3));
        rectangle.setColor(Color.BLUE);

        Circle circle = new Circle(150, 150, 50);
        circle.setBorder(new Border(Color.RED, 3));
        circle.setColor(Color.YELLOW);

        Line line = new Line(1, 250, 250, 350, 350);
        line.setBorder(new Border(Color.BLUE, 3));
        line.setColor(Color.ORANGE);

        Triangle triangle = new Triangle(150, 150, 200, 250, 250, 150);
        triangle.setColor(Color.YELLOW);
        triangle.setBorder(new Border(Color.CORAL, 3));

        this.renderer.add(line);
        this.renderer.add(rectangle);
        this.renderer.add(circle);
        this.renderer.add(triangle);
    }

    @Override
    public void render() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        this.renderer.render();
    }

    @Override
    public void update(float delta) {
        viewport.update(Window.getWidth(), Window.getHeight());
        Window.setTitle("FPS: " + JGL.getFramesPerSecond());
    }

    @Override
    public void dispose() {

    }

    public static void main(String[] args) {
        JGL.init(new ShapeRendererTest(), "Shape Renderer Test", 1280, 720);
    }
}
