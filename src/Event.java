/**
 * Created by Chopin on 10/6/16.
 */
public class Event {
    private int Event_Type;// 0 -> arrival; 1 -> person; 2 -> boarder
    private double Event_Time;
    private int Name_Stop;
    private int Name_Bus;
    public Event(int type, double time, int stop, int bus) {
        Event_Time = time;
        Event_Type = type;
        Name_Stop = stop;
        Name_Bus = bus;
    }
    public int getEvent_Type() {
        return Event_Type;
    }

    public void setEvent_Type(int event_Type) {
        Event_Type = event_Type;
    }

    public double getEvent_Time() {
        return Event_Time;
    }

    public void setEvent_Time(double event_Time) {
        Event_Time = event_Time;
    }

    public int getName_Stop() {
        return Name_Stop;
    }

    public void setName_Stop(int name_Stop) {
        Name_Stop = name_Stop;
    }

    public int getName_Bus() {
        return Name_Bus;
    }

    public void setName_Bus(int name_Bus) {
        Name_Bus = name_Bus;
    }

}
