package com.business_logic.fasteritaly.data_helper;

import org.json.JSONException;
import org.json.JSONObject;

public class ERStatusLazio {

    private String updateDate;
    private String nome;
    private String comune;
    private int Red_WaitQueue;
    private int Red_TreatQueue;
    private int Red_ObsQueue;
    private int Yellow_WaitQueue;
    private int Yellow_TreatQueue;
    private int Yellow_ObsQueue;
    private int Green_WaitQueue;
    private int Green_TreatQueue;
    private int Green_ObsQueue;
    private int White_WaitQueue;
    private int White_TreatQueue;
    private int White_ObsQueue;
    private int NonExec_WaitQueue;
    private int NonExec_TreatQueue;
    private int NonExec_ObsQueue;

    private ERStatusLazio(){}

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public int getRed_WaitQueue() {
        return Red_WaitQueue;
    }

    public void setRed_WaitQueue(int red_WaitQueue) {
        Red_WaitQueue = red_WaitQueue;
    }

    public int getRed_TreatQueue() {
        return Red_TreatQueue;
    }

    public void setRed_TreatQueue(int red_TreatQueue) {
        Red_TreatQueue = red_TreatQueue;
    }

    public int getRed_ObsQueue() {
        return Red_ObsQueue;
    }

    public void setRed_ObsQueue(int red_ObsQueue) {
        Red_ObsQueue = red_ObsQueue;
    }

    public int getYellow_WaitQueue() {
        return Yellow_WaitQueue;
    }

    public void setYellow_WaitQueue(int yellow_WaitQueue) {
        Yellow_WaitQueue = yellow_WaitQueue;
    }

    public int getYellow_TreatQueue() {
        return Yellow_TreatQueue;
    }

    public void setYellow_TreatQueue(int yellow_TreatQueue) {
        Yellow_TreatQueue = yellow_TreatQueue;
    }

    public int getYellow_ObsQueue() {
        return Yellow_ObsQueue;
    }

    public void setYellow_ObsQueue(int yellow_ObsQueue) {
        Yellow_ObsQueue = yellow_ObsQueue;
    }

    public int getGreen_WaitQueue() {
        return Green_WaitQueue;
    }

    public void setGreen_WaitQueue(int green_WaitQueue) {
        Green_WaitQueue = green_WaitQueue;
    }

    public int getGreen_TreatQueue() {
        return Green_TreatQueue;
    }

    public void setGreen_TreatQueue(int green_TreatQueue) {
        Green_TreatQueue = green_TreatQueue;
    }

    public int getGreen_ObsQueue() {
        return Green_ObsQueue;
    }

    public void setGreen_ObsQueue(int green_ObsQueue) {
        Green_ObsQueue = green_ObsQueue;
    }

    public int getWhite_WaitQueue() {
        return White_WaitQueue;
    }

    public void setWhite_WaitQueue(int white_WaitQueue) {
        White_WaitQueue = white_WaitQueue;
    }

    public int getWhite_TreatQueue() {
        return White_TreatQueue;
    }

    public void setWhite_TreatQueue(int white_TreatQueue) {
        White_TreatQueue = white_TreatQueue;
    }

    public int getWhite_ObsQueue() {
        return White_ObsQueue;
    }

    public void setWhite_ObsQueue(int white_ObsQueue) {
        White_ObsQueue = white_ObsQueue;
    }

    public int getNonExec_WaitQueue() {
        return NonExec_WaitQueue;
    }

    public void setNonExec_WaitQueue(int nonExec_WaitQueue) {
        NonExec_WaitQueue = nonExec_WaitQueue;
    }

    public int getNonExec_TreatQueue() {
        return NonExec_TreatQueue;
    }

    public void setNonExec_TreatQueue(int nonExec_TreatQueue) {
        NonExec_TreatQueue = nonExec_TreatQueue;
    }

    public int getNonExec_ObsQueue() {
        return NonExec_ObsQueue;
    }

    public void setNonExec_ObsQueue(int nonExec_ObsQueue) {
        NonExec_ObsQueue = nonExec_ObsQueue;
    }

    public static ERStatusLazio dataToContainer(JSONObject data) throws JSONException {
        ERStatusLazio result = new ERStatusLazio();
        result.setComune(data.getString("COMUNE"));
        result.setNome(data.getString("ISTITUTO"));
        result.setUpdateDate(data.getString("DATA"));
        result.setGreen_WaitQueue(data.getInt("VERDI_ATT"));
        result.setGreen_TreatQueue(data.getInt("VERDI_TRATT"));
        result.setGreen_ObsQueue(data.getInt("VERDI_OB"));
        result.setYellow_WaitQueue(data.getInt("GIALLI_ATT"));
        result.setYellow_TreatQueue(data.getInt("GIALLI_TRATT"));
        result.setYellow_ObsQueue(data.getInt("GIALLI_OB"));
        result.setRed_WaitQueue(data.getInt("ROSSI_ATT"));
        result.setRed_TreatQueue(data.getInt("ROSSI_TRATT"));
        result.setRed_ObsQueue(data.getInt("ROSSI_OB"));
        result.setWhite_WaitQueue(data.getInt("BIANCHI_ATT"));
        result.setWhite_TreatQueue(data.getInt("BIANCHI_TRATT"));
        result.setWhite_ObsQueue(data.getInt("BIANCHI_OB"));
        result.setNonExec_WaitQueue(data.getInt("NONESEG_ATT"));
        result.setNonExec_TreatQueue(data.getInt("NONESEG_TRATT"));
        //result.setNonExec_ObsQueue(data.getInt("NONESEG_OB"));
        return result;
    }
}
