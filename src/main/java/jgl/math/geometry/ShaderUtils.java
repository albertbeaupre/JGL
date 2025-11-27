package jgl.math.geometry;

import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;

public class ShaderUtils {

    public static int createShader(String vert, String frag) {
        int vs = compile(GL33.GL_VERTEX_SHADER, vert);
        int fs = compile(GL33.GL_FRAGMENT_SHADER, frag);

        int prog = GL33.glCreateProgram();
        GL33.glAttachShader(prog, vs);
        GL33.glAttachShader(prog, fs);
        GL33.glLinkProgram(prog);

        GL33.glDeleteShader(vs);
        GL33.glDeleteShader(fs);
        return prog;
    }

    private static int compile(int type, String src) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);
        return id;
    }

    public static int loadShader(String path, int type) {
        String src = readFile(path);
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " +
                    glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public static int createProgram(String vertPath, String fragPath) {
        int vs = loadShader(vertPath, GL_VERTEX_SHADER);
        int fs = loadShader(fragPath, GL_FRAGMENT_SHADER);

        int program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " +
                    glGetProgramInfoLog(program));
        }

        glDeleteShader(vs);
        glDeleteShader(fs);

        return program;
    }

    private static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("Failed reading shader: " + path, e);
        }
    }
}
