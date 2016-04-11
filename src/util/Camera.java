package util;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.lwjgl.opengl.GL20.glUniform3f;
import org.lwjgl.util.vector.Vector3f;
import static util.Util.lookAt;
import static util.Util.vCross;
import static util.Util.vMinus;
import static util.Util.vPlus;
import static util.Util.vScale;

/**
 *
 * @author Erik
 */
public class Camera {

    enum CameraMovement {
        FORAWRD, BACKWARD, LEFT, RIGHT, UP, DOWN
    }

    private Vector3f position, front, up, right, worldUp;
    private float yaw, pitch;
    private float movementSpeed, mouseSensitivity, zoom;

    public void restoreDefault() {
        position = new Vector3f(0, 0, 0);
        up = new Vector3f(0, 1, 0);
        yaw = -90;
        pitch = 0;
        front = new Vector3f(0, 0, -1);
        movementSpeed = 3f;
        mouseSensitivity = 0.25f;
        zoom = 45;
    }

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        restoreDefault();
        this.position = position;
        this.worldUp = up;
        this.yaw = yaw;
        this.pitch = pitch;
        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        return lookAt(position, vPlus(position, front), up);
    }

    public Matrix4f getViewMatrix2() {
        Vector3f pos2 = new Vector3f(position);
        pos2 = vPlus(pos2, vScale(right, 0.1f));
        return lookAt(pos2, vPlus(pos2, front), up);
    }

    public void processKeyboard(CameraMovement direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;
        if (direction == CameraMovement.FORAWRD) {
            position = vPlus(position, vScale(front, velocity));
        } else if (direction == CameraMovement.BACKWARD) {
            position = vMinus(position, vScale(front, velocity));
        } else if (direction == CameraMovement.LEFT) {
            position = vMinus(position, vScale(right, velocity));
        } else if (direction == CameraMovement.RIGHT) {
            position = vPlus(position, vScale(right, velocity));
        } else if (direction == CameraMovement.UP) {
            position = vPlus(position, vScale(up, velocity));
        } else if (direction == CameraMovement.DOWN) {
            position = vMinus(position, vScale(up, velocity));
        }
    }

    public void processMouseMovement(float offsetX, float offsetY) {
        float zoomSensRepair = zoom / 45f;
        offsetX *= mouseSensitivity * zoomSensRepair;
        offsetY *= mouseSensitivity * zoomSensRepair;
        yaw += offsetX;
        pitch += offsetY;
        if (pitch > 89) {
            pitch = 89;
        } else if (pitch < -89) {
            pitch = -89;
        }
        updateCameraVectors();
    }

    public void processMouseScroll(float yoffset) {
        yoffset = (float) Math.pow(1.08, -yoffset);
        zoom *= yoffset;

        if (zoom <= 1.0f) {
            zoom = 1.0f;
        } else if (zoom >= 70.0f) {
            zoom = 70.0f;
        }
    }

    public float getFOV() {
        return zoom;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getDirection() {
        return front;
    }
    
    

    public void setUniformToPosition(Shader shader, String uniform) {
        glUniform3f(shader.getUniform(uniform), position.x, position.y, position.z);
    }

    public void setUniformToDirection(Shader shader, String uniform) {
        glUniform3f(shader.getUniform(uniform), front.x, front.y, front.z);
    }

    private void updateCameraVectors() {
        //front vector
        Vector3f front = new Vector3f(0, 0, 0);
        front.x = (float) (cos(toRadians(yaw)) * cos(toRadians(pitch)));
        front.y = (float) sin(toRadians(pitch));
        front.z = (float) (sin(toRadians(yaw)) * cos(toRadians(pitch)));
        front = (Vector3f) front.normalise();
        this.front = front;

        //right vector
        Vector3f right = vCross(this.front, this.worldUp);
        right = (Vector3f) right.normalise();
        this.right = right;

        //up vector
        Vector3f up = vCross(this.right, this.front);
        up = (Vector3f) up.normalise();
        this.up = up;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
    
    

}
