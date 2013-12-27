public class StrokeEvent{
	public int id;
	public long time;
	public float x,y;
	public int valid;
	public StrokeEvent(int StrokeID, float xIn, float yIn, long ScheduleTime, int validity){
		id = StrokeID;
		x = xIn;
		y = yIn;
		time = ScheduleTime;
		valid = validity;
	}
}