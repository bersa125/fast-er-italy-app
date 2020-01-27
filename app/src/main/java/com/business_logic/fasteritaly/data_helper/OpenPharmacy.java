package com.business_logic.fasteritaly.data_helper;

import org.jsoup.nodes.Document;

public class OpenPharmacy {

    private String nome;
    private String orari;
    private String indirizzo;
    private String immagine;
    private double distanza;
    private String turno;

    private OpenPharmacy(){}

    public String getNome() {
        return nome;
    }

    public String getOrari() {
        return orari;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getImmagine() {
        return immagine;
    }

    public double getDistanza(){
        return  distanza;
    }

    public String getTurno(){
        return turno;
    }

    public static OpenPharmacy parseNewOpenPharmacy(Document doc){
        OpenPharmacy res=new OpenPharmacy();
        res.immagine="https:"+doc.getElementsByTag("img").attr("src").replace("foto80","foto728");
        res.nome=upperCaseWords(doc.getElementsByTag("b").get(0).text());
        String[] AddressData=doc.getElementsByTag("a").get(2).text().split("\\s");
        res.indirizzo="";
        for(int i=0; i<AddressData.length-2;i++){
            res.indirizzo=res.indirizzo+AddressData[i];
            if(i!=AddressData.length-3){
                res.indirizzo=res.indirizzo+" ";
            }
        }
        res.distanza=Double.parseDouble(doc.getElementsByTag("b").get(2).text().replace(",","."));
        String apertura=doc.text().split("km ")[1];
        if(apertura.contains("Apertura standard: ")){
            res.orari=apertura.split("Apertura standard: ")[1];
        }else{
            if(apertura.contains("Turno*")){
                res.orari=apertura.split("Turno")[1];
            }else{
                res.orari=apertura;
            }
        }
        //System.err.println(apertura);
        if(apertura.contains("Turno")){
            res.turno=apertura.split("Turno")[1].split("\\s")[1];
            try {
                if (!apertura.split("Turno")[1].split("\\s")[2].equals("Apertura")) {
                    res.turno = res.turno+" "+apertura.split("Turno")[1].split("\\s")[2];
                }
            }catch(Exception e){}
            //System.err.println(res.turno);
        }
        return res;
    }

    private static String upperCaseWords(String str) {
        String res="";
        for(String s:str.split("\\s")){
            res=res+Character.toUpperCase(s.charAt(0))+s.substring(1).toLowerCase()+" ";
        }
        res=res.substring(0,res.length()-1);
        return res;
    }
}
