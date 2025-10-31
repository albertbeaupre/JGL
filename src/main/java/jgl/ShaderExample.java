package jgl;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ShaderExample implements Application {

    private int vao;
    private int vbo;
    private int shaderProgram;

    public static void main(String[] args) {
        JGL.init(new ShaderExample(), "Shader Example", 800, 600);
    }

    @Override
    public void init() {
        // Vertex data (a simple triangle)
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.0f,  0.5f, 0.0f
        };

        // === 1️⃣ Create VAO & VBO ===
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // === 2️⃣ Create & Compile Shaders ===
        int vertexShader = createShader(GL_VERTEX_SHADER, """
            #version 330 core
            layout (location = 0) in vec3 aPos;

            void main() {
                gl_Position = vec4(aPos, 1.0);
            }
        """);

        int fragmentShader = createShader(GL_FRAGMENT_SHADER, """
            #version 330 core
            out vec4 FragColor;

            void main() {
                FragColor = vec4(0.2, 0.7, 1.0, 1.0);
            }
        """);

        // === 3️⃣ Link Program ===
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        // Check linking
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Program linking failed: " + glGetProgramInfoLog(shaderProgram));

        // Shaders can be deleted after linking
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    @Override
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        // === 4️⃣ Use Shader Program ===
        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteProgram(shaderProgram);
    }

    // === Utility Method to Create a Shader ===
    private static int createShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        // Check for errors
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(shader));

        return shader;
    }
}
