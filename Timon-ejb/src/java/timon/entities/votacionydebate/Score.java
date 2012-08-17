/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timon.entities.votacionydebate;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author alfonso
 */
@Entity
public class Score implements Serializable, Comparable<Score> {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int score;
    @OneToOne
    private Opcion opcion;

    public Score() {
    }

    public Score(Opcion o, int score) {
        this.opcion = o;
        this.score = score;
    }

    @Override
    public int compareTo(Score o) {
        return getScore() > o.getScore() ? -1 : getScore() < o.getScore() ? 1 : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Score)) {
            return false;
        }
        Score other = (Score) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.votacionydebate.Score[ id=" + id + " ]";
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
