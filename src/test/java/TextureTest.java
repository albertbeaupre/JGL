import jgl.Application;
import jgl.JGL;
import jgl.Window;
import jgl.graphics.Texture;
import jgl.graphics.TextureData;
import jgl.graphics.TextureFilter;
import jgl.viewport.ScreenViewport;

public class TextureTest implements Application {

    private Texture texture;
    private ScreenViewport viewport;

    private float time = 0f;

    @Override
    public void init() {
        viewport = new ScreenViewport(Window.getWidth(), Window.getHeight());

        // Load texture ONCE
        TextureData texData = TextureData.loadTexture("./src/test/resources/pixel-art.png");
        texture = new Texture(texData);

        ///texture.setFilter(TextureFilter.LINEAR);
        texture.setFilter(TextureFilter.NEAREST); // try this too for pixel-art look

        texture.setFlip(false, false);
        texture.setScale(1f, 1f); // normal size
        texture.setOriginCenter();
    }

    @Override
    public void update(float delta) {
        viewport.update(Window.getWidth(), Window.getHeight());
        Window.setTitle("FPS: " + JGL.getFramesPerSecond());
        time += delta;

        // Animate scale to verify scaleX/scaleY work
        float s = 1f + (float) Math.sin(time) * 0.5f; // range 0.5 → 1.5
        texture.setScale(s, s);
        // Toggle flipping every 2 seconds to test flip logic
        texture.setFlipX((int) (time * 0.5f) % 2 == 0);
        texture.setFlipY((int) (time * 0.25f) % 2 == 0);
        texture.setRotation(texture.getRotation() + delta * 100f);
    }

    @Override
    public void render() {
        viewport.render(() -> {

            float w = texture.getWidth() * texture.getScaleX();
            float h = texture.getHeight() * texture.getScaleY();
            texture.setPosition(Window.getWidth() / 2f - w / 2f, Window.getHeight() / 2f - h / 2f);

            texture.draw();
        });
    }

    @Override
    public void dispose() {

    }

    public static void main(String[] args) {
        JGL.init(new TextureTest(), "Texture Feature Test", 1280, 720);
    }
}
