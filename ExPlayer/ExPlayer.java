import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;

@SuppressWarnings("serial")
public class ExPlayer extends JComponent {
	private ArrayList<StrokeEvent> events;
	private ArrayList<StrokeEvent> trueEv;
	private ArrayList<StrokeEvent> falseEv;
	private ArrayList<StrokeEvent> controlEv;
	
	private ControlPainter cp;
	private int partID;
	private float scl;
	
	private int w,h,finish;
	private JSlider js;
	public long timeTrack;
	public boolean playing;
	public Timer t;
	public JFrame frame;
	
	private float[] xHead;
	private float[] yHead;
	private float[] sxHead;
	private float[] syHead;
	
	public ExPlayer(ArrayList<StrokeEvent> evt, int width, int height, String name, int finId, JSlider jsl, float scale){
		scl = scale;
		
		if(scale != 1){
			events = new ArrayList<StrokeEvent>();
			for(StrokeEvent e : evt){
				events.add(new StrokeEvent(e.id, e.x*scale, e.y*scale, e.time, e.valid));
			}
		}else{
			events = evt;
		}
		
		trueEv = new ArrayList<StrokeEvent>();
		falseEv = new ArrayList<StrokeEvent>();
		controlEv = new ArrayList<StrokeEvent>();
		
		for(StrokeEvent e : events){
			switch(e.valid){
			case 0:
				falseEv.add(e);
				break;
			case 1:
				trueEv.add(e);
				break;
			case 2:
				controlEv.add(e);
				break;
			}
		}
		
		w = (int) (width*scale);
		h = (int) (height*scale);
		js = jsl;
		finish = finId;
		setPreferredSize(new Dimension(w,h));
		timeTrack = 0;
		
		frame = new JFrame(name);
		frame.setBackground(Color.WHITE);
		frame.setContentPane(this);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
		xHead = new float[finId+1];
		yHead = new float[finId+1];
		sxHead = new float[finId+1];
		syHead = new float[finId+1];
		
		js.setMinimum(0);
		js.setMaximum((int) events.get(events.size()-1).time);
		
		playing = false;
	}
	
	public void setCP(ControlPainter cPainter, int pID){
		cp = cPainter;
		partID = pID;
	}
	
	public void paintComponent(Graphics g2d){
		Graphics2D g = (Graphics2D)g2d;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		resetHead();
		
		g.setColor(new Color(255,200,200));
		
		for(int i=0; i<falseEv.size(); i++){
			StrokeEvent ev = falseEv.get(i);
			if(ev.time <= timeTrack){
				if(ev.x >= 0){
		        	g.drawLine((int)xHead[ev.id], (int)yHead[ev.id], (int)ev.x, (int)ev.y);
		        	xHead[ev.id] = ev.x;
		        	yHead[ev.id] = ev.y;
	        	}else{
	        		xHead[ev.id] = sxHead[ev.id];
	        		yHead[ev.id] = syHead[ev.id];
	        	}
			}else{
				i = falseEv.size();
			}
		}
		
		g.setColor(new Color(150,255,150));
		
		for(int i=0; i<controlEv.size(); i++){
			StrokeEvent ev = controlEv.get(i);
			if(ev.time <= timeTrack){
				if(ev.x >= 0){
		        	g.drawLine((int)xHead[ev.id], (int)yHead[ev.id], (int)ev.x, (int)ev.y);
		        	xHead[ev.id] = ev.x;
		        	yHead[ev.id] = ev.y;
	        	}else{
	        		xHead[ev.id] = sxHead[ev.id];
	        		yHead[ev.id] = syHead[ev.id];
	        	}
			}else{
				i = controlEv.size();
			}
		}
		
		g.setColor(Color.BLACK);
		
		for(int i=0; i<trueEv.size(); i++){
			StrokeEvent ev = trueEv.get(i);
			if(ev.time <= timeTrack){
				if(ev.x >= 0){
		        	g.drawLine((int)xHead[ev.id], (int)yHead[ev.id], (int)ev.x, (int)ev.y);
		        	xHead[ev.id] = ev.x;
		        	yHead[ev.id] = ev.y;
	        	}else{
	        		xHead[ev.id] = sxHead[ev.id];
	        		yHead[ev.id] = syHead[ev.id];
	        	}
			}else{
				i = trueEv.size();
			}
		}
		
		cp.drawControl(partID, g, timeTrack, scl, w+10, h);//sketchy +10
	}
	
	public void togglePlay(){
		if(!playing){
			playing = true;
			t = new Timer();
			t.schedule(new TimerTask() {
				public void run() {
					timeTrack += 50;
					js.setValue((int) timeTrack);
					repaint();
				}
			},0,50);
		}else{
			playing = false;
			t.cancel();
			t.purge();
		}
	}
	
	//seeks sets play to false
	public void seekTo(int time){
		playing = false;
		if(t != null){
			t.cancel();
			t.purge();
		}
		timeTrack = time;
		repaint();
	}
	
	private void resetHead(){
		boolean[] set = new boolean[finish+1];
		for(boolean b : set){
			b = false;
		}
		
		for(int i=0; i<events.size(); i++){
			StrokeEvent e = events.get(i);
			if(!set[e.id]){
				xHead[e.id] = e.x;
				yHead[e.id] = e.y;
				sxHead[e.id] = e.x;
				syHead[e.id] = e.y;
				set[e.id] = true;
			}
		}
	}
}
