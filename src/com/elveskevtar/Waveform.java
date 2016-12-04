package com.elveskevtar;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Waveform {

	private AudioInputStream audioInputStream;
	private File file;

	private byte[] bytes;
	private int sampleSize;
	private int numChannels;
	private int frameLength;
	private int frameSize;
	private int bitRate;

	public Waveform(File file) {
		this.file = file;
		audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		this.sampleSize = audioInputStream.getFormat().getSampleSizeInBits();
		this.numChannels = audioInputStream.getFormat().getChannels();
		this.frameLength = (int) audioInputStream.getFrameLength();
		this.frameSize = (int) audioInputStream.getFormat().getFrameSize();
		this.bitRate = (int) audioInputStream.getFormat().getFrameRate();
		System.out.print("Sample Size (bits): " + sampleSize + "\nNumber of Channels: " + numChannels
				+ "\nFrame Length (# in file): " + frameLength + "\nFrame Size (bytes): " + frameSize + "\nBit Rate: "
				+ bitRate + "\n");
		this.bytes = new byte[frameLength * frameSize];
		int result = 0;
		try {
			result = audioInputStream.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.print("Bytes written: " + result + "\nByte array length: " + bytes.length + "\n");
	}

	public int[][] getWaveform() {
		int[][] toReturn = new int[numChannels][frameLength];
		int sampleIndex = 0;
		for (int t = 0; t < bytes.length;) {
			for (int channel = 0; channel < numChannels; channel++) {
				int low = (int) bytes[t];
				t++;
				int high = (int) bytes[t];
				t++;
				int sample = ((high << 8) + (low & 0x00ff));
				toReturn[channel][sampleIndex] = sample;
			}
			sampleIndex++;
		}
		System.out.print("Sample Size: " + toReturn[0].length + "\n");
		return toReturn;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public int getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
	}

	public int getFrameLength() {
		return frameLength;
	}

	public void setFrameLength(int frameLength) {
		this.frameLength = frameLength;
	}

	public int getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(int frameSize) {
		this.frameSize = frameSize;
	}

	public AudioInputStream getAudioInputStream() {
		return audioInputStream;
	}

	public void setAudioInputStream(AudioInputStream audioInputStream) {
		this.audioInputStream = audioInputStream;
	}

	public int getBitRate() {
		return bitRate;
	}

	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}
}