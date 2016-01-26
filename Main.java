package fractal;

import java.awt.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main extends JFrame
{
	private GameCanvas canvas;
	
	static enum State
	{
		INITIAL, PLAYING, RESTART, PAUSE;
	}
	static State state;
	
	public static final int CANVAS_WIDTH = 1100;
	public static final int CANVAS_HEIGHT = 600;
	public static final int UPDATE_RATE = 60;
	public static final long UPDATE_PERIOD = 1000000000L / UPDATE_RATE;
	
	private Dimension dim, screenSize, frameSize;
	private boolean keyDown, leftKeyDown, rightKeyDown, upKeyDown, downKeyDown, wKeyDown, aKeyDown, sKeyDown, dKeyDown, spaceKeyDown;
	private int xS, yS, numStepsX, numStepsY, xMin, yMin, noise, fractalSum, valueOffset;
	private int[][] level, levelGrad, fractal, random;
	private int kMaxVertices, xMaxVerticesZoom, yMaxVerticesZoom;
	private double tRemapSmoothstep, xInput, yInput, txDist, tyDist, frequency, amplitude, lacurity, gain, maxSum, turbulence;
	
	public Main()
	{
		initVars();
		
		canvas = new GameCanvas();
		canvas.setPreferredSize(dim);
		this.setContentPane(canvas);
		this.setLocation(xS, yS);
   
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle("2D Fractaling!");
		this.setVisible(true);
		
		gameStart();
	}
	
	public void initVars()
	{
		dim = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frameSize = getSize();
		xS = ((screenSize.width - frameSize.width) / 2) - CANVAS_WIDTH/2;
		yS = ((screenSize.height - frameSize.height) / 2) - CANVAS_HEIGHT/2;
		
		leftKeyDown = false;
		rightKeyDown = false;
		upKeyDown = false;
		downKeyDown = false;
		wKeyDown = false;
		aKeyDown = false;
		sKeyDown = false;
		dKeyDown = false;
		spaceKeyDown = false;
		keyDown = false;
		
		
		frequency = 1;
		amplitude = 1;
		turbulence = .50;
		maxSum = 0;
		
		kMaxVertices = 512;
		numStepsX = 500;
		numStepsY = 500;
		lacurity = .5;
		gain = 2;
		fractalSum = 7;
		
		
		
	    random = new int[kMaxVertices][kMaxVertices];
	    level = new int[numStepsX][numStepsY];
	    levelGrad = new int[numStepsX][numStepsY];
	    fractal = new int[numStepsX][numStepsY];
	    randomInts();
	    calcNoise();
	    levelSet();
	}
	
	
	public void randomInts()
	{
		for(int i = 0; i < kMaxVertices; i++)
		{
			for(int j = 0; j < kMaxVertices; j++)
	    	{
	    		random[i][j] = (int) (Math.random()*255);
	    	}
		}
	}

	public double Quintic(double t)
	{
		return 6*(Math.pow(t, 5)) - 15*(Math.pow(t, 4)) + 10*(Math.pow(t, 3));
	}
	
	public double Smoothstep(double t)
	{
	    tRemapSmoothstep = t * t * ( 3 - 2 * t );
	    return tRemapSmoothstep;
	}
	
	public int Lerp(int a, int b, double t)
	{
	    return (int)(a*(1 - t) + b*t);
	}	
	
	public void calcNoise()
	{
		for(int l = 0; l < fractalSum; l++)
	    {
			//randomInts();
			maxSum += amplitude;
			for(int i = 0; i < numStepsX; i++)
		    {
		    	for(int j = 0; j < numStepsY; j++)
			    {
			    	xInput = i / (double)(numStepsX - 1) * (kMaxVertices-1);
			    	yInput = j / (double)(numStepsY - 1) * (kMaxVertices-1);
			    	noise = (int) ((eval(xInput*frequency, yInput*frequency)*amplitude) + valueOffset);
			    	
			    	fractal[i][j] += (int) (noise);
			    }  
		    }
			frequency *= lacurity;
			amplitude *= gain;
			//valueOffset = (int)(256*amplitude);
	    }
		for(int i = 0; i < numStepsX; i++)
	    {
	    	for(int j = 0; j < numStepsY; j++)
		    {
		    	fractal[i][j] /= (maxSum);
		    }  
	    }
		//fractal[i][j] = noiseWeight;
	}
	
	public int eval(double x, double y)
	{
		xMin = (int)x;
		yMin = (int)y;
		txDist = x - xMin;
		tyDist = y - yMin;
		//txDist = Smoothstep(txDist);
		//tyDist = Smoothstep(tyDist);
		txDist = Quintic(txDist);
		tyDist = Quintic(tyDist);
		int Lerp1 = Lerp(random[xMin][yMin], random[(xMin + 1)%kMaxVertices][yMin], txDist);
		int Lerp2 = Lerp(random[xMin][(yMin + 1)%kMaxVertices], random[(xMin + 1)%kMaxVertices][(yMin + 1)%kMaxVertices], txDist);
		int Lerp3 = Lerp(Lerp1, Lerp2, tyDist);
		return Lerp3;
	}
	
	public void levelSet()
	{
		for(int i = 0; i < numStepsX; i++)
		{
			for(int j = 0; j < numStepsY; j++)
			{
				//Set top half black, bottom half white//
				if(j < (numStepsY/2 + 1))
				{
					level[i][j] = 1;
				}
				else
				{
					level[i][j] = 0;
				}
			}
		}
		
		for(int i = 0; i < numStepsX; i++)
		{
			for(int j = 0; j < numStepsY; j++)
			{
				//Set top half black, bottom half white//
				if(j < (numStepsY/2 + 1))
				{
					levelGrad[i][j] = 1;
				}
				else
				{
					levelGrad[i][j] = 0;
				}
			}
		}
		
		//Caves(Direct Set)//
		
		for(int i = 0; i < numStepsX; i++)
		{
			for(int j = 0; j < numStepsY; j++)
			{
				
				/*if(fractal[i][j] > 127)
				{
					level[i][j] = 0;
				}
				if(fractal[i][j] < 128)
				{
					level[i][j] = 1;
				}*/
			}
		}
		
		//Landscape(HeightMap)//
		
		for(int i = 0; i < numStepsX; i++)
		{
			for(int j = 0; j < numStepsY*turbulence; j++)
			{

				double multiplier = (numStepsY*turbulence)/256;
				int offsetValue = (int) (((fractal[i][(int) (j + numStepsY*(turbulence/2))] + 1)*multiplier) - ((numStepsY*turbulence)*turbulence));

				level[i][(int) (j + (numStepsY*(turbulence/2)))] = levelGrad[i][(int) (j + (numStepsY*(turbulence/2))) + offsetValue];
			}
		}
	}
	    
	
	public void gameLoop()
	{
		long beginTime, timeTaken, timeLeft;
		while(true)
		{
			beginTime = System.nanoTime();
			repaint();
			if(state == State.PLAYING)
			{
				gameUpdate();
			}
			timeTaken = System.nanoTime() - beginTime;
			timeLeft = (UPDATE_PERIOD - timeTaken) / 100000L;
			if(timeLeft < 10)
			{
				timeLeft = 10;
			}
			try 
			{
				Thread.sleep(timeLeft);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void gameUpdate()
	{
		if(spaceKeyDown == true)
		{
			frequency = 1;
			amplitude = 1;
			maxSum = 0;
			
			kMaxVertices = 512;
			numStepsX = 500;
			numStepsY = 500;
			fractalSum = 6;
			
			turbulence = .50;
			
		    random = new int[kMaxVertices][kMaxVertices];
		    level = new int[numStepsX][numStepsY];
		    levelGrad = new int[numStepsX][numStepsY];
		    fractal = new int[numStepsX][numStepsY];
		    randomInts();
		    calcNoise();
		    levelSet();
		}
		/*
		if(aKeyDown == true)
		{
			kMaxVertices--;
			System.out.println("kMaxVertices: " + kMaxVertices);
		}
		
		if(sKeyDown == true)
		{
			kMaxVertices++;
			System.out.println("kMaxVertices: " + kMaxVertices);
		}	
		
		if(wKeyDown == true)
		{
			numSteps--;
			System.out.println("numSteps: " + numSteps);
		}
		
		if(dKeyDown == true)
		{
			numSteps++;
			System.out.println("numSteps: " + numSteps);
		}*/
	}
	
	public void gameDraw(Graphics2D g2d)
	{
		switch (state) 
   		{
   			case INITIAL:
   				//
   				break;
   			case PLAYING:
   				//g2d.setColor(Color.black);
   				//g2d.fillRect(0, 0, xMaxVerticesZoom, yMaxVerticesZoom);
   				
   				for(int i = 0; i < numStepsX; i++)
   				{
	   				for(int j = 0; j < numStepsY; j++)
	   				{
		   				if(level[i][j] == 1)
   						{
   							g2d.setColor(Color.black);
   						}
   						else if(level[i][j] == 0)
   						{
   							g2d.setColor(Color.white);
   						}
   						g2d.drawRect(i, j, 1, 1);
	   				}
   				}
   				
   				g2d.setColor(Color.black);
   				g2d.fillRect(10 + numStepsX, 0, numStepsX, numStepsY);
   				
   				for(int i = 0; i < numStepsX; i++)
   				{
	   				for(int j = 0; j < numStepsY; j++)
	   				{
	   					Color newColor = new Color(255, 255, 255, fractal[i][j]);
		   				g2d.setColor(newColor);
		   				g2d.fillRect(i + 10 + numStepsX, j, 1, 1);
	   				}
   				}
   				break;
   			case RESTART:
   				// ......
   				break;
   			case PAUSE:
   				// ......
   				break;
   		}
	}
	
	public void gameKeyPressed(int keyCode)
   	{
   		keyDown = true;
   		switch (keyCode)
   		{
   			case KeyEvent.VK_UP:
   				upKeyDown = true;
   				break;
   			case KeyEvent.VK_DOWN:
   				downKeyDown = true;
   				break;
   			case KeyEvent.VK_LEFT:
   				leftKeyDown = true;
   				break;
   			case KeyEvent.VK_RIGHT:
   				rightKeyDown = true;
   				break;
   			case KeyEvent.VK_W:
   				wKeyDown = true;
   				break;
   			case KeyEvent.VK_A:
   				aKeyDown = true;
   				break;
   			case KeyEvent.VK_S:
   				sKeyDown = true;
   				break;
   			case KeyEvent.VK_D:
   				dKeyDown = true;
   				break;
   			case KeyEvent.VK_SPACE:
   				spaceKeyDown = true;
   				break;
   		}
   	}
   
   	// Process a key-released event.
   	public void gameKeyReleased(int keyCode) 
   	{
   		keyDown = false;
   		switch (keyCode)
   		{
   			case KeyEvent.VK_UP:
   				upKeyDown = false;
   				break;
   			case KeyEvent.VK_DOWN:
   				downKeyDown = false;
   				break;
   			case KeyEvent.VK_LEFT:
   				leftKeyDown = false;
   				break;
   			case KeyEvent.VK_RIGHT:
   				rightKeyDown = false;
   				break;
   			case KeyEvent.VK_W:
   				wKeyDown = false;
   				break;
   			case KeyEvent.VK_A:
   				aKeyDown = false;
   				break;
   			case KeyEvent.VK_S:
   				sKeyDown = false;
   				break;
   			case KeyEvent.VK_D:
   				dKeyDown = false;
   				break;
   			case KeyEvent.VK_SPACE:
   				spaceKeyDown = false;
   				break;
   		}
   	}

	class GameCanvas extends JPanel implements KeyListener, MouseListener, MouseMotionListener
	{

		
		public GameCanvas()
		{
			setFocusable(true);
			requestFocus();
			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public void paintComponent(Graphics g)
		{
			Graphics2D g2d = (Graphics2D)g;
			super.paintComponent(g);
			setBackground(Color.lightGray);
			gameDraw(g2d);
		}
		
		@Override
		public void mouseDragged(MouseEvent arg0) 
		{
			
		}

		@Override
		public void mouseMoved(MouseEvent arg0) 
		{
			
		}

		@Override
		public void mouseClicked(MouseEvent arg0) 
		{
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0)
		{
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) 
		{
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) 
		{
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) 
		{
			
		}

		@Override
		public void keyPressed(KeyEvent arg0) 
		{
			gameKeyPressed(arg0.getKeyCode());
		}

		@Override
		public void keyReleased(KeyEvent arg0)
		{
			gameKeyReleased(arg0.getKeyCode());
		}

		@Override
		public void keyTyped(KeyEvent arg0) 
		{
			
		}
	}
	
	public void gameStart()
	{
		state = State.PLAYING;
		gameLoop();
	}
	
	public static void main(String[] args)
	{
		new Main();
	}
}

