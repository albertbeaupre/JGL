package jgl.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 * Immutable container holding OpenGL texture information and its decoded width/height.
 *
 * <p>This record acts as a lightweight wrapper around a created OpenGL texture.
 * It stores the OpenGL-generated texture ID along with the pixel dimensions obtained
 * from STBImage. No rendering functionality is included—only loading and metadata.</p>
 *
 * <p>The texture loading pipeline performs the following operations:</p>
 * <ol>
 *     <li>Converts raw bytes into a direct {@link ByteBuffer}</li>
 *     <li>Decodes image data using {@link STBImage#stbi_load_from_memory}</li>
 *     <li>Creates an OpenGL texture via {@code glGenTextures()}</li>
 *     <li>Uploads RGBA8 data to GPU memory</li>
 *     <li>Applies texture parameters (min/mag filter, wrap mode)</li>
 *     <li>Frees decoded image memory</li>
 * </ol>
 *
 * <p>This class always forces the loaded image into 4-channel RGBA format for consistency.
 * STBImage is configured to vertically flip textures to match OpenGL UV orientation.</p>
 *
 * @param ID     the OpenGL texture handle returned from {@code glGenTextures()}
 * @param width  the width of the decoded texture in pixels
 * @param height the height of the decoded texture in pixels
 * @author Albert Beaupre
 * @since November 11th, 2025
 */
public record TextureData(int ID, short width, short height) {
    
    private static final List<String> l = List.of();

    /**
     * Loads a texture from a file path. This is a convenience wrapper around
     * {@link #loadTexture(byte[])} which retrieves the raw file bytes first.
     *
     * @param path file system path to an image
     * @return a fully constructed {@link TextureData} instance
     * @throws RuntimeException if file reading fails or image decoding fails
     */
    public static TextureData loadTexture(String path) {
        try {
            return loadTexture(Files.readAllBytes(Path.of(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a texture from raw image bytes, decodes it, and uploads it to OpenGL.
     *
     * <p>This method handles:</p>
     * <ul>
     *     <li>ByteBuffer preparation</li>
     *     <li>STB image decoding</li>
     *     <li>Texture creation</li>
     *     <li>Texture parameter configuration</li>
     *     <li>GL texture upload</li>
     * </ul>
     *
     * @param data raw PNG/JPEG/etc. bytes
     * @return a new {@link TextureData} containing the GPU texture ID and pixel size
     * @throws RuntimeException if STBImage fails to decode the image
     */
    public static TextureData loadTexture(byte[] data) {
        // Convert raw data into a direct buffer required by STBImage.
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
        buffer.put(data).flip();

        // Buffers for width, height, and channel count returned by STBImage.
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);

        // Flip vertically so UVs match OpenGL's coordinate expectations.
        STBImage.stbi_set_flip_vertically_on_load(true);

        // Attempt to decode the image into RGBA format (4 channels).
        ByteBuffer image = STBImage.stbi_load_from_memory(buffer, w, h, comp, 4);
        if (image == null) throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());

        int width = w.get(0);
        int height = h.get(0);

        // Generate an OpenGL texture object.
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Configure texture filtering for minification and magnification.
        // Linear filtering gives smoother results for scaled textures.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Clamp texture edges to prevent bleeding when sampling near borders.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Upload the pixel data to the GPU using 8-bit RGBA format.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

        // Free the CPU-side decoded image buffer.
        STBImage.stbi_image_free(image);

        return new TextureData(textureID, (short) width, (short) height);
    }
}
