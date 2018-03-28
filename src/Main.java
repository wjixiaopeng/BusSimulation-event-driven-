
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.IOException;

public class Main {

    private static double clock;
    private static BusStatus[] Buses;
    private static StopStatus[] Stops;
    private static double Driving_Time = 0;
    private static double Boarding_Time;
    private static double Stop_Time;
    private static double Mean_Arrival_Rate;
    private static PriorityQueue<Event> heap = new PriorityQueue<>((a, b) -> Double.compare(a.getEvent_Time(), b.getEvent_Time()));

    private static void Initialization(File fin) throws IOException{

        FileInputStream fis = new FileInputStream(fin);

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line; int num = 0;
        while ((line = br.readLine()) != null) {
            String sub = line.split("\\s+")[1];
            if (num == 0) Buses = new BusStatus[Integer.parseInt(sub)];
            if (num == 1) Stops = new StopStatus[Integer.parseInt(sub)];
            if (num == 2) Driving_Time = Double.parseDouble(sub);
            if (num == 3) Boarding_Time = Double.parseDouble(sub);
            if (num == 4) Stop_Time = Double.parseDouble(sub);
            if (num == 5) Mean_Arrival_Rate = Double.parseDouble(sub);
            num++;
        }
        clock = 0;
//        Buses = new BusStatus[5];           //use 0 -> 4 denote 5 buses and every bus is supposed to stay which station
//        Stops = new StopStatus[15];         //use 0 -> 14 denote 15 stop and which bus stop current station
//        Driving_Time = 300;                 //use 5 * 60 transfer to seconds --> interval between 2 stop
//        Boarding_Time = 2;
//        //Mean_Arrival_Rate;         // 2 / 60
//        Stop_Time = 1000;                      // stop time!!
        br.close();
        distribute();
    }
    private static void distribute() {
        // initialize every stop which has one person waiting bus
        // every person will be specialized for the bus, so set current but to -1;

        // initialize every bus along the route
        int interval = Stops.length / Buses.length;           // distribute bus
        for (int bus = 0, stop = 0; bus < Buses.length; bus++, stop += interval) {
            heap.offer(new Event(0, clock, stop, bus));
            Buses[bus] = new BusStatus(clock, "" + stop);
        }

        for (int i = 0; i < Stops.length; i++) {             // distribute stop
            Event first = new Event(1, 12, i, -1);
            heap.offer(first);                              // every stop has one passenger
        }

    }
    // 0 -> arrival; 1 -> person; 2 -> boarder
    private static Event generate_Arrival(int curStop, int curBus) {
        int nextStop = (curStop + 1) % Stops.length;
        if (Stops[curStop] == null || Stops[curStop].queue.isEmpty()) {                       // no one at current stop
            Buses[curBus].Cur_Stop = curStop + " -> " + nextStop;   // current bus is going to next stop
            return new Event(0, clock + Driving_Time, nextStop, curBus);
        } else {
            Buses[curBus].Cur_Stop = curBus + " is Waiting people board at " + curStop;
            return new Event(2, clock, curStop, curBus);
        }
    }
    private static Event generate_Person(int curStop, int curBus) {
        Random rand = new Random();                 // generate random number
        double n = Mean_Arrival_Rate * 2 - 0 + 1;
        double i = rand.nextDouble() % n;
        Passenger laowang = new Passenger(clock + i); // generate next passenger
        if (Stops[curStop] == null) {
            Stops[curStop] = new StopStatus();
        }
        Stops[curStop].queue.offer(laowang);
        return new Event(1, laowang.Arrival_Time, curStop, curBus);
    }
    private static Event generate_Boarder(int curStop, int curBus) {
        int nextStop = (curStop + 1) % Stops.length;
        if (Stops[curStop] == null || Stops[curStop].queue.isEmpty()) {
            Buses[curBus].Cur_Stop = curStop + " -> " + nextStop;
            System.out.println(curStop + " -> " + nextStop);
            return new Event(0, clock + Driving_Time, nextStop, curBus);
        }
        Passenger cur = Stops[curStop].queue.poll();
        Buses[curBus].Cur_Stop = "Passenger who arrival at " + cur.Arrival_Time + " aboarded";
        return new Event(2, clock += Boarding_Time, curStop, curBus);
    }

    public static void main(String[] args) throws IOException {
        PrintWriter writer = new PrintWriter("SystemSnap.txt", "UTF-8");
        File dir = new File(".");
        File fin = new File(dir.getCanonicalPath() + File.separator + "in.txt");
        Initialization(fin);
        int[] max = new int[Stops.length];
        long[] avg = new long[Stops.length];
        long[] sum = new long[Stops.length];
        long[] times = new long[Stops.length];
        //System.out.println("clock  " + "Name_Bus" + "  "+ " Name_Stop " + "  " + "Event_Time");
        List<String> lines = new ArrayList<>();
        List<Double> hourly = new ArrayList<>();
        for (int i = 0; i < Stop_Time / 3600; i++) {
            hourly.add((double)(i * 3600 + 3600));
        }
        boolean flag = false;
        boolean[] visited = new boolean[hourly.size()];
        while (clock <= Stop_Time && !heap.isEmpty()) {
            Event cur = heap.poll();
            //System.out.println(clock + ":   " + cur.getName_Bus() + "        "+cur.getName_Stop() + "          " + cur.getEvent_Time());
            clock = cur.getEvent_Time();

            for (double i : hourly) {
                if (Math.abs(clock - i) < 0.00001 && !visited[(int)i / 3600]) {
                    flag = true;
                    visited[(int)i / 3600] = true;
                    break;
                }
            }
            for (int i = 0; i < max.length; i++) {
                if (Stops[i] == null) {
                    continue;
                }
                int size = Stops[i].queue.size();
                max[i] = Math.max(max[i], size);
                sum[i] = sum[i] + size;
                times[i]++;
                avg[i] = sum[i] / times[i];
            }
            if (flag) {
                for (int i = 0; i < Stops.length; i++)
                    lines.add("time: " + (int)clock / 3600 + " -- " + " max: " + max[i] + "    avg: " + avg[i] + "   stop: " + i);
            }
            flag = false;
            switch (cur.getEvent_Type()) {
                case 0:     // arrival
                    heap.offer(generate_Arrival(cur.getName_Stop(), cur.getName_Bus()));
//                    System.out.println(
//                            "Clock: " + clock + " -- " + Buses[cur.getName_Bus()].Cur_Stop);
//                    lines.add("Clock: " + clock + " -- " + Buses[cur.getName_Bus()].Cur_Stop);
                    break;
                case 1:     // person
                    heap.offer(generate_Person(cur.getName_Stop(), cur.getName_Bus()));
//                    System.out.println(
//                            "Clock: " + clock + " -- " + cur.getName_Stop() + " has " + Stops[cur.getName_Stop()].queue.size() + " Passengers");
//                    lines.add("Clock: " + clock + " -- " + cur.getName_Stop() + " has " + Stops[cur.getName_Stop()].queue.size() + " Passengers");
                    break;
                case 2:     // boarder
                    heap.offer(generate_Boarder(cur.getName_Stop(), cur.getName_Bus()));
//                    System.out.println(
//                            "Clock: " + clock + " -- " + "Passenger boarding at " + cur.getName_Stop() + Stops[cur.getName_Stop()].queue.size() +"passengers");
//                    lines.add("Clock: " + clock + " -- " + "Passenger boarding at " + cur.getName_Stop() + Stops[cur.getName_Stop()].queue.size() +"passengers");
                    break;
            }
        }
        Path file = Paths.get("SystemSnap.txt");
        Files.write(file, lines, Charset.forName("UTF-8"));
        writer.close();
    }

}
