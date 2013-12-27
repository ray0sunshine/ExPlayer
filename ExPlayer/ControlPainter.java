import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class ControlPainter {
	
	private ArrayList<ArrayList<ControlEvent>> cv;
	private ArrayList<Integer> type;
	private ControlEvent latest;
	
	public ControlPainter() {
		cv = new ArrayList<ArrayList<ControlEvent>>();
		type = new ArrayList<Integer>();
		latest = null;
	}
	
	//TODO painter function
	public void addEnder(String[] end){
		ArrayList<ControlEvent> c = new ArrayList<ControlEvent>();
		int filterID = Integer.parseInt(end[1]);
		type.add(filterID);
		for(int i=2; i<end.length; i++){
			c.add(new ControlEvent(end[i]));
		}
		cv.add(c);
	}
	
	//TODO reading entire line function
	public void drawControl(int part, Graphics2D g, long time, float scale, int w, int h){
		g.setColor(Color.RED);
		
		switch(type.get(part)){
		case 1:
			for(ControlEvent ce : cv.get(part)){
				if(ce.time <= time){
					latest = ce;
				}else{
					//it's still at default pos assuming the bounds are reset each time
				}
			}
			
			if(latest != null){
				g.drawLine(0, 0, w, (int)(scale*latest.data[0]));
				g.drawLine(0, (int)(scale*latest.data[0]), w, (int)(scale*latest.data[0]));
				g.drawLine(0, (int)(scale*latest.data[1]), w, (int)(scale*latest.data[1]));
				g.drawLine(0, (int)(scale*latest.data[1]), w, h);
			}
			break;
		case 3:
			for(ControlEvent ce : cv.get(part)){
				if(ce.time <= time){
					latest = ce;
				}else{
					//it's still at default pos assuming the bounds are reset each time
				}
			}
			
			if(latest != null){
				int x = (int)(scale*latest.data[2]);
				int y = (int)(scale*latest.data[0]);
				int wd = (int)(scale*latest.data[3])-x;
				int ht = (int)(scale*latest.data[1])-y;
				g.drawRect(x,y,wd,ht);
			}
			break;
		}
	}
	
	private class ControlEvent{
		public float[] data;
		public long time;
		
		public ControlEvent(String seg){
			String[] segs = seg.split(",");
			data = new float[segs.length-1];
			for(int i=0; i<segs.length-1; i++){
				data[i] = Float.parseFloat(segs[i]);
			}
			time = Long.parseLong(segs[segs.length-1]);
		}
	}
}
