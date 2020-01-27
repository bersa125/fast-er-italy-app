package com.model.fasteritaly.singleton_and_helpers;

import android.os.Bundle;

import com.model.fasteritaly.Address;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.Pharmacy;
import com.model.fasteritaly.User;
import com.model.fasteritaly.UserEvaluation;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ModelBundleAdapter {//Converts Bundle Objects to Model and viceversa
    public static Bundle getBundleRepresentation(Object model){
        Bundle result=new Bundle();
        if(model instanceof Hospital){ // We miss AVG_VOTE, AVG_VOTE_WAIT, AVG_VOTE_STRUCT, AVG_VOTE_SERVICE, BEST_QUEUE e WORST_QUEUE, PLACE_PHOTO
            Hospital a=(Hospital) model;
            result.putDouble("LAT", a.getAddress().getLatitude());
            result.putDouble("LONG", a.getAddress().getLongitude());
            result.putBoolean("TYPE",false);
            result.putString("PLACE",a.getPlaceName());
            result.putString("ADDRESS",a.getAddress().getAddress());
            result.putString("RED_WAIT_QUEUE",a.getRed_WaitQueue()==-1?"-/-":a.getRed_WaitQueue()+"");
            result.putString("YELLOW_WAIT_QUEUE",a.getYellow_WaitQueue()==-1?"-/-":a.getYellow_WaitQueue()+"");
            result.putString("GREEN_WAIT_QUEUE",a.getGreen_WaitQueue()==-1?"-/-":a.getGreen_WaitQueue()+"");
            result.putString("WHITE_WAIT_QUEUE",a.getWhite_WaitQueue()==-1?"-/-":a.getWhite_WaitQueue()+"");
            result.putString("NO_EXEC_WAIT_QUEUE",a.getNonExec_WaitQueue()==-1?"-/-":a.getNonExec_WaitQueue()+"");
            result.putString("TOT_WAIT_QUEUE",(Math.max(a.getRed_WaitQueue()+a.getYellow_WaitQueue()+a.getGreen_WaitQueue()+a.getWhite_WaitQueue()+a.getNonExec_WaitQueue(),-1))==-1?"-/-":(a.getRed_WaitQueue()+a.getYellow_WaitQueue()+a.getGreen_WaitQueue()+a.getWhite_WaitQueue()+a.getNonExec_WaitQueue())+"");
            result.putString("RED_TREAT_QUEUE",a.getRed_TreatQueue()==-1?"-/-":a.getRed_TreatQueue()+"");
            result.putString("YELLOW_TREAT_QUEUE",a.getYellow_TreatQueue()==-1?"-/-":a.getYellow_TreatQueue()+"");
            result.putString("GREEN_TREAT_QUEUE",a.getGreen_TreatQueue()==-1?"-/-":a.getGreen_TreatQueue()+"");
            result.putString("WHITE_TREAT_QUEUE",a.getWhite_TreatQueue()==-1?"-/-":a.getWhite_TreatQueue()+"");
            result.putString("NO_EXEC_TREAT_QUEUE",a.getNonExec_TreatQueue()==-1?"-/-":a.getNonExec_TreatQueue()+"");
            result.putString("TOT_TREAT_QUEUE",(Math.max(a.getRed_TreatQueue()+a.getYellow_TreatQueue()+a.getGreen_TreatQueue()+a.getWhite_TreatQueue()+a.getNonExec_TreatQueue(),-1))==-1?"-/-":(a.getRed_TreatQueue()+a.getYellow_TreatQueue()+a.getGreen_TreatQueue()+a.getWhite_TreatQueue()+a.getNonExec_TreatQueue())+"");
            result.putString("RED_OBS_QUEUE",a.getRed_ObsQueue()==-1?"-/-":a.getRed_ObsQueue()+"");
            result.putString("YELLOW_OBS_QUEUE",a.getYellow_ObsQueue()==-1?"-/-":a.getYellow_ObsQueue()+"");
            result.putString("GREEN_OBS_QUEUE",a.getGreen_ObsQueue()==-1?"-/-":a.getGreen_ObsQueue()+"");
            result.putString("WHITE_OBS_QUEUE",a.getWhite_ObsQueue()==-1?"-/-":a.getWhite_ObsQueue()+"");
            result.putString("NO_EXEC_OBS_QUEUE","-/-");
            result.putString("TOT_OBS_QUEUE",(Math.max(a.getRed_ObsQueue()+a.getYellow_ObsQueue()+a.getGreen_ObsQueue()+a.getWhite_ObsQueue()+a.getNonExec_ObsQueue(),-1)==-1?"-/-":(a.getRed_ObsQueue()+a.getYellow_ObsQueue()+a.getGreen_ObsQueue()+a.getWhite_ObsQueue()+a.getNonExec_ObsQueue()))+"");
            if(a.getUpdateDate()!=null)
                result.putString("UPDATE_DATE",a.getUpdateDate().split("T")[0]+" "+a.getUpdateDate().split("T")[1]);
            //Defaults to be overwritten
            result.putInt("AVG_VOTE",0);
            result.putInt("AVG_VOTE_WAIT",0);
            result.putInt("AVG_VOTE_STRUCT",0);
            result.putInt("AVG_VOTE_SERVICE",0);
            result.putBoolean("BEST_QUEUE",false);
            result.putBoolean("WORST_QUEUE",false);
        }else{
            if(model instanceof Pharmacy){ // MANCA PLACE_PHOTO
                Pharmacy a=(Pharmacy) model;
                result.putDouble("LAT", a.getAddress().getLatitude());
                result.putDouble("LONG", a.getAddress().getLongitude());
                result.putBoolean("TYPE",true);
                result.putString("PLACE",a.getPlaceName());
                result.putString("ADDRESS",a.getAddress().getAddress());
                if(a.getOpeningTimes()[1].equals("")){
                    result.putString("TIME", a.getOpeningTimes()[0]+"-"+a.getClosingTimes()[0]);
                    a.setOpen(isCurrentHourInInterval(a.getOpeningTimes()[0],a.getClosingTimes()[0]));
                    result.putBoolean("OPEN", a.isOpen());
                }else{
                    result.putString("TIME", a.getOpeningTimes()[0]+"-"+a.getClosingTimes()[0]+","+a.getOpeningTimes()[1]+"-"+a.getClosingTimes()[1]);
                    a.setOpen(isCurrentHourInInterval(a.getOpeningTimes()[0],a.getClosingTimes()[0])||isCurrentHourInInterval(a.getOpeningTimes()[1],a.getClosingTimes()[1]));
                    result.putBoolean("OPEN", a.isOpen());
                }

            }else{
                if(model instanceof UserEvaluation){// manca AVG_VOTE
                    UserEvaluation a=(UserEvaluation) model;
                    result.putString("USER_ID",a.getUser().getUserID());
                    result.putDouble("LAT", a.getHospital().getAddress().getLatitude());
                    result.putDouble("LONG", a.getHospital().getAddress().getLongitude());
                    result.putBoolean("TYPE",false);
                    result.putString("PLACE",a.getHospital().getPlaceName());
                    result.putString("ADDRESS",a.getHospital().getAddress().getAddress());
                    result.putString("DATE",a.getDate());
                    result.putInt("AVG_VOTE_USR_WAIT", a.getWaitVote());
                    result.putInt("AVG_VOTE_USR_STRUCT", a.getStructVote());
                    result.putInt("AVG_VOTE_USR_SERVICE", a.getServiceVote());
                    result.putInt("AVG_VOTE_USR", ((int)(a.getWaitVote()+a.getStructVote()+a.getServiceVote())/3));
                    //Defaults to be overwritten
                    result.putInt("AVG_VOTE",0);
                }
            }
        }
        return result;
    }
    public static Hospital getHospitalFromBundle(Bundle data){
        Hospital result=new Hospital(data.getString("PLACE"),new Address(data.getString("ADDRESS"),data.getDouble("LAT"),data.getDouble("LONG")));
        try {
            result.setRed_WaitQueue(Integer.parseInt(data.getString("RED_WAIT_QUEUE")));
            result.setYellow_WaitQueue(Integer.parseInt(data.getString("YELLOW_WAIT_QUEUE")));
            result.setGreen_WaitQueue(Integer.parseInt(data.getString("GREEN_WAIT_QUEUE")));
            result.setWhite_WaitQueue(Integer.parseInt(data.getString("WHITE_WAIT_QUEUE")));
            result.setNonExec_WaitQueue(Integer.parseInt(data.getString("NO_EXEC_WAIT_QUEUE")));
            result.setRed_TreatQueue(Integer.parseInt(data.getString("RED_TREAT_QUEUE")));
            result.setYellow_TreatQueue(Integer.parseInt(data.getString("YELLOW_TREAT_QUEUE")));
            result.setGreen_TreatQueue(Integer.parseInt(data.getString("GREEN_TREAT_QUEUE")));
            result.setWhite_TreatQueue(Integer.parseInt(data.getString("WHITE_TREAT_QUEUE")));
            result.setNonExec_TreatQueue(Integer.parseInt(data.getString("NO_EXEC_TREAT_QUEUE")));
            result.setRed_ObsQueue(Integer.parseInt(data.getString("RED_OBS_QUEUE")));
            result.setYellow_ObsQueue(Integer.parseInt(data.getString("YELLOW_OBS_QUEUE")));
            result.setGreen_ObsQueue(Integer.parseInt(data.getString("GREEN_OBS_QUEUE")));
            result.setWhite_ObsQueue(Integer.parseInt(data.getString("WHITE_OBS_QUEUE")));
            result.setNonExec_ObsQueue(Integer.parseInt(data.getString("NO_EXEC_OBS_QUEUE")));
            result.setUpdateDate(data.getString("UPDATE_DATE"));
        }catch(Exception e){}
        return result;
    }
    public static Pharmacy getPharmacyFromBundle(Bundle data){
        Pharmacy result=new Pharmacy(data.getString("PLACE"),new Address(data.getString("ADDRESS"),data.getDouble("LAT"),data.getDouble("LONG")));
        String[] openings=new String[]{"",""};
        String[] closures=new String[]{"",""};
        String[] splitData= data.getString("TIME").split(",");
        if(splitData.length==2){
            openings[0]=splitData[0].split("-")[0]; closures[0]=splitData[0].split("-")[1];
            openings[1]=splitData[1].split("-")[0]; closures[1]=splitData[1].split("-")[1];
        }else{
            openings[0]=splitData[0].split("-")[0]; closures[0]=splitData[0].split("-")[1];
        }
        result.setOpeningTimes(openings);
        result.setClosingTimes(closures);
        result.setOpen(isCurrentHourInInterval(result.getOpeningTimes()[0],result.getClosingTimes()[0])||isCurrentHourInInterval(result.getOpeningTimes()[1],result.getClosingTimes()[1]));
        return result;
    }
    public static UserEvaluation getUserEvaluationFromBundle(Bundle data){
        User user=new User(data.getString("USER_ID"));
        Hospital hospital= new Hospital(data.getString("PLACE"),new Address(data.getString("ADDRESS"),data.getDouble("LAT"),data.getDouble("LONG")));
        UserEvaluation result=new UserEvaluation(data.getString("DATE"),data.getInt("AVG_VOTE_USR_WAIT",0),data.getInt("AVG_VOTE_USR_STRUCT",0),data.getInt("AVG_VOTE_USR_SERVICE",0),user,hospital);
        return result;
    }

    public static boolean isCurrentHourInInterval(String initialTime, String finalTime) {
        try {
            Calendar now = Calendar.getInstance();

            int hour = now.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
            int minute = now.get(Calendar.MINUTE);
            int hour1 = Integer.parseInt(initialTime.split(":")[0]);
            int minute1 = Integer.parseInt(initialTime.split(":")[1]);
            int hour2 = Integer.parseInt(finalTime.split(":")[0]);
            int minute2 = Integer.parseInt(finalTime.split(":")[1]);


            if (hour >= hour1 && hour <= hour2) {
                if (hour == hour1) {
                    if (minute >= minute1) {

                        return true;
                    }
                } else {
                    if (hour == hour2) {
                        if (minute <= minute2) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }catch(Exception e){}
        return false;
    }
}
