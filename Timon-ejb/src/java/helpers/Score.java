/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import entities.votacionydebate.Opcion;

/**
 *
 * @author alfonso
 */
public class Score implements Comparable<Score> {
    private int score;
    private Opcion opcion;
    
    public Score(Opcion o, int score) {
        this.opcion = o;
        this.score = score;
    }

    @Override
    public int compareTo(Score o) {
        return getScore() > o.getScore() ? -1 : getScore() < o.getScore() ? 1 : 0;
    }

    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @return the opcion
     */
    public Opcion getOpcion() {
        return opcion;
    }

    /**
     * @param opcion the opcion to set
     */
    public void setOpcion(Opcion opcion) {
        this.opcion = opcion;
    }
    
    
    
}
