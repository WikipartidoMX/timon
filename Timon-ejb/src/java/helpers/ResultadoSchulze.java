/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import entities.votacionydebate.Opcion;
import java.util.List;

/**
 *
 * @author Alfonso Tames
 */
public class ResultadoSchulze {
    
    private List<Score> scores;
    private long[][] pref;
    private long[][] sp;
    private List<Opcion> opciones;
    private long exetime;

    /**
     * @return the scores
     */
    public List<Score> getScores() {
        return scores;
    }

    /**
     * @param scores the scores to set
     */
    public void setScores(List<Score> scores) {
        this.scores = scores;
    }


    /**
     * @return the opciones
     */
    public List<Opcion> getOpciones() {
        return opciones;
    }

    /**
     * @param opciones the opciones to set
     */
    public void setOpciones(List<Opcion> opciones) {
        this.opciones = opciones;
    }

    /**
     * @return the pref
     */
    public long[][] getPref() {
        return pref;
    }

    /**
     * @param pref the pref to set
     */
    public void setPref(long[][] pref) {
        this.pref = pref;
    }

    /**
     * @return the sp
     */
    public long[][] getSp() {
        return sp;
    }

    /**
     * @param sp the sp to set
     */
    public void setSp(long[][] sp) {
        this.sp = sp;
    }

    /**
     * @return the exetime
     */
    public long getExetime() {
        return exetime;
    }

    /**
     * @param exetime the exetime to set
     */
    public void setExetime(long exetime) {
        this.exetime = exetime;
    }

    
}
