package com.example.accidentdetection.AlertSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Distance {
    private static Distance d=null;
    private static HashMap<Double,Double> map =  new HashMap<Double, Double>();
    private static ArrayList<Double> array =  new ArrayList<Double>();
    private Distance(){
        map.put(22.6001338,88.4069089);
        map.put(22.466318,88.392723);
        map.put(22.5414775,88.3507265);
        map.put(22.5392279,88.3393287);
        map.put(22.451662,88.301227);
        map.put(22.5758679,88.350033);
        map.put(22.459034,88.3099172);
        map.put(22.5253609,88.4370163);
        map.put(22.626948,88.378681);
        map.put(22.57662,88.47931);
//        BISWA BANGLA GATE
        map.put(22.578806253258964, 88.4717260252482);

        map.put(26.12455880,89.46500256);
        map.put(26.12782125,89.46316961);
        map.put(26.13002436,89.462536610);
        map.put(26.12871375,89.46460425);
        map.put(22.1011392, 88.1291549);
        map.put(22.1186415, 88.1206573);
        //bishal
        map.put(24.17302,88.27313);
        map.put(24.188309,88.267448);
        map.put(24.173309, 88.270387);
        //swarup
        map.put(23.798132, 88.455021);
        //souvik
        map.put(22.7261958, 87.9713137);

        array.add(22.6001338);
        array.add(22.466318);
        array.add(22.5414775);
        array.add(22.5392279);
        array.add(22.451662);
        array.add(22.5758679);
        array.add(22.459034);
        array.add(22.5253609);
        array.add(22.626948);
        array.add(22.57662);

        array.add(26.12455880);
        array.add(26.12782125);
        array.add(26.12782125);
        array.add(26.12871375);
        array.add(22.1011392);
        array.add(22.1186415);

        array.add(22.578806253258964);
        array.add(23.798132);
        array.add(22.7261958);
        Collections.sort(array);
    }
    //singleton method
    public static Distance getInstance(){
        if(d == null)
            d =  new Distance();
        return d;
    }
    public boolean caldistance(double lat1,double long1){
        int lower_bound = lower_bound(lat1);
        int upper_bound=-1;
        if(lower_bound!=0){
            upper_bound = lower_bound-1;
        }
        double dist1 = distance(lat1,long1,array.get(lower_bound),map.get(array.get(lower_bound)));
        double dist2 = dist1+1;
        if(upper_bound!=-1)
            dist2 = distance(lat1,long1,array.get(upper_bound),map.get(array.get(upper_bound)));
        dist1 =  Math.min(dist1,dist2);
        if(dist1<=0.5)
            return true;
        return false;
    }
    //distance function
    private double distance(double lat1, double long1, double lat2, double long2) {
        double dlat =  Math.toRadians(lat2-lat1);
        double dlong =  Math.toRadians(long2-long1);
        double radius =  6371;
        double a = Math.sin(dlat/2)*Math.sin(dlat/2) + Math.cos(Math.toRadians(lat1))
                *Math.cos(Math.toRadians(lat2))*Math.sin(dlong/2)*Math.sin(dlong/2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        double d = radius*c;

        return d;
    }
    //lower bound function
    private int lower_bound(Double key)
    {

        int index = Collections.binarySearch(array, key);

        if (index < 0) {
            return Math.abs(index) - 1;
        }

        else {
            while (index > 0) {

                if (array.get(index-1) == key)
                    index--;
                else
                    return index;
            }

            return index;
        }
    }
}
