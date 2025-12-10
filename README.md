# JGL — Java Game Library

Stop wiring GLFW every time. JGL is the smallest useful layer on top of LWJGL 3 that gets you from “empty desktop” to a running OpenGL window with input, timing, events, and audio — in one line — while keeping full access to raw LWJGL whenever you want it.

Why is JGL awesome (vs. “just LWJGL” or a heavy engine)?
- Start instantly: `JGL.init(app, title, w, h)` creates the window/context, boots input (keyboard/mouse), time/FPS, and OpenAL audio — no boilerplate wall
- Stay in control: explicit lifecycle (`init`, `update(delta)`, `render`, `dispose`) that fits any architecture and doesn’t hide LWJGL/OpenGL
- Code that scales: a typed, priority‑ordered event bus with listener filtering for clean cross‑cutting behavior
- Input that feels right: simple, stateful queries (`Keyboard.isKeyDown`, `Mouse.getX/Y`, scroll, modifiers, key→char)
- Audio without yak‑shaving: `Audio.load("file.ext")` → `SoundPlayer.play()` with loop/volume/pause/seek (wav/ogg/mp3)
- Tiny and transparent: import only what you need, call GLFW/OpenGL directly inside your loop — zero lock‑in

This README covers the entire project: windowing, lifecycle, input, events, audio, and a quick overview of additional packages.

## Why JGL (the short version)

- Without JGL (raw LWJGL): you hand‑roll the window, context, callbacks, input state, timing/FPS, audio device/context, and cleanup every project.
- With heavy engines: you get features you don’t need, constrained lifecycles, and opaque abstractions that fight low‑level control.
- With JGL: you skip the ceremony, keep the metal. Use our lifecycle, input, events, and audio — and dip straight into GLFW/OpenGL anytime.

Show, don’t tell — audio + texture + font in one tiny app:
```java
import jgl.*;                                // lifecycle, input, timing
import jgl.event.events.KeyPressEvent;       // event example
import jgl.sound.SoundPlayer;                // audio
import jgl.graphics.texture.Texture;         // texture sprite
import jgl.graphics.font.Font;               // bitmap font renderer
import jgl.graphics.font.FontData;           // font atlas data

public class Demo implements Application {
    private SoundPlayer music;
    private Texture logo;
    private Font uiText;

    @Override public void init() {
        Window.setTitle("JGL Demo");

        // Hotkey example: Ctrl+S → change title
        JGL.subscribe(KeyPressEvent.class, e -> {
            if (e.isCtrlDown() && (e.getChar() == 's' || e.getChar() == 'S'))
                Window.setTitle("Saved!");
        });

        // Audio: load and loop a short track
        music = Audio.load("assets/loop.ogg");
        music.setLooping(true);
        music.play();

        // Texture: load a sprite and place it
        logo = new Texture("assets/logo.png");
        logo.setPosition(32, 32);

        // Font: build atlas and render text
        FontData data = FontData.load("assets/Roboto-Regular.ttf", 24, 32, 96);
        uiText = new Font(data);
        uiText.setText("Hello, JGL! Press ESC to show FPS");
        uiText.setPosition(32, 128);
    }

    @Override public void update(float dt) {
        // Simple input + timing: show FPS when ESC (GLFW 256) is held
        if (Keyboard.isKeyDown(256))
            Window.setTitle("ESC – FPS=" + JGL.getFramesPerSecond());
    }

    @Override public void render() {
        logo.draw();
        uiText.draw();
    }

    @Override public void dispose() {
        if (music != null) music.dispose();
        if (logo != null) logo.dispose();
        if (uiText != null) uiText.dispose();
    }

    public static void main(String[] a){ JGL.init(new Demo(), "JGL", 1280, 720); }
}
```

## Why it’s better in practice

- Less glue code, fewer bugs: Lifecycle + input + timing + audio are solved once and battle‑tested; you focus on your game loop.
- Events that stay readable: Subscribe per type, optional `canHandle` filtering, and priority ordering via annotations.
- Direct metal access: Nothing stops you from calling GLFW/OpenGL/NanoVG directly where it makes sense.
- Predictable performance: event dispatch calls listeners directly (no reflection per publish); input state is polled once per frame; timing and FPS come ready‑made.

## Requirements

- Java 23 (toolchain configured in `build.gradle`)
- Gradle (wrapper included) or Maven
- LWJGL 3.x (GLFW, OpenGL, STB, NanoVG, OpenAL; managed via BOM)

## Getting Started

You already saw a minimal, working example above. Use that as your starting point.

### Run

- Using Gradle wrapper: `./gradlew run` (Linux/Mac) or `gradlew.bat run` (Windows)
- Or run your `main` class directly from your IDE

## Core Concepts and APIs

### Lifecycle: `JGL` and `Application`

- Implement `Application` with: `void init()`, `void update(float delta)`, `void render()`, `void dispose()`
- Start your app with `JGL.init(application, title, width, height)`
- Query timing: `JGL.getDeltaTime()`, `JGL.getFramesPerSecond()`

### Window: `Window`

- Create and manage the window/context automatically via `JGL.init(...)`
- Control window state:
  - `Window.setTitle(String)`
  - `Window.setSize(int width, int height)`
  - `Window.setFullscreen(boolean)`
  - `Window.setSwapInterval(SwapInterval)` and `Window.getSwapInterval()`
  - Query dimensions/position: `getWidth()`, `getHeight()`, `getX()`, `getY()`, `getFramebufferSize()`

### Input: `Keyboard` and `Mouse`

- Keyboard:
  - `Keyboard.isKeyDown(int key)`
  - Modifiers: `isShiftDown()`, `isCtrlDown()`, `isAltDown()`, `isSuperDown()`
  - `Keyboard.getKeyChar(int key)` for character mapping
- Mouse:
  - Position: `Mouse.getX()`, `Mouse.getY()`
  - Buttons: `Mouse.isButtonDown(int button)`
  - Scroll: `Mouse.getScrollX()`, `Mouse.getScrollY()`, and `Mouse.resetScroll()` per frame

### Events: Publish/Subscribe

JGL includes a simple event bus:

- Subscribe: `JGL.subscribe(EventType.class, listener)`
- Publish: `JGL.publish(event)`
- Listeners implement `EventListener<E>` and optionally override `canHandle(E)` for filtering

Example: keyboard shortcut (Ctrl+S) with filtering

```java
import jgl.JGL;
import jgl.Window;
import jgl.event.EventListener;
import jgl.event.events.KeyPressEvent;

public final class SaveShortcutExample {
    public static void register() {
        // Listen for Ctrl+S using canHandle filtering
        JGL.subscribe(KeyPressEvent.class, new EventListener<KeyPressEvent>() {
            @Override public boolean canHandle(KeyPressEvent e) {
                char c = e.getChar();
                return e.isCtrlDown() && (c == 's' || c == 'S');
            }

            @Override public void handle(KeyPressEvent e) {
                Window.setTitle("Save triggered (Ctrl+" + Character.toUpperCase(e.getChar()) + ")");
                // performSave(); // your save logic here
            }
        });
    }
}
```

You can also create strongly-typed listener interfaces such as `jgl.event.listeners.WindowResizeListener`.

### Audio: `Audio` and `SoundPlayer`

- JGL initializes OpenAL internally during `JGL.init(...)`
- Load sounds:
  - `SoundPlayer s = Audio.load("file.ext")` (wav/ogg/mp3 supported)
  - `SoundPlayer s = Audio.load(byte[])`
- Control playback via `SoundPlayer`:
  - `play()`, `pause()`, `resume()`, `stop()`, `rewind()`
  - `setLooping(boolean)`, `setVolume(float)`, `setPitch(float)`
  - Query `isPlaying()`, `isPaused()`, `isStopped()`, `duration()`, `getCurrentTime()`
  - Seek with `setCurrentTime(float seconds)`
  - Always call `dispose()` when done

## Package Overview

The project is organized into modules under `src/main/java/jgl`:

- `jgl` — Core (`JGL`, `Application`, `Window`, `Audio`, `Keyboard`, `Mouse`)
- `jgl.event` — Event system (publisher, listener, priorities, events like `WindowResizeEvent`)
- `jgl.sound` — Decoders and playback (`SoundPlayer`, `SoundData`, `WaveDecoder`, `OggDecoder`, `Mp3Decoder`)
- `jgl.graphics` — Graphics helpers (shaders, textures, fonts, etc.)
- `jgl.math` — Math and geometry utilities
- `jgl.io` — I/O helpers and pools
- `jgl.collections` — Arrays, stacks, queues, trees, bitsets
- `jgl.viewport`, `jgl.camera`, `jgl.ui`, `jgl.utility`, `jgl.plugin`, and more

Note: Some packages provide building blocks and utilities intended to be used directly or extended within your app; explore the source for further details.

## Building and Running This Project

This repository includes a Gradle build.

- Build: `./gradlew build`
- Run: `./gradlew run`

## Tips & Notes

- Ensure native LWJGL binaries match your OS (`natives-windows`, `natives-linux`, or `natives-macos`)
- Manage assets (audio/images/shaders) on the runtime classpath or via absolute/relative paths accessible at runtime
- Always dispose resources you create (`SoundPlayer.dispose()`, any GL objects you allocate in graphics helpers)

## Next updates...

1. Shaders 
2. Shape Drawing 
3. User Interface
---

