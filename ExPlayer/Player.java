import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class Player extends JComponent{
	private static JFrame frame = new JFrame("ExPlayer");
	private static JButton openFile = new JButton("View part:");
	private static JButton togglePP = new JButton("PLAY");
	private static JButton next = new JButton(">");
	private static JButton prev = new JButton("<");
	private static JList<String> jl = new JList<String>();
	private static JSlider jsl = new JSlider();
	private static JSlider jsc = new JSlider();
	private static JScrollPane js;
	private static Player p;
	private static JLabel ctime = new JLabel();
	private static JLabel cscale = new JLabel();
	private static ControlPainter cp = new ControlPainter();
	
	public static void main(String[] args) throws FileNotFoundException{
		//initial checks
		if(args.length != 1){
			System.out.println("Proper input is the name of a .txt data file");
			System.exit(0);
		}else if(!args[0].endsWith(".txt")){
			System.out.println("Proper input is the name of a .txt data file");
			System.exit(0);
		}
		
		//read the file and check
		File data = new File(args[0]);
		if(!data.exists()){
			System.out.println("Cannot find the file '"+args[0]+"'");
			System.exit(0);
		}
		
		//init the player with the correct file and ui, player's constructor fills in the ui with actual data
		ctime.setText("0");
		ctime.setSize(100, 25);
		ctime.setLocation(230,230);
		ctime.setForeground(new Color(200,200,200));
		
		openFile.setSize(120,30);
		openFile.setLocation(10,10);
		openFile.setEnabled(false);
		
		togglePP.setSize(80,30);
		togglePP.setLocation(210,190);
		togglePP.setEnabled(false);
		
		next.setSize(60,30);
		next.setLocation(300,190);
		next.setEnabled(false);
		
		prev.setSize(60,30);
		prev.setLocation(140,190);
		prev.setEnabled(false);
		
		jl.setSize(360,120);
		jl.setLocation(140, 10);
		jl.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				openFile.setEnabled(true);
			}
		});
		
		js = new JScrollPane(jl);
		js.setSize(360,120);
		js.setLocation(140, 10);
		
		jsl.setSize(490, 50);
		jsl.setLocation(10,140);
		jsl.setValue(0);
		jsl.setBackground(Color.BLACK);
		jsl.setEnabled(false);
		
		jsc.setSize(120, 30);
		jsc.setLocation(10,50);
		jsc.setMinimum(30);
		jsc.setMaximum(100);
		jsc.setValue(100);
		jsc.setBackground(Color.BLACK);
		
		cscale.setText("Scale: 1");
		cscale.setSize(100, 25);
		cscale.setLocation(20,80);
		cscale.setForeground(new Color(200,200,200));
		
		p = new Player(data);
		p.setPreferredSize(new Dimension(500,250));
		p.add(ctime);
		p.add(cscale);
		p.add(openFile);
		p.add(js);
		p.add(jsl);
		p.add(jsc);
		p.add(togglePP);
		p.add(next);
		p.add(prev);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.BLACK);
		frame.setContentPane(p);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	private DefaultListModel<String> model = new DefaultListModel<String>();
	private ArrayList<ArrayList<StrokeEvent>> expList;	//array of all experiments containing the sorted events of all parts
	private ArrayList<String> expParts;
	private ArrayList<Integer> finishIds;
	private int width, height;
	private ExPlayer expl;
	private float curScale;
	
	WindowListener exitListen = new WindowAdapter(){
		public void windowClosing(WindowEvent e){
			jsl.setValue(0);
			jsl.setEnabled(false);
			jsc.setEnabled(true);
			togglePP.setEnabled(false);
			prev.setEnabled(false);
			next.setEnabled(false);
			openFile.setEnabled(true);
			jl.setEnabled(true);
			if(expl.t != null){
				expl.t.cancel();
				expl.t.purge();
			}
			jsl.removeChangeListener(seekListen);
		}
	};
	
	ActionListener openListen = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			jsl.setEnabled(true);
			jsc.setEnabled(false);
			togglePP.setEnabled(true);
			prev.setEnabled(true);
			next.setEnabled(true);
			openFile.setEnabled(false);
			jl.setEnabled(false);
			int sel = jl.getSelectedIndex();
			expl = new ExPlayer(expList.get(sel), width, height, expParts.get(sel),finishIds.get(sel),jsl,curScale);
			expl.setCP(cp,sel);
			expl.frame.addWindowListener(exitListen);
			jsl.addMouseListener(selListen);
		}
	};
	
	ActionListener ppListen = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			jsl.removeChangeListener(seekListen);
			expl.togglePlay();
		}
	};
	
	ActionListener nextListen = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			expl.seekTo((int) (expl.timeTrack + 100));
			jsl.setValue((int) (expl.timeTrack + 100));
		}
	};
	
	ActionListener prevListen = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			expl.seekTo((int) (expl.timeTrack - 100));
			jsl.setValue((int) (expl.timeTrack - 100));
		}
	};
	
	ChangeListener seekListen = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
			expl.seekTo(jsl.getValue());
		}
	};
	
	ChangeListener scaleListen = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
			curScale = (float)jsc.getValue()/100;
			cscale.setText("scale: "+Float.toString(curScale));
		}
	};
	
	ChangeListener updateListen = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
			String newTime = Float.toString((float)jsl.getValue()/(float)1000);
			ctime.setText(newTime);
		}
	};
	
	MouseListener selListen = new MouseAdapter(){
		public void mousePressed(MouseEvent e){
			jsl.addChangeListener(seekListen);
		}
		
		public void mouseReleased(MouseEvent e){
			jsl.removeChangeListener(seekListen);
		}
	};
	
	public Player(File data) throws FileNotFoundException{
		//TODO update based on revised data and header format, along with multiple experiment parts
		Scanner s = new Scanner(data);
		
		//TODO update the master header reading based on new specs
		//read the master header
		s.nextLine();
		String[] dim = s.nextLine().split(",");
		width =  Integer.parseInt(dim[0]);
		height = Integer.parseInt(dim[1]);
		
		//read the rest of the lines
		ArrayList<String> lines =  new ArrayList<String>();
		while(s.hasNext()){
			lines.add(s.nextLine());
		}
		
		//build event tables and sort
		parse(lines);
		
		//build ui based on headers
		for(String part : expParts){
			model.addElement(part);
		}
		jl.setModel(model);
		curScale = 1;
		
		//add event handlers and save a instance of the actual player
		openFile.addActionListener(openListen);
		togglePP.addActionListener(ppListen);
		next.addActionListener(nextListen);
		prev.addActionListener(prevListen);
		jsl.addChangeListener(updateListen);
		jsc.addChangeListener(scaleListen);
	}
	
	@SuppressWarnings("unchecked")
	private void parse(ArrayList<String> line){
		ArrayList<ArrayList<StrokeEvent>> ret = new ArrayList<ArrayList<StrokeEvent>>();
		ArrayList<String> parts = new ArrayList<String>();
		ArrayList<Integer> finIds = new ArrayList<Integer>();
		ArrayList<StrokeEvent> evt = new ArrayList<StrokeEvent>();
		for(String l : line){
			if(l.toCharArray()[0] == '-'){
				finIds.add(evt.get(evt.size()-1).id);
				Collections.sort(evt, new Comparator<StrokeEvent>(){
					public int compare(StrokeEvent a, StrokeEvent b) {
						return (int)(a.time - b.time);
					}
				});
				ret.add((ArrayList<StrokeEvent>)evt.clone());
				evt.clear();
			}else{
				String[] segs = l.split(":");
				if(segs[0].toCharArray()[0] == 'h'){
					parts.add("["+segs[2]+"] "+segs[1]);
				}else if(segs[0].toCharArray()[0] == 'e'){
					cp.addEnder(segs);
				}else{
					String[] head = segs[0].split("[\\[\\]]");
					int id = Integer.parseInt(head[0]);
					int valid = Integer.parseInt(head[1]);
					long baseTime = Long.parseLong(head[2]);
					for(int i=1; i<segs.length; i++){
						String[] ev = segs[i].split(",");
						evt.add(new StrokeEvent(id, Float.parseFloat(ev[0]), Float.parseFloat(ev[1]), baseTime + Long.parseLong(ev[2]), valid));
						if(i == segs.length - 1){
							evt.add(new StrokeEvent(id, -1, -1, baseTime + Long.parseLong(ev[2]), valid));
						}
					}
				}
			}
		}
		expList = ret;
		expParts = parts;
		finishIds = finIds;
	}
}
