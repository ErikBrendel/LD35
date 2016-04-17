package sounds;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class SoundManager {
	/** Buffers hold sound data. */
	IntBuffer[] buffer;

	/** Sources are points emitting sound. */
	IntBuffer[] source;

	/** Position of the source sound. */
	FloatBuffer sourcePos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).flip();

	/** Velocity of the source sound. */
	FloatBuffer sourceVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).flip();

	ArrayList<WaveData> waves;

	private int count;
	private HashMap<String, Integer> loadedSounds;

	public SoundManager() {

		// Initialize OpenAL and clear the error bit.
		try {
			AL.create();
		} catch (LWJGLException le) {
			le.printStackTrace();
			return;
		}

		waves = new ArrayList<>();
		loadedSounds = new HashMap<>();
		count = 0;

		AL10.alGetError();

		FloatBuffer pos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0, 0, 0 }).flip();

		FloatBuffer ori = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[] { 0, 0, 1, 0, 1, 0 }).flip();

		FloatBuffer vel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0, 0, 0 }).flip();
		setListenerValues(pos, ori, vel);
	}

	/**
	 * boolean LoadALData()
	 *
	 * This function will load our sample data from the disk using the Alut
	 * utility and send the data into OpenAL as a buffer. A source is then also
	 * created to play that buffer.
	 */

	public void playSound(String name) {
		if (!loadedSounds.containsKey(name)) {
			System.out.println(name);
			loadedSounds.put(name, count);
			WaveData waveFile = WaveData.create("audio/" + name + ".wav");
			waves.add(waveFile);
			count++;

			/** Buffers hold sound data. */
			buffer = new IntBuffer[count];

			for (int i = 0; i < count; i++) {
				buffer[i] = BufferUtils.createIntBuffer(1);
				AL10.alGenBuffers(buffer[i]);
				AL10.alBufferData(buffer[i].get(0), waves.get(i).format, waves.get(i).data, waves.get(i).samplerate);
			}

			/** Sources are points emitting sound. */
			source = new IntBuffer[count];

			for (int i = 0; i < count; i++) {
				source[i] = BufferUtils.createIntBuffer(1);

				AL10.alGenSources(source[i]);
				if (AL10.alGetError() != AL10.AL_NO_ERROR) {
					return;
				}

				final int j = i;
				char firstLetter = loadedSounds.keySet().stream().filter((String n) -> loadedSounds.get(n) == j).findFirst().get().charAt(0);

				if (firstLetter == 'e') {
					AL10.alSourcei(source[i].get(0), AL10.AL_BUFFER, buffer[i].get(0));
					AL10.alSourcef(source[i].get(0), AL10.AL_PITCH, 1.0f);
					AL10.alSourcef(source[i].get(0), AL10.AL_GAIN, 1.0f);
					AL10.alSource(source[i].get(0), AL10.AL_POSITION, sourcePos);
					AL10.alSource(source[i].get(0), AL10.AL_VELOCITY, sourceVel);
					AL10.alSourcei(source[i].get(0), AL10.AL_LOOPING, AL10.AL_FALSE);
				} else if (firstLetter == 'm') {
					AL10.alSourcei(source[i].get(0), AL10.AL_BUFFER, buffer[i].get(0));
					AL10.alSourcef(source[i].get(0), AL10.AL_PITCH, 1.0f);
					AL10.alSourcef(source[i].get(0), AL10.AL_GAIN, 1.0f);
					AL10.alSource(source[i].get(0), AL10.AL_POSITION, sourcePos);
					AL10.alSource(source[i].get(0), AL10.AL_VELOCITY, sourceVel);
					AL10.alSourcei(source[i].get(0), AL10.AL_LOOPING, AL10.AL_TRUE);
				}
			}
		}
		//AL10.alSourcePlay(source[loadedSounds.get(name)].get(0));
	}

	void setListenerValues(FloatBuffer pos, FloatBuffer vel, FloatBuffer ori) {
		AL10.alListener(AL10.AL_POSITION, pos);
		AL10.alListener(AL10.AL_VELOCITY, vel);
		AL10.alListener(AL10.AL_ORIENTATION, ori);
	}

	void killALData() {
		for (int i = 0; i < count; i++) {
			AL10.alDeleteSources(source[i]);
			AL10.alDeleteBuffers(buffer[i]);
		}
	}

	public void destroy() {
		killALData();
		AL.destroy();
	}
}