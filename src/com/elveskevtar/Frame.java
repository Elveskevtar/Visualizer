package com.elveskevtar;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class Frame extends JFrame {

	private static final long serialVersionUID = 6117597923195976123L;

	public Frame() {
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setLayout(null);
		this.setUndecorated(true);
		this.setBackground(Color.BLACK);
		this.add(new Visualizer("res/run.wav", 0));
		this.setVisible(true);
		this.repaint();
	}
}