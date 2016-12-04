package com.elveskevtar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

import org.jtransforms.fft.DoubleFFT_1D;

public class Visualizer extends JPanel {

	private static final long serialVersionUID = 8444330369696479630L;

	private AudioInputStream audioInputStream;
	private Waveform waveform;
	private File file;
	private Clip audio;

	private int[] wave;
	private int channel;

	private double rOffset;
	private double[] rand;
	private double[] buffer;
	private double[] size;
	private double[] sizeBuffer;

	private static final float[] colorFracs = { 0.28f, 0.33f, 0.5f };
	private static final Color[] colors = { new Color(0, 0, 0, 80), Color.CYAN, Color.RED };

	public Visualizer(String filePath, int channel) {
		this.setChannel(channel);
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setVisible(true);
		this.file = new File(filePath);
		this.waveform = new Waveform(file);
		this.wave = waveform.getWaveform()[channel];
		this.buffer = new double[1024];
		size = new double[360 / 18 + 1];
		sizeBuffer = new double[360 / 18 + 1];
		System.out.print("Width: " + getWidth() + "\nHeight: " + getHeight() + "\n");
		new Thread(new BlockRotation()).start();
		new Thread(new Repaint()).start();
		playMusic();
		new Thread(new calculateYOffset()).start();
		new Thread(new calculateRandMotion()).start();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(Color.RED);
		g2d.drawLine(0, (int) (getHeight() * 6 / 7), (int) getWidth(), (int) (getHeight() * 6 / 7));
		int oldX = 0;
		int oldY = (int) (getHeight() * 6 / 7);
		int xIndex = 0;
		int increment = (int) (wave.length / getWidth());
		g2d.setColor(Color.GREEN);
		System.out.println(audio.getFrameLength());
		if (audio.isActive())
			for (int t = audio.getFramePosition(); t < audio.getFramePosition()
					+ wave.length / (wave.length / waveform.getBitRate() / 10); t += increment) {
				double scaledSample = wave[t] / (getHeight() * 6 / 7);
				int y = (int) ((getHeight() * 6 / 7) + (scaledSample));
				g2d.drawLine(oldX, oldY, xIndex, y);
				xIndex += (wave.length / waveform.getBitRate() / 10);
				oldX = xIndex;
				oldY = y;
			}
		RadialGradientPaint rgp = new RadialGradientPaint(getWidth() / 2, getHeight() / 2, getHeight() / 2, colorFracs,
				colors);
		g2d.setPaint(rgp);
		int f = 0;
		for (double i = rOffset; i < 360.0 + rOffset; i += 18.0) {
			sizeBuffer[f] = 0;
			for (int k = f * 18; k < f * 18 + 50; k++) {
				sizeBuffer[f] += buffer[k];
			}
			sizeBuffer[f] /= 50.0;
			if (sizeBuffer[f] >= 2.5)
				size[f] = sizeBuffer[f];
			else if (size[f] >= 0)
				size[f] -= 0.1;
			Rectangle rect = new Rectangle((getWidth() / 2) - 5,
					(int) ((getHeight() / 2) - 150 - 30 - Math.pow(size[f], 5)), 10, (int) (30 + Math.pow(size[f], 5)));
			g2d.rotate(Math.toRadians(i), getWidth() / 2, getHeight() / 2);
			g2d.fill(rect);
			g2d.rotate(-Math.toRadians(i), getWidth() / 2, getHeight() / 2);
			f++;
		}
		g2d.dispose();
	}

	private void playMusic() {
		audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException | IOException e1) {
		}
		DataLine.Info info = new Info(Clip.class, audioInputStream.getFormat());
		audio = null;
		try {
			audio = (Clip) AudioSystem.getLine(info);
			audio.open(audioInputStream);
		} catch (LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
		audio.start();
	}

	private double[] fftBuffer(double[] sample) {
		double[] doubleWave = new double[sample.length * 2];
		System.arraycopy(sample, 0, doubleWave, 0, sample.length);
		DoubleFFT_1D transform = new DoubleFFT_1D(sample.length);
		transform.realForwardFull(doubleWave);
		return doubleWave;
	}

	public int[] getWave() {
		return wave;
	}

	public void setWave(int[] wave) {
		this.wave = wave;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public double getrOffset() {
		return rOffset;
	}

	public void setrOffset(double rOffset) {
		this.rOffset = rOffset;
	}

	public AudioInputStream getAudioInputStream() {
		return audioInputStream;
	}

	public void setAudioInputStream(AudioInputStream audioInputStream) {
		this.audioInputStream = audioInputStream;
	}

	private class calculateRandMotion extends Thread {

		@Override
		public void run() {
			rand = new double[(360 / 18) + 1];
			while (true) {
				for (int i = 0; i < rand.length; i++) {
					rand[i] = new Random().nextDouble();
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class calculateYOffset extends Thread {

		@Override
		public void run() {
			while (audio.isActive()) {
				int index = (int) (((double) audio.getMicrosecondPosition() / audio.getMicrosecondLength())
						* wave.length);
				double[] sample = new double[512];
				for (int i = index; i < index + 512; i++)
					sample[i - index] = wave[i];
				double[] fftWave = fftBuffer(sample);
				for (int i = 0; i < fftWave.length; i += 2) {
					double realValue = fftWave[i];
					double imagValue = fftWave[i + 1];
					double value = (Math.log10(Math.sqrt((realValue * realValue) + (imagValue * imagValue))) + 1.0)
							* 0.5;
					buffer[i >> 1] = value;
				}
				try {
					Thread.sleep(12);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class BlockRotation extends Thread {

		@Override
		public void run() {
			while (true) {
				if (rOffset > 360)
					rOffset -= 360;
				rOffset += 0.7;
				try {
					Thread.sleep(8);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class Repaint extends Thread {

		@Override
		public void run() {
			while (true) {
				Visualizer.this.repaint();
				try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}